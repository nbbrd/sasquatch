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
import internal.bytes.Seq;
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

    @lombok.NonNull
    private SubHeaderLocation lastRowLocation;

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

        return (i, base) -> new RowIndex(
                new SubHeaderLocation(pageIndex, i),
                Seq.parseU4U8(bigEndian, base + SEQ.getOffset(u64, 0), u64),
                SubHeaderLocation.parse(pageBytes, base + SEQ.getOffset(u64, 1), u64));
    }

    private static int getLength(boolean u64) {
        return SEQ.getTotalLength(u64);
    }

    private static final Seq SEQ = Seq
            .builder()
            .and("rowNumber", Seq.U4U8)
            .and("lastRowLocation", SubHeaderLocation.SEQ)
            .build();
}
