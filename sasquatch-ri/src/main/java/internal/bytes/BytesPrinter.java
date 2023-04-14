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
package internal.bytes;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static java.nio.file.StandardOpenOption.READ;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 *
 * @author Philippe Charles
 */
@lombok.Builder
public final class BytesPrinter {

    @NonNegative
    private int index;

    @NonNegative
    private int length;

    @lombok.NonNull
    private int[] groupSizes;

    @lombok.NonNull
    private final PrintStream stream;

    public BytesPrinter print(Path file) throws IOException {
        Bytes bytes = Bytes.allocate(length, ByteOrder.nativeOrder());
        try (SeekableByteChannel sbc = Files.newByteChannel(file, READ)) {
            bytes.fill(sbc, index);
        }
        return print(bytes);
    }

    public BytesPrinter print(BytesReader bytes) {
        int[] stackSizes = groupSizes.clone();
        for (int i = 1; i < stackSizes.length; i++) {
            stackSizes[i] += stackSizes[i - 1];
        }
        int groupIndex = 0;
        for (int i = 0; i < length; i++) {
            stream.print(String.format(Locale.ROOT, "%02X ", bytes.getByte(index + i)));

            int groupSize = stackSizes[groupIndex % groupSizes.length];

            if (i % groupSize == groupSize - 1) {
                if (groupIndex % groupSizes.length == groupSizes.length - 1) {
                    stream.println("");
                } else {
                    stream.print("| ");
                }
                groupIndex++;
            }
        }
        stream.println();
        return this;
    }
}
