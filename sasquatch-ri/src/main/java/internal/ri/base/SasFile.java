/*
 * Copyright 2017 National Bank of Belgium
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

import internal.bytes.BytesReader;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.READ;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SasFile {

    public static void visit(@NonNull Path file, @NonNull SasFileVisitor visitor) throws IOException {
        try (SeekableByteChannel sbc = Files.newByteChannel(file, READ)) {
            visit(sbc, visitor);
        }
    }

    public void visit(@NonNull SeekableByteChannel sbc, @NonNull SasFileVisitor visitor) throws IOException {
        Header header = Header.parse(sbc);
        switch (visitor.onHeader(header)) {
            case CONTINUE:
                checkHeader(header, sbc.size());
                PageCursor pageCursor = PageCursor.of(sbc, header);
                visitPages(header, pageCursor, visitor);
                break;
            case SKIP_SIBLINGS:
            case SKIP_SUBTREE:
            case TERMINATE:
                break;
        }
    }

    private void checkHeader(Header header, long size) throws IOException {
        if (header.getLength() > size) {
            throw new IOException("Header too short");
        }
        if (header.getPageLength() < 0) {
            throw new IOException("Invalid page length");
        }
        if (header.getPageCount() < 1) {
            throw new IOException("Invalid page count");
        }
    }

    private boolean visitPages(Header header, PageCursor pageCursor, SasFileVisitor visitor) throws IOException {
        while (pageCursor.next()) {
            BytesReader pageData = pageCursor.getBytes();
            PageHeader page = PageHeader.parse(pageData, header.isU64(), pageCursor.getIndex());
            switch (visitor.onPage(header, page, pageData)) {
                case CONTINUE:
                    if (page.getType().isKnown()) {
                        switch (page.getType().get()) {
                            case META:
                            case MIX:
                            case AMD:
                                if (!visitSubHeaderPointers(header, page, pageData, visitor)) {
                                    return false;
                                }
                                break;
                            case INDEX:
                                if (!visitRowCompEntry(header, page, pageData, visitor)) {
                                    return false;
                                }
                                break;
                        }
                    }
                    break;
                case SKIP_SIBLINGS:
                    return false;
                case SKIP_SUBTREE:
                    break;
                case TERMINATE:
                    return false;
            }
        }
        return true;
    }

    private boolean visitSubHeaderPointers(Header header, PageHeader page, BytesReader pageData, SasFileVisitor visitor) {
        for (int sh = 0; sh < page.getSubHeaderCount(); sh++) {
            SubHeaderPointer pointer = SubHeaderPointer.parse(pageData, header.isU64(), page.getSubHeaderLocation(sh));
            switch (visitor.onSubHeaderPointer(header, page, pageData, pointer)) {
                case CONTINUE:
                    break;
                case SKIP_SIBLINGS:
                    return true;
                case SKIP_SUBTREE:
                    break;
                case TERMINATE:
                    return false;
            }
        }
        return true;
    }

    private boolean visitRowCompEntry(Header header, PageHeader page, BytesReader pageData, SasFileVisitor visitor) {
        for (RowIndex o : RowIndex.parseAll(page, pageData, header.isU64())) {
            switch (visitor.onRowIndex(header, page, pageData, o)) {
                case CONTINUE:
                    break;
                case SKIP_SIBLINGS:
                    return true;
                case SKIP_SUBTREE:
                    break;
                case TERMINATE:
                    return false;
            }
        }
        return true;
    }
}
