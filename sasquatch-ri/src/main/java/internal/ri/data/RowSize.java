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
import internal.bytes.Seq;
import internal.ri.base.SubHeader;
import internal.ri.base.SubHeaderLocation;
import internal.ri.base.SubHeaderPointer;
import internal.ri.base.XRef;
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
    private int firstPageMaxCount;

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

    @XRef(var = "npshd+nshpl")
    @lombok.NonNull
    private SubHeaderLocation lastMetaLocation;

    @lombok.NonNull
    private StringRef labelRef;

    @NonNull
    public static RowSize parse(@NonNull BytesReader pageBytes, boolean u64, @NonNull SubHeaderPointer pointer) {
        BytesReader bytes = pointer.slice(pageBytes);

        int length = Seq.getU4U8(bytes, SEQ.getOffset(u64, 5), u64);
        int count = Seq.getU4U8(bytes, SEQ.getOffset(u64, 6), u64);
        int firstPageMaxCount = Seq.getU4U8(bytes, SEQ.getOffset(u64, 15), u64);
        short lcs = bytes.getInt16(SEQ.getOffset(u64, 37));
        short nct = bytes.getInt16(SEQ.getOffset(u64, 46));
        SubHeaderLocation lastMetaLocation = SubHeaderLocation.parse(SEQ.getOffset(u64, 23), bytes, u64);

        StringRef labelRef = StringRef.parse(bytes, !u64 ? (350) : (678));

        return new RowSize(pointer.getLocation(), length, count, firstPageMaxCount, lcs, nct, lastMetaLocation, labelRef);
    }

    public static final Seq SEQ = Seq
            .builder()
            // Group1: initial header 
            // @0+72|0+144 (=18*U4U8)
            .and("signature", Seq.U4U8) //#0
            .and("?", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("rowLength", Seq.U4U8)
            .and("rowCount", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("ncfl1", Seq.U4U8)
            .and("ncfl2", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("pageSize", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("firstPageMaxCount", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("?", Seq.U4U8)
            // Group2: ?
            // @72+192|144+368 (=16+44*U4U8)
            .and("zeroes", 148, 296) //#18
            .and("pageSignature", 4)
            .and("zeroes", 40, 68)
            // Group3: locations 
            // @264+40|512+80 (=10*U4U8)
            .and("?", SubHeaderLocation.SEQ) //#21
            .and("zeroes", 2, 6)
            .and("lastMetaLocation", SubHeaderLocation.SEQ) //#23
            .and("zeroes", 2, 6)
            .and("?", SubHeaderLocation.SEQ) //#25
            .and("zeroes", 2, 6)
            .and("?", SubHeaderLocation.SEQ) //#27
            .and("zeroes", 2, 6)
            .and("?", SubHeaderLocation.SEQ) //#29
            .and("zeroes", 2, 6)
            // Group4: locations? 
            // @304+40|592+80 (=10*U4U8)
            .and("zeroes", 40, 80) //#31
            // Group5: counters?
            // @344+136|672+136
            .and("?", 2)
            .and("?", 2)
            .and("?", 2)
            .and("?", 2)
            .and("?", 2)
            .and("lcs", 2) //#37
            .and("?", 2)
            .and("?", 2)
            .and("?", 2)
            .and("zeroes", 8)
            .and("?", 2)
            .and("?", 2)
            .and("?", 2)
            .and("?", 2)
            .and("lcp", 2) //#46
            .and("zeroes", 36)
            .and("?", 2)
            .and("?", 2)
            .and("?", 2)
            .and("?", 2)
            .and("?", 2)
            .and("zeroes", 12)
            .and("?", 2)
            .and("zeroes", 27)
            .and("?", 1)
            .and("zeroes", 12)
            .build();
}
