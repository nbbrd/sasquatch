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
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Column Attribute
 *
 * @see
 * https://github.com/BioStatMatt/sas7bdat/blob/master/vignettes/sas7bdat.rst#column-attribute-vectors
 * @author Philippe Charles
 */
@lombok.Value
public final class ColAttrs implements SubHeader {

    @lombok.NonNull
    private SubHeaderLocation location;

    @lombok.NonNull
    private List<ColAttr> items;

    @NonNull
    public static ColAttrs parse(@NonNull BytesReader pageBytes, boolean u64, @NonNull SubHeaderPointer pointer) {
        return parse(pointer.slice(pageBytes), u64, pointer.getLocation());
    }

    private static ColAttrs parse(BytesReader bytes, boolean u64, SubHeaderLocation location) {
        int lcav = getAttributeVectorLength(u64);

        int count = (bytes.getLength() - (u64 ? 28 : 20)) / lcav;
        int offset = lcav;
        int length = lcav;

        int columnWitdhOffset = getColumnWidthOffset(u64);
        int columnTypeOffset = getColumnTypeOffset(u64);

        ColAttr[] result = new ColAttr[count];
        for (int i = 0; i < result.length; i++) {
            int base = offset + i * length;

            int columnOffsetInDataRow = Seq.parseU4U8(bytes, base, u64);
            int columnWidth = bytes.getInt32(base + columnWitdhOffset);
            byte columnType = bytes.getByte(base + columnTypeOffset);

            result[i] = new ColAttr(location, i, columnOffsetInDataRow, columnWidth, ColType.tryParse(columnType));
        }
        return new ColAttrs(location, Arrays.asList(result));
    }

    private static int getColumnWidthOffset(boolean u64) {
        return u64 ? 8 : 4;
    }

    private static int getColumnTypeOffset(boolean u64) {
        return u64 ? 14 : 10;
    }

    @XRef(var = "LCAV")
    private static int getAttributeVectorLength(boolean u64) {
        return u64 ? 16 : 12;
    }
}
