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
import internal.bytes.Seq;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Page
 *
 * @see
 * https://github.com/BioStatMatt/sas7bdat/blob/master/vignettes/sas7bdat.rst#sas7bdat-pages
 * @author Philippe Charles
 */
@lombok.Value
public final class PageHeader {

    /**
     * Page index
     */
    @NonNegative
    private int index;

    private short deletedOffset;

    /**
     * Page type
     */
    @NonNull
    private PValue<PageType, Short> type;

    @NonNegative
    private short dataBlockCount;

    /**
     * Subheader pointers count
     */
    @XRef(var = "SC")
    @NonNegative
    private short subHeaderCount;

    @NonNegative
    private int subHeaderOffset;

    @NonNull
    public SubHeaderLocation getSubHeaderLocation(int subHeaderIndex) {
        return new SubHeaderLocation(index, subHeaderIndex);
    }

    @NonNull
    public static PageHeader parse(@NonNull BytesReader pageBytes, boolean u64, int index) {
        short deletedOffset = pageBytes.getInt16(SEQ.getOffset(u64, 2));
        short type = pageBytes.getInt16(SEQ.getOffset(u64, 3));
        short dataBlockCount = pageBytes.getInt16(SEQ.getOffset(u64, 4));
        short subHeaderCount = pageBytes.getInt16(SEQ.getOffset(u64, 5));
        int subHeaderOffset = pageBytes.getUInt16(SEQ.getOffset(u64, 6));

        return new PageHeader(index, deletedOffset, PageType.tryParse(type), dataBlockCount, subHeaderCount, subHeaderOffset);
    }

    public static boolean canAlign(
            @NonNegative int requiredLength,
            @NonNegative int availableLength,
            @NonNegative int base,
            @NonNegative int offset,
            @NonNegative int align) {
        return availableLength - (base + offset + align) >= requiredLength;
    }

    public static int getHeadLength(boolean u64) {
        return SEQ.getTotalLength(u64);
    }

    public static final Seq SEQ = Seq
            .builder()
            .and("signature", 4)
            .and("?", 8, 20)
            .and("deletedOffset", 4, 8)
            .and("type", 2)
            .and("dataBlockCount", 2)
            .and("subHeaderCount", 2)
            .and("subHeaderOffset", 2)
            .build();
}
