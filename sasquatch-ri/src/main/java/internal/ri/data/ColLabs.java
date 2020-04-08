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
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Column Format and Label
 *
 * @see
 * https://github.com/BioStatMatt/sas7bdat/blob/master/vignettes/sas7bdat.rst#column-format-and-label-subheader
 * @author Philippe Charles
 */
@lombok.Value
public final class ColLabs implements SubHeader {

    @lombok.NonNull
    private SubHeaderLocation location;

    /**
     * Column format width
     */
    @NonNegative
    private int formatWidth;

    /**
     * Column format precision
     */
    @NonNegative
    private int formatPrecision;

    /**
     * Column format name ref
     */
    @NonNull
    private StringRef formatName;

    /**
     * Column label ref
     */
    @NonNull
    private StringRef label;

    @NonNull
    public static ColLabs parse(@NonNull BytesReader pageBytes, boolean u64, @NonNull SubHeaderPointer pointer) {
        return parse(pointer.slice(pageBytes), u64, pointer.getLocation());
    }

    private static ColLabs parse(BytesReader bytes, boolean u64, SubHeaderLocation location) {
        return new ColLabs(
                location,
                bytes.getUInt16(SEQ.getOffset(u64, 2)),
                bytes.getUInt16(SEQ.getOffset(u64, 3)),
                StringRef.parse(bytes, SEQ.getOffset(u64, 5)),
                StringRef.parse(bytes, SEQ.getOffset(u64, 6))
        );
    }

    public static final Seq SEQ = Seq
            .builder()
            .and("signature", Seq.U4U8)
            .and("?", 8)
            .and("width", 2)
            .and("precision", 2)
            .and("?", 2 * 4 + 10, 2 * 8 + 10)
            .and("format", StringRef.SEQ)
            .and("label", StringRef.SEQ)
            .build();
}
