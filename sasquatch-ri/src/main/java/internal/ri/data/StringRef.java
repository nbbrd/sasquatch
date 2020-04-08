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
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
public final class StringRef {

    @NonNegative
    private int hdr;

    @NonNegative
    private int off;

    @NonNegative
    private int len;

    @NonNull
    public static StringRef parse(@NonNull BytesReader bytes, int base) {
        return new StringRef(
                bytes.getUInt16(base + SEQ.getOffset(false, 0)),
                bytes.getUInt16(base + SEQ.getOffset(false, 1)),
                bytes.getUInt16(base + SEQ.getOffset(false, 2)));
    }

    public static final Seq SEQ = Seq
            .builder()
            .and("hdr", 2)
            .and("off", 2)
            .and("len", 2)
            .build();
}
