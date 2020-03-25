/*
 * Copyright 2013 National Bank of Belgium
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
package internal.ri.data.rows;

import internal.bytes.BytesCursor;
import internal.ri.data.Document;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
public interface RowCursor extends BytesCursor {

    @NonNull
    public static RowCursor of(@NonNull SeekableByteChannel sbc, @NonNull Document doc) throws IOException {
        if (doc.getCompression().isKnown()) {
            switch (doc.getCompression().get()) {
                case CHAR:
                    return CompressedDataCursor.of(sbc, doc.getHeader(), doc.getRowSize(), RLEDecompressor.INSTANCE);
                case BIN:
                    return CompressedDataCursor.of(sbc, doc.getHeader(), doc.getRowSize(), RDCDecompressor.INSTANCE);
                case NONE:
                    return PackedBinaryDataCursor.of(sbc, doc.getHeader(), doc.getRowSize());
            }
        }
        throw new IOException("Cannot handle compression " + doc.getCompression().toString());
    }
}
