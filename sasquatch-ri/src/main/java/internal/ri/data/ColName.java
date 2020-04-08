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

import internal.bytes.Seq;
import internal.ri.base.SubHeaderLocation;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Column Name
 *
 * @see
 * https://github.com/BioStatMatt/sas7bdat/blob/master/vignettes/sas7bdat.rst#column-name-pointers
 * @author Philippe Charles
 */
@lombok.Value
public final class ColName {

    @lombok.NonNull
    private SubHeaderLocation location;

    /**
     * ColName index
     */
    @NonNegative
    private int index;

    /**
     * Column name ref
     */
    @NonNull
    private StringRef name;

    public static final Seq SEQ = Seq
            .builder()
            .and("name", StringRef.SEQ)
            .and("zeroes", 2)
            .build();
}
