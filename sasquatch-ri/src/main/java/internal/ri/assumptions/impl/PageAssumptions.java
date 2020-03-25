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

import internal.ri.assumptions.SasFileAssumption;
import internal.ri.assumptions.SasFileStructure;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.Collection;
import java.util.EnumSet;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
public enum PageAssumptions implements SasFileAssumption {

    FIRST_PAGE_META {
        @Override
        public String test(SeekableByteChannel file, SasFileStructure structure) throws IOException {
            return !structure.getPages().get(0).getType().get().hasMeta()
                    ? "First page always meta"
                    : null;
        }
    },
    FILE_SIZE {
        @Override
        public String test(SeekableByteChannel file, SasFileStructure structure) throws IOException {
            long expectedSize = structure.getHeader().getLength() + structure.getHeader().getPageCount() * structure.getHeader().getPageLength();
            return file.size() != expectedSize
                    ? "Header + pages should be equal to file"
                    : null;
        }
    };

    @Override
    public String getName() {
        return name();
    }

    @ServiceProvider
    public static final class Provider implements SasFileAssumption.Provider {

        @lombok.Getter
        private final Collection<? extends SasFileAssumption> assumptions = EnumSet.allOf(PageAssumptions.class);
    }
}
