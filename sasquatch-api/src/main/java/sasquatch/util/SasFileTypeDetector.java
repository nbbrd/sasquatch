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
package sasquatch.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.spi.FileTypeDetector;
import java.util.Arrays;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(FileTypeDetector.class)
public final class SasFileTypeDetector extends FileTypeDetector {

    public static final String MIME_TYPE = "application/x-sas-data";

    @Override
    public String probeContentType(Path file) throws IOException {
        return hasSasId(file) ? MIME_TYPE : null;
    }

    /**
     * Checks if a SAS dataset contains the required identifier.
     * <p>
     * A SAS dataset file always contains a binary file identifier in its
     * header. This id is a 32 byte sequence at offset 0 with fixed values.
     * <br>Its presence means that the file is a real SAS dataset while its
     * absence means the opposite.
     *
     * @param file the SAS dataset to check
     * @return true if the file contains the right identifier
     * @throws IOException if an I/O exception occurred
     */
    public static boolean hasSasId(@NonNull Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file, StandardOpenOption.READ)) {
            byte[] buffer = new byte[MAGIC_NUMBER.length];
            return stream.read(buffer) == buffer.length && isSasId(buffer);
        }
    }

    public static boolean isSasId(@NonNull byte[] bytes) {
        return Arrays.equals(MAGIC_NUMBER, bytes);
    }

    private static final byte[] MAGIC_NUMBER = new byte[]{
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xc2, (byte) 0xea, (byte) 0x81, (byte) 0x60,
        (byte) 0xb3, (byte) 0x14, (byte) 0x11, (byte) 0xcf, (byte) 0xbd, (byte) 0x92, (byte) 0x08, (byte) 0x00,
        (byte) 0x09, (byte) 0xc7, (byte) 0x31, (byte) 0x8c, (byte) 0x18, (byte) 0x1f, (byte) 0x10, (byte) 0x11};
}
