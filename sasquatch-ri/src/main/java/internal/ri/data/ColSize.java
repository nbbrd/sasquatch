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
 * Column Size
 *
 * @see
 * https://github.com/BioStatMatt/sas7bdat/blob/master/vignettes/sas7bdat.rst#column-size-subheader
 * @author Philippe Charles
 */
@lombok.Value
public final class ColSize implements SubHeader {

    @lombok.NonNull
    private SubHeaderLocation location;

    /**
     * Number of columns
     */
    @XRef(var = "NCOL")
    @NonNegative
    private int count;

    @NonNull
    public static ColSize parse(@NonNull BytesReader pageBytes, boolean u64, @NonNull SubHeaderPointer pointer) {
        BytesReader bytes = pointer.slice(pageBytes);

        return new ColSize(
                pointer.getLocation(),
                Seq.getU4U8(bytes, SEQ.getOffset(u64, 1), u64)
        );
    }

    public static final Seq SEQ = Seq
            .builder()
            .and("signature", Seq.U4U8)
            .and("columnCount", Seq.U4U8)
            .and("?", Seq.U4U8)
            .build();
}
