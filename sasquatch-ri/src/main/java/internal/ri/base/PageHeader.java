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
        short type = pageBytes.getInt16(u64 ? 32 : 16);
        short dataBlockCount = pageBytes.getInt16(u64 ? 34 : 18);
        short subHeaderCount = pageBytes.getInt16(u64 ? 36 : 20);
        int subHeaderOffset = pageBytes.getUInt16(u64 ? 38 : 22);

        return new PageHeader(index, PageType.tryParse(type), dataBlockCount, subHeaderCount, subHeaderOffset);
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
        return u64 ? HEAD_LENGTH_64 : HEAD_LENGTH_32;
    }

    public static final int HEAD_LENGTH_32 = 4 + (12) + (2 + 2 + 2 + 2);
    public static final int HEAD_LENGTH_64 = 4 + (28) + (2 + 2 + 2 + 2);
}
