/*
 * Copyright 2016 National Bank of Belgium
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
package internal.ri.base;

import internal.bytes.BytesReader;
import internal.bytes.Record;
import internal.bytes.Record.BiIntFunction;
import internal.bytes.RecordLength;
import java.nio.ByteOrder;
import java.util.List;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
public final class RowIndex implements SubHeader {

    @lombok.NonNull
    private SubHeaderLocation location;

    @NonNegative
    private int rowNumber;

    @NonNegative
    private int pageNumber;

    @NonNegative
    private short subHeaderNumber;

    @NonNull
    public SubHeaderLocation getLastRowLocation() {
        return new SubHeaderLocation(pageNumber - 1, subHeaderNumber - 1);
    }

    @NonNull
    public static List<RowIndex> parseAll(@NonNull PageHeader page, @NonNull BytesReader pageBytes, boolean u64) {
        int count = page.getSubHeaderCount();
        int offset = getPageOffset(page, u64, pageBytes);
        int length = getLength(u64);
        return Record.toList(count, offset, length, getFactory(page.getIndex(), pageBytes, u64));
    }

    private static int getPageOffset(PageHeader page, boolean u64, BytesReader pageBytes) {
        int requiredLength = getLength(u64) * page.getSubHeaderCount();
        int availableLength = pageBytes.getLength();

        int base = PageHeader.getHeadLength(u64) + (u64 ? 8 : 4);
        int offset = page.getSubHeaderOffset();
        int align = 4;

        int result = base + offset;
        if (!u64 & PageHeader.canAlign(requiredLength, availableLength, base, offset, align)) {
            result += align;
        }
        return result;
    }

    private static BiIntFunction<RowIndex> getFactory(int pageIndex, BytesReader pageBytes, boolean u64) {
        BytesReader bigEndian = pageBytes.duplicate(ByteOrder.BIG_ENDIAN);

        return u64
                ? (i, base) -> new RowIndex(
                        new SubHeaderLocation(pageIndex, i),
                        bigEndian.getInt64As32(base + LENGTH_64.getOffset(0)),
                        pageBytes.getInt64As32(base + LENGTH_64.getOffset(1)),
                        pageBytes.getInt16(base + LENGTH_64.getOffset(2)))
                : (i, base) -> new RowIndex(
                        new SubHeaderLocation(pageIndex, i),
                        bigEndian.getInt32(base + LENGTH_32.getOffset(0)),
                        pageBytes.getInt32(base + LENGTH_32.getOffset(1)),
                        pageBytes.getInt16(base + LENGTH_32.getOffset(2)));
    }

    private static int getLength(boolean u64) {
        return u64 ? LENGTH_64.getTotalLength() : LENGTH_32.getTotalLength();
    }

    private static final RecordLength LENGTH_32 = RecordLength.of(4, 4, 2);
    private static final RecordLength LENGTH_64 = RecordLength.of(8, 8, 2);
}
