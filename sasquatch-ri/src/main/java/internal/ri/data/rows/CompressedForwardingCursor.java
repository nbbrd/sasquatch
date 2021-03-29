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

import internal.bytes.Bytes;
import internal.bytes.BytesReader;
import internal.ri.base.Header;
import internal.ri.base.PageCursor;
import internal.ri.base.PageHeader;
import internal.ri.base.PageType;
import internal.ri.base.SubHeaderFormat;
import internal.ri.base.SubHeaderLocation;
import internal.ri.base.SubHeaderPointer;
import internal.ri.data.RowSize;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
final class CompressedForwardingCursor extends ForwardingCursor {

    @NonNull
    public static RowCursor of(@NonNull SeekableByteChannel sbc, @NonNull Header header, @NonNull RowSize rowSize, @NonNull Decompressor decompressor) {
        return new CompressedForwardingCursor(
                PageCursor.of(sbc, header),
                header.isU64(),
                rowSize.getCount(),
                rowSize.getLastMeta(),
                decompressor,
                Bytes.allocate(rowSize.getLength(), header.getEndianness()),
                INITIAL_INDEX,
                null,
                null,
                null,
                false
        );
    }

    private final PageCursor pageCursor;
    private final boolean u64;
    @lombok.Getter
    private final int count;
    private final SubHeaderLocation lastMetaLocation;
    private final Decompressor decompressor;
    private final Bytes rowBytes;
    @lombok.Getter
    private int index;
    private PageHeader currentPage;
    private SubHeaderPointer currentPointer;
    private SubHeaderPointer nextPointer;
    private boolean rowLoaded;

    @Override
    protected boolean hasNextRow() {
        return index + 1 < count;
    }

    @Override
    protected boolean hasNextRowInCurrentPage() throws IOException {
        if (currentPointer != null && currentPointer.getLocation().getIndex() < currentPage.getSubHeaderCount()) {
            nextPointer = SubHeaderPointer.parse(pageCursor.getBytes(), u64, currentPointer.getLocation().next());
            return !nextPointer.getFormat().isKnownAs(SubHeaderFormat.TRUNCATED);
        }
        return false;
    }

    @Override
    protected void moveToNextRowInCurrentPage() throws IOException {
        currentPointer = nextPointer;
        nextPointer = null;
        rowLoaded = false;
        index++;
    }

    @Override
    protected void moveToFirstRowInNextPage() throws IOException {
        if (currentPage == null) {
            pageCursor.moveTo(lastMetaLocation.getPage());
            currentPage = PageHeader.parse(pageCursor.getBytes(), u64, pageCursor.getIndex());
            currentPointer = SubHeaderPointer.parse(pageCursor.getBytes(), u64, lastMetaLocation.next());
        } else {
            currentPage = ForwardingCursor.nextPageWithData(pageCursor, u64, CompressedForwardingCursor::hasData);
            currentPointer = SubHeaderPointer.parse(pageCursor.getBytes(), u64, new SubHeaderLocation(currentPage.getIndex(), 0));
        }
        nextPointer = null;
        rowLoaded = false;
        index++;
    }

    @Override
    protected boolean isDeleted() throws IOException {
        // TODO
        return false;
    }

    @Override
    public BytesReader getBytes() throws IOException {
        if (!rowLoaded) {
            if (isCurrentRowNotCompressed()) {
                pageCursor.getBytes().copyTo(currentPointer.getOffset(), rowBytes, 0, rowBytes.getLength());
            } else {
                decompressor.uncompress(pageCursor.getBytes(), currentPointer.getOffset(), rowBytes, currentPointer.getLength());
            }
            rowLoaded = true;
        }
        return rowBytes;
    }

    private boolean isCurrentRowNotCompressed() {
        return currentPointer.getLength() == rowBytes.getLength();
    }

    private static boolean hasData(BytesReader pageBytes, PageHeader page, boolean u64) {
        return page.getType().isKnownAs(PageType.META);
    }
}
