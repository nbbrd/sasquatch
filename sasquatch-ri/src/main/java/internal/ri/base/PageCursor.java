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
import internal.bytes.SeekableCursor;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import lombok.AccessLevel;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PageCursor implements SeekableCursor {

    @NonNull
    public static PageCursor of(@NonNull SeekableByteChannel sbc, @NonNull Header header) {
        return new PageCursor(
                sbc,
                header.getLength(),
                header.getPageLength(),
                header.getPageCount(),
                allocatePageBytes(header),
                INITIAL_INDEX
        );
    }

    private static Bytes allocatePageBytes(Header header) {
        return Bytes.allocate(header.getPageLength(), header.getEndianness());
    }

    @lombok.NonNull
    private final SeekableByteChannel sbc;

    @NonNegative
    private final int headerLength;

    @NonNegative
    private final int pageLength;

    @lombok.Getter
    private final int count;

    @lombok.Getter
    private final Bytes bytes;

    @lombok.Getter
    private int index;

    @Override
    public void moveTo(int index) throws IOException {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException();
        }
        this.index = index;
        bytes.fill(sbc, getPagePosition());
    }

    private int getPagePosition() {
        return headerLength + pageLength * index;
    }

    @NonNull
    public static BytesReader getBytes(@NonNull SeekableByteChannel file, @NonNull Header header, @NonNegative int pageIndex) throws IOException {
        PageCursor cursor = PageCursor.of(file, header);
        cursor.moveTo(pageIndex);
        return cursor.getBytes();
    }
}
