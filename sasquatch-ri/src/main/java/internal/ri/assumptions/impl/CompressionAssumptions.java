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
package internal.ri.assumptions.impl;

import internal.bytes.BytesReader;
import internal.ri.assumptions.SasFileAssumption;
import internal.ri.assumptions.SasFileStructure;
import internal.ri.base.Header;
import internal.ri.base.PageHeader;
import internal.ri.base.PageType;
import internal.ri.base.RowIndex;
import internal.ri.base.SasFile;
import internal.ri.base.SasFileVisitor;
import internal.ri.data.Document;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileVisitResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
public enum CompressionAssumptions implements SasFileAssumption {

    COMPRESSION_KNOWN {
        @Override
        public String test(SeekableByteChannel file, SasFileStructure structure) throws IOException {
            Document doc = Document.parse(file);
            return !doc.getCompression().isKnown()
                    ? "All compression IDs are known"
                    : null;
        }
    },
    COMPRESSION_INDEX_UNIQUE {
        @Override
        public String test(SeekableByteChannel file, SasFileStructure structure) throws IOException {
            if (Util.isCompressed(file)) {
                long count = structure.getPages()
                        .stream()
                        .filter(page -> page.getType().isKnownAs(PageType.INDEX))
                        .count();
                // TODO: check pattern location of INDEX page
                return count != 1
                        ? "Compressed file has one page of type INDEX"
                        : null;
            }
            return null;
        }
    },
    COMPRESSION_INDEX_SORTED {
        @Override
        public String test(SeekableByteChannel file, SasFileStructure structure) throws IOException {
            if (Util.isCompressed(file)) {
                List<RowIndex> indexes = new ArrayList<>();
                SasFile.visit(file, new SasFileVisitor() {
                    @Override
                    public FileVisitResult onRowIndex(Header header, PageHeader page, BytesReader pageData, RowIndex rowIndex) {
                        indexes.add(rowIndex);
                        return FileVisitResult.CONTINUE;
                    }
                });
                return !Util.isSorted(indexes, Comparator.comparing(RowIndex::getLastRowLocation).reversed())
                        ? "isRowIndexesSorted"
                        : null;
            }
            return null;
        }
    };

    @Override
    public String getName() {
        return name();
    }

    @ServiceProvider
    public static final class Provider implements SasFileAssumption.Provider {

        @lombok.Getter
        private final Collection<? extends SasFileAssumption> assumptions = EnumSet.allOf(CompressionAssumptions.class);
    }
}
