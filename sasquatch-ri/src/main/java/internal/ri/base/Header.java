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
package internal.ri.base;

import internal.bytes.Bytes;
import internal.bytes.BytesReader;
import internal.bytes.PValue;
import internal.bytes.Seq;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.READ;
import java.time.LocalDateTime;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import sasquatch.SasFileTypeDetector;

/**
 * Header
 *
 * @see
 * https://github.com/BioStatMatt/sas7bdat/blob/master/vignettes/sas7bdat.rst#sas7bdat-header
 * @author Philippe Charles
 */
@lombok.Value
public final class Header {

    /**
     * Unix 64 bit
     */
    private boolean u64;

    /**
     * Endianness
     */
    @NonNull
    private ByteOrder endianness;

    /**
     * OS type
     */
    @NonNull
    private PValue<Platform, Character> platform;

    /**
     * Character Encoding
     */
    @NonNull
    private PValue<Encoding, Short> encoding;

    /**
     * Creation time
     */
    @NonNull
    private LocalDateTime creationTime;

    /**
     * Last modification time
     */
    @NonNull
    private LocalDateTime lastModificationTime;

    /**
     * Header length (in bytes)
     */
    @XRef(var = "HL")
    @NonNegative
    private int length;

    /**
     * Page size (in bytes)
     */
    @XRef(var = "PL")
    @NonNegative
    private int pageLength;

    /**
     * Page count (in bytes)
     */
    @XRef(var = "PC")
    @NonNegative
    private int pageCount;

    /**
     * SAS release
     */
    @NonNull
    private String sasRelease;

    /**
     * SAS host
     */
    @NonNull
    private String sasHost;

    /**
     * OS version number
     */
    @NonNull
    private String osVersion;

    /**
     * OS maker or version
     */
    @NonNull
    private String osMaker;

    /**
     * OS name
     */
    @NonNull
    private String osName;

    /**
     * Dataset name
     */
    @NonNull
    private String name;

    /**
     * File type
     */
    @NonNull
    private String fileType;

    @NonNull
    public static Header parse(@NonNull Path file) throws IOException {
        try (SeekableByteChannel sbc = Files.newByteChannel(file, READ)) {
            return Header.parse(sbc);
        }
    }

    @NonNull
    public static Header parse(@NonNull SeekableByteChannel sbc) throws IOException {
        checkFileSize(sbc);

        Bytes file = Bytes.allocate(MIN_LENGTH, ByteOrder.nativeOrder());
        file.fill(sbc, 0);

        checkMagicNumber(file);

        return parse(file);
    }

    private static Header parse(BytesReader file) throws IOException {
        boolean u64 = file.getByte(32) == 0x33;

        int align1 = file.getByte(35) == 0x33 ? 4 : 0;

        // byte order matters already!
        ByteOrder endianness = readEndianness(file.getByte(37));
        file = file.duplicate(endianness);

        char platform = file.getString(39, 1, US_ASCII).charAt(0);
        short encoding = file.getUInt8(70);

        String name = file.getString(92, 64, US_ASCII);
        String fileType = file.getString(156, 8, US_ASCII);

        double creationTime = file.getFloat64(164 + align1);
        double lastModificationTime = file.getFloat64(172 + align1);

        int length = file.getInt32(196 + align1);

        int pageLength = file.getInt32(200 + align1);
        int pageCount = Seq.getU4U8(file, 204 + align1, u64);

        int align2 = u64 ? 4 : 0;

        String sasRelease = file.getString(216 + align1 + align2, 8, US_ASCII);
        String sasHost = file.getString(224 + align1 + align2, 8, US_ASCII);
        String osVersion = file.getString(240 + align1 + align2, 16, US_ASCII);
        String osMaker = file.getString(256 + align1 + align2, 16, US_ASCII);
        String osName = file.getString(272 + align1 + align2, 16, US_ASCII);

        return new Header(u64, endianness, Platform.tryParse(platform), Encoding.tryParse(encoding),
                SasCalendar.getDateTime(creationTime), SasCalendar.getDateTime(lastModificationTime),
                length, pageLength, pageCount,
                sasRelease, sasHost, osVersion, osMaker, osName, name, fileType);
    }

    private static void checkFileSize(SeekableByteChannel sbc) throws IOException {
        if (sbc.size() < MIN_LENGTH) {
            throw new IOException("Header too short: expected=" + MIN_LENGTH + ", actual=" + sbc.size());
        }
    }

    private static void checkMagicNumber(BytesReader file) throws IOException {
        if (!SasFileTypeDetector.isSasId(file.getBytes(0, 32))) {
            throw new IOException("Magic number mismatch");
        }
    }

    private static ByteOrder readEndianness(byte value) throws IOException {
        switch (value) {
            case 0x00:
                return ByteOrder.BIG_ENDIAN;
            case 0x01:
                return ByteOrder.LITTLE_ENDIAN;
            default:
                throw new IOException(String.format("Unknown endianness: %02X ", value));
        }
    }

    private static final int MIN_LENGTH = 296;
}
