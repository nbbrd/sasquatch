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
import internal.bytes.Record;
import internal.ri.base.SubHeader;
import internal.ri.base.SubHeaderLocation;
import internal.ri.base.SubHeaderPointer;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Column Name
 *
 * @see
 * https://github.com/BioStatMatt/sas7bdat/blob/master/vignettes/sas7bdat.rst#column-name-pointers
 * @author Philippe Charles
 */
@lombok.Value
public final class ColNames implements SubHeader {

    @lombok.NonNull
    private SubHeaderLocation location;

    @lombok.NonNull
    private List<ColName> items;

    @NonNull
    public static ColNames parse(@NonNull BytesReader pageBytes, boolean u64, @NonNull SubHeaderPointer pointer) {
        return parse(pointer.slice(pageBytes), u64, pointer.getLocation());
    }

    private static ColNames parse(BytesReader bytes, boolean u64, SubHeaderLocation location) {
        int length = ColName.SEQ.getTotalLength(u64);
        int count = (bytes.getLength() - (u64 ? 28 : 20)) / length;
        int offset = (u64 ? 16 : 12);

        return new ColNames(location, Record.toList(count, offset, length, getFactory(bytes, location)));
    }

    private static Record.BiIntFunction<ColName> getFactory(BytesReader bytes, SubHeaderLocation location) {
        return (i, base) -> new ColName(location, i, StringRef.parse(bytes, base));
    }
}
