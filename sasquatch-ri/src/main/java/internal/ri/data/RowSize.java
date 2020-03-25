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
package internal.ri.data;

import internal.bytes.BytesReader;
import internal.ri.base.SubHeader;
import internal.ri.base.SubHeaderLocation;
import internal.ri.base.SubHeaderPointer;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Row size
 *
 * @see
 * https://github.com/BioStatMatt/sas7bdat/blob/master/vignettes/sas7bdat.rst#row-size-subheader
 * @author Philippe Charles
 */
@lombok.Value
public final class RowSize implements SubHeader {

    @lombok.NonNull
    private SubHeaderLocation location;

    /**
     * Row length (in bytes)
     */
    @NonNegative
    private int length;

    /**
     * Total row count
     */
    @NonNegative
    private int count;

    /**
     * Max row count on mix page
     */
    @NonNegative
    int firstPageMaxCount;

    /**
     * Length of Creator Software string
     */
    @NonNegative
    private short lcs;

    /**
     * Number of Column Text subheaders in file
     */
    @NonNegative
    private short nct;

    @NonNegative
    private int npshd;

    @NonNegative
    private short nshpl;

    @NonNull
    public SubHeaderLocation getLastMetaLocation() {
        return new SubHeaderLocation(npshd - 1, nshpl - 1);
    }
    
    @NonNull
    public static RowSize parse(@NonNull BytesReader pageBytes, boolean u64, @NonNull SubHeaderPointer pointer) {
        BytesReader bytes = pointer.slice(pageBytes);

        int lenght = u64 ? bytes.getInt64As32(40) : bytes.getInt32(20);
        int count = u64 ? bytes.getInt64As32(48) : bytes.getInt32(24);
        int firstPageMaxCount = u64 ? bytes.getInt64As32(120) : bytes.getInt32(60);
        short lcs = bytes.getInt16(u64 ? 682 : 354);
        short nct = bytes.getInt16(u64 ? 748 : 420);
        int npshd = u64 ? bytes.getInt64As32(528) : bytes.getInt32(272);
        short nshpl = bytes.getInt16(u64 ? 536 : 276);

        return new RowSize(pointer.getLocation(), lenght, count, firstPageMaxCount, lcs, nct, npshd, nshpl);
    }
}
