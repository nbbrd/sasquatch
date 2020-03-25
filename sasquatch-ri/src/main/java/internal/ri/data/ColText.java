/*
 * Copyright 2020 National Bank of Belgium
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

import internal.bytes.Bytes;
import internal.bytes.BytesReader;
import internal.ri.base.SubHeader;
import internal.ri.base.SubHeaderLocation;
import internal.ri.base.SubHeaderPointer;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Column Text
 *
 * @see
 * https://github.com/BioStatMatt/sas7bdat/blob/master/vignettes/sas7bdat.rst#column-text-subheader
 * @author Philippe Charles
 */
@lombok.Value
public class ColText implements SubHeader {

    @lombok.NonNull
    private SubHeaderLocation location;

    @lombok.NonNull
    private BytesReader content;

    @NonNull
    public static ColText parse(@NonNull BytesReader pageBytes, boolean u64, @NonNull SubHeaderPointer pointer) {
        byte[] copied = pointer.slice(pageBytes).toArray();
        return new ColText(pointer.getLocation(), Bytes.wrap(copied, pageBytes.getOrder()));
    }
}
