/*
 * Copyright 2013 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal.ri.data.rows;

import internal.bytes.BytesReader;
import internal.bytes.BytesWithOffset;
import internal.ri.base.Header;
import internal.ri.base.PageCursor;
import internal.ri.base.PageHeader;
import internal.ri.base.PageType;
import internal.ri.base.SubHeaderPointer;
import internal.ri.data.RowSize;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import lombok.AccessLevel;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
final class PackedBinaryDataCursor extends AbstractRowCursor {

    @NonNull
    public static RowCursor of(@NonNull SeekableByteChannel sbc, @NonNull Header header, @NonNull RowSize rowSize) {
        return new PackedBinaryDataCursor(
                PageCursor.of(sbc, header),
                header.isU64(),
                rowSize.getCount(),
                rowSize.getLength(),
                new BytesWithOffset(),
                INITIAL_INDEX,
                null,
                0
        );
    }

    @lombok.NonNull
    private final PageCursor pageCursor;

    private final boolean u64;

    @lombok.Getter
    private final int count;

    @NonNegative
    private final int rowLength;

    @lombok.Getter
    private final BytesWithOffset bytes;

    @lombok.Getter
    private int index;

    @Nullable
    private PageHeader currentPage;

    @NonNegative
    private int remainingRowsInCurrentPage;

    @Override
    protected boolean hasNextRow() {
        return index + 1 < count;
    }

    @Override
    protected boolean hasNextRowInCurrentPage() {
        return remainingRowsInCurrentPage > 0;
    }

    @Override
    protected void moveToNextRowInCurrentPage() {
        bytes.incrementOffset(rowLength);
        remainingRowsInCurrentPage--;
        index++;
    }

    @Override
    protected void moveToFirstRowInNextPage() throws IOException {
        currentPage = nextPageWithData(pageCursor, u64, PackedBinaryDataCursor::hasData);
        bytes.reset(pageCursor.getBytes(), 0);
        setBaseAndRemaining();
        remainingRowsInCurrentPage--;
        index++;
    }

    private void setBaseAndRemaining() throws IOException {
        switch (currentPage.getType().get()) {
            case MIX:
                bytes.incrementOffset(getRowOffsetInMix(currentPage, u64, rowLength, pageCursor.getBytes()));
                remainingRowsInCurrentPage = getRowCountInMix(currentPage);
                break;
            case DATA:
                bytes.incrementOffset(PageHeader.getHeadLength(u64));
                remainingRowsInCurrentPage = currentPage.getDataBlockCount();
                break;
            default:
                throw new RuntimeException();
        }
    }

    private static int getRowCountInMix(PageHeader page) {
        return page.getDataBlockCount() - page.getSubHeaderCount();
    }

    //# skip subheader pointers & round up to 8-byte boundary if possible
    private static int getRowOffsetInMix(PageHeader page, boolean u64, int rowLength, BytesReader pageBytes) {
        int requiredLength = rowLength * getRowCountInMix(page);
        int availableLength = getMinimumSubHeaderOffsetInMix(pageBytes, u64, page);

        int base = PageHeader.getHeadLength(u64);
        int offset = page.getSubHeaderCount() * (u64 ? 24 : 12);
        int align = offset % 8;

        int result = base + offset;
        if (PageHeader.canAlign(requiredLength, availableLength, base, offset, align)) {
            result += align;
        }
        return result;
    }

    private static int getMinimumSubHeaderOffsetInMix(BytesReader pageBytes, boolean u64, PageHeader page) {
        return SubHeaderPointer.parseAll(pageBytes, u64, page)
                .stream()
                .mapToInt(SubHeaderPointer::getOffset)
                .min()
                .orElse(pageBytes.getLength());
    }

    private static boolean hasData(BytesReader pageBytes, PageHeader page, boolean u64) {
        return page.getType().isKnownAs(PageType.MIX) 
                || page.getType().isKnownAs(PageType.DATA);
    }
}
