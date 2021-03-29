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
     * Deleted row count
     */
    @NonNegative
    private int deletedCount;

    /**
     * Max row count on mix page
     */
    @NonNegative
    private int firstPageMaxCount;

    @lombok.NonNull
    private SubHeaderLocation firstMeta;

    @XRef(var = "npshd+nshpl")
    @lombok.NonNull
    private SubHeaderLocation lastMeta;

    @lombok.NonNull
    private SubHeaderLocation firstRow;

    @lombok.NonNull
    private SubHeaderLocation lastRow;

    @lombok.NonNull
    private SubHeaderLocation firstColLab;

    @lombok.NonNull
    private StringRef label;

    @lombok.NonNull
    private StringRef compression;

    @lombok.NonNull
    private StringRef proc;

    /**
     * Number of Column Text subheaders in file
     */
    @NonNegative
    private short nct;

    @NonNull
    public static RowSize parse(@NonNull BytesReader pageBytes, boolean u64, @NonNull SubHeaderPointer pointer) {
        BytesReader bytes = pointer.slice(pageBytes);

        short nct = bytes.getInt16(u64 ? 748 : 420);

        return new RowSize(
                pointer.getLocation(),
                Seq.parseU4U8(bytes, SEQ.getOffset(u64, 5), u64),
                Seq.parseU4U8(bytes, SEQ.getOffset(u64, 6), u64),
                Seq.parseU4U8(bytes, SEQ.getOffset(u64, 7), u64),
                Seq.parseU4U8(bytes, SEQ.getOffset(u64, 15), u64),
                SubHeaderLocation.parse(bytes, SEQ.getOffset(u64, 21), u64),
                SubHeaderLocation.parse(bytes, SEQ.getOffset(u64, 22), u64),
                SubHeaderLocation.parse(bytes, SEQ.getOffset(u64, 23), u64),
                SubHeaderLocation.parse(bytes, SEQ.getOffset(u64, 24), u64),
                SubHeaderLocation.parse(bytes, SEQ.getOffset(u64, 25), u64),
                StringRef.parse(bytes, SEQ.getOffset(u64, 28)),
                StringRef.parse(bytes, SEQ.getOffset(u64, 30)),
                StringRef.parse(bytes, SEQ.getOffset(u64, 32)),
                nct);
    }

    private static final Seq PADDED_LOCATION = Seq
            .builder()
            .and("location", SubHeaderLocation.SEQ)
            .and("zeroes", 2, 6)
            .build();

    public static final Seq SEQ = Seq
            .builder()
            /**
             * Group1: initial header
             *
             * @0+72|0+144 (=18*U4U8)
             */
            .and("signature", Seq.U4U8) //#0
            .and("?", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("rowLength", Seq.U4U8) //#5
            .and("rowCount", Seq.U4U8) //#6
            .and("deletedRowCount", Seq.U4U8) //#7
            .and("?", Seq.U4U8)
            .and("ncfl1", Seq.U4U8)
            .and("ncfl2", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("pageSize", Seq.U4U8)
            .and("?", Seq.U4U8)
            .and("firstPageMaxCount", Seq.U4U8) //#15
            .and("?", Seq.U4U8)
            .and("?", Seq.U4U8)
            /**
             * Group2: ?
             *
             * @72+192|144+368 (=44*U4U8+16)
             */
            .and("zeroes", 148, 296) //#18
            .and("pageSignature", 4)
            .and("zeroes", 40, 68)
            /**
             * Group3: subheader locations
             *
             * @264+40|512+80 (=10*U4U8)
             */
            .and("firstMeta", PADDED_LOCATION) //#21
            .and("lastMeta", PADDED_LOCATION) //#22
            .and("firstRow", PADDED_LOCATION) //#23
            .and("lastRow", PADDED_LOCATION) //#24
            .and("firstColLab", PADDED_LOCATION) //#25
            /**
             * Group4: subheader locations?
             *
             * @304+40|592+80 (=10*U4U8)
             */
            .and("notUsed", 40, 80) //#26
            /**
             * Group5: string references
             *
             * @344+36|672+36 (=3*(6+6))
             */
            .and("?", 6)
            .and("label", StringRef.SEQ) //#28
            .and("?", 6)
            .and("compression", StringRef.SEQ) //#30
            .and("?", 6)
            .and("procedure", StringRef.SEQ) //#32
            /**
             * Group6: string references?
             *
             * @380+36|708+36 (=3*(6+6))
             */
            .and("notUsed", 36)
            /**
             * Group7: ?
             *
             * @416+64|744+64
             */
            .and("?", 2)
            .and("?", 2)
            .and("nct", 2)
            .and("?", 2)
            .and("?", 2)
            .and("zeroes", 12)
            .and("?", 2)
            .and("zeroes", 27)
            .and("?", 1)
            .and("zeroes", 12)
            .build();
}
