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
package internal.ri.base;

import internal.bytes.BytesReader;
import internal.bytes.PValue;
import internal.bytes.Record;
import internal.bytes.Seq;
import java.util.List;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Subheader
 *
 * @see
 * https://github.com/BioStatMatt/sas7bdat/blob/master/vignettes/sas7bdat.rst#subheader-pointers
 * @author Philippe Charles
 */
@lombok.Value
public final class SubHeaderPointer {

    @lombok.NonNull
    private SubHeaderLocation location;

    /**
     * Offset from page start to subheader
     */
    @NonNegative
    private int offset;

    /**
     * Length of subheader
     */
    @XRef(var = "QL")
    @NonNegative
    private int length;

    /**
     * Compression
     */
    @XRef(var = "COMP")
    @NonNull
    private PValue<SubHeaderFormat, Byte> format;

    @NonNull
    public BytesReader slice(@NonNull BytesReader bytes) {
        return bytes.slice(offset, length);
    }

    public boolean hasContent() {
        return length > 0;
    }

    @NonNull
    public static SubHeaderPointer parse(@NonNull BytesReader pageBytes, boolean u64, @NonNull SubHeaderLocation location) {
        int offset = getOffset(u64);
        int length = getLength(u64);
        return parse(pageBytes, u64, location, Record.getBase(offset, length, location.getIndex()));
    }

    @NonNull
    public static List<SubHeaderPointer> parseAll(@NonNull BytesReader pageBytes, boolean u64, @NonNull PageHeader page) {
        int count = page.getSubHeaderCount();
        int offset = getOffset(u64);
        int length = getLength(u64);
        return Record.toList(count, offset, length, (i, base) -> parse(pageBytes, u64, new SubHeaderLocation(page.getIndex(), i), base));
    }

    private static SubHeaderPointer parse(BytesReader pageBytes, boolean u64, SubHeaderLocation location, int base) {
        int offset = Seq.parseU4U8(pageBytes, base + SEQ.getOffset(u64, 0), u64);
        int length = Seq.parseU4U8(pageBytes, base + SEQ.getOffset(u64, 1), u64);
        byte format = pageBytes.getByte(base + SEQ.getOffset(u64, 2));

        return new SubHeaderPointer(
                location, offset, length,
                SubHeaderFormat.tryParse(format)
        );
    }

    private static int getOffset(boolean u64) {
        return PageHeader.getHeadLength(u64);
    }

    private static int getLength(boolean u64) {
        return SEQ.getTotalLength(u64);
    }

    private static final Seq SEQ = Seq
            .builder()
            .and("offset", Seq.U4U8)
            .and("length", Seq.U4U8)
            .and("format", 1)
            .and("st", 1)
            .and("zeroes", 2, 6)
            .build();
}
