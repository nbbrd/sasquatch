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

import internal.bytes.BytesReader;
import internal.bytes.PValue;
import internal.ri.base.SubHeader;
import internal.ri.base.SubHeaderFormat;
import internal.ri.base.SubHeaderPointer;
import java.util.Optional;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public enum DescriptorType implements SubHeader.Parser {

    ROW_SIZE(RowSize::parse),
    COL_SIZE(ColSize::parse),
    COL_TEXT(ColText::parse),
    COL_ATTRS(ColAttrs::parse),
    COL_NAMES(ColNames::parse),
    COL_LABS(ColLabs::parse),
    COL_LIST(ColList::parse),
    SUBH_CNT(SubhCnt::parse);

    @lombok.experimental.Delegate
    private final SubHeader.Parser parser;

    private static PValue<DescriptorType, String> tryParse32(int value) {
        switch (value) {
            case 0xF7F7F7F7:
                return PValue.known(ROW_SIZE);
            case 0xF6F6F6F6:
                return PValue.known(COL_SIZE);
            case 0xFFFFFFFD:
                return PValue.known(COL_TEXT);
            case 0xFFFFFFFC:
                return PValue.known(COL_ATTRS);
            case 0xFFFFFFFF:
                return PValue.known(COL_NAMES);
            case 0xFFFFFBFE:
                return PValue.known(COL_LABS);
            case 0xFFFFFFFE:
                return PValue.known(COL_LIST);
            case 0xFFFFFC00:
                return PValue.known(SUBH_CNT);
        }
        return PValue.unknown(Integer.toHexString(value));
    }

    private static PValue<DescriptorType, String> tryParse64(long value) {
        String hex = Long.toHexString(value);
        switch (hex) {
            case "f7f7f7f7":
            case "f7f7f7f700000000":
            case "f7f7f7f7fffffbfe":
                return PValue.known(ROW_SIZE);
            case "f6f6f6f6":
            case "f6f6f6f600000000":
            case "f6f6f6f6fffffbfe":
                return PValue.known(COL_SIZE);
            case "fffffffffffffffd":
                return PValue.known(COL_TEXT);
            case "fffffffffffffffc":
                return PValue.known(COL_ATTRS);
            case "ffffffffffffffff":
                return PValue.known(COL_NAMES);
            case "fffffffffffffbfe":
                return PValue.known(COL_LABS);
            case "fffffffffffffffe":
                return PValue.known(COL_LIST);
            case "fffffffffffffc00":
                return PValue.known(SUBH_CNT);
        }
        return PValue.unknown(hex);
    }

    public static Optional<PValue<DescriptorType, String>> tryParse(BytesReader pageBytes, boolean u64, SubHeaderPointer pointer) {
        if (pointer.getFormat().isKnownAs(SubHeaderFormat.PLAIN) && pointer.hasContent()) {
            int offset = pointer.getOffset();
            return u64
                    ? Optional.of(tryParse64(pageBytes.getInt64(offset)))
                    : Optional.of(tryParse32(pageBytes.getInt32(offset)));
        }
        return Optional.empty();
    }
}
