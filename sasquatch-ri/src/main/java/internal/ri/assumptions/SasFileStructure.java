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
package internal.ri.assumptions;

import internal.bytes.BytesReader;
import internal.bytes.PValue;
import internal.ri.base.Header;
import internal.ri.base.PageHeader;
import internal.ri.base.SasFile;
import internal.ri.base.SasFileVisitor;
import internal.ri.base.SubHeaderPointer;
import internal.ri.data.DescriptorType;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileVisitResult;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class SasFileStructure {

    @lombok.NonNull
    private Header header;

    @lombok.Singular
    private List<PageHeader> pages;

    @lombok.Singular
    private List<SubHeaderPointer> pointers;

    @lombok.Singular
    private List<PValue<DescriptorType, String>> descriptorTypes;

    public static SasFileStructure of(SeekableByteChannel file) throws IOException {
        SasFileStructure.Builder builder = SasFileStructure.builder();
        SasFile.visit(file, new SasFileVisitor() {
            @Override
            public FileVisitResult onHeader(Header header) {
                builder.header(header);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult onPage(Header header, PageHeader page, BytesReader pageData) {
                builder.page(page);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult onSubHeaderPointer(Header header, PageHeader page, BytesReader pageData, SubHeaderPointer pointer) {
                builder.pointer(pointer);
                builder.descriptorType(DescriptorType.tryParse(pageData, header.isU64(), pointer).orElse(NO_DESCRIPTOR_NULL));
                return FileVisitResult.CONTINUE;
            }
        });
        return builder.build();
    }

    private static final PValue<DescriptorType, String> NO_DESCRIPTOR_NULL = PValue.unknown("null");
}
