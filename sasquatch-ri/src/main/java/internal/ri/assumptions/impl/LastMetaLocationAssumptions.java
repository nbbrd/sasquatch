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
import internal.bytes.PValue;
import internal.ri.assumptions.SasFileAssumption;
import internal.ri.assumptions.SasFileStructure;
import internal.ri.base.PageCursor;
import internal.ri.base.SubHeaderFormat;
import internal.ri.base.SubHeaderLocation;
import internal.ri.base.SubHeaderPointer;
import internal.ri.data.DescriptorType;
import internal.ri.data.RowSize;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public enum LastMetaLocationAssumptions implements SasFileAssumption {

    LAST_META_LOCATION;

    @Override
    public String test(SeekableByteChannel file, SasFileStructure structure) throws IOException {
        RowSize rowSize = getRowSize(file, structure);

        SubHeaderLocation lastMeta = rowSize.getLastMeta();

        BytesReader pageBytes = PageCursor.getBytes(file, structure.getHeader(), lastMeta.getPage());
        boolean u64 = structure.getHeader().isU64();

        SubHeaderPointer lastMetaPointer = SubHeaderPointer.parse(pageBytes, u64, lastMeta);
        Optional<PValue<DescriptorType, String>> lastMetaType = DescriptorType.tryParse(pageBytes, u64, lastMetaPointer);

        if (!lastMetaType.isPresent()) {
            return "Expected last subheader type to be present";
        }

        if (lastMetaType.get().isUnknown()) {
            return "Expected last subheader type to be known";
        }

        SubHeaderLocation nextLocation = lastMeta.next();
        SubHeaderPointer nextPointer = SubHeaderPointer.parse(pageBytes, u64, nextLocation);
        Optional<PValue<DescriptorType, String>> nextType = DescriptorType.tryParse(pageBytes, u64, nextPointer);

        if (nextType.isPresent() && nextType.get().isKnown()) {
            return "Expected next subheader type to not be known";
        }

        if (Util.isCompressed(file) && !isRowOfCompressedFile(nextPointer, rowSize)) {
            return "Expected next subheader to be a data row";
        }

        return null;
    }

    private static boolean isRowOfCompressedFile(SubHeaderPointer nextPointer, RowSize rowSize) {
        return nextPointer.getFormat().isKnownAs(SubHeaderFormat.COMPRESSED)
                || (nextPointer.getFormat().isKnownAs(SubHeaderFormat.PLAIN) && nextPointer.getLength() == rowSize.getLength());
    }

    private RowSize getRowSize(SeekableByteChannel file, SasFileStructure structure) throws IOException {
        return (RowSize) DescriptorType.ROW_SIZE.parse(file, structure.getHeader(), structure.getPointers().get(0));
    }

    @Override
    public String getName() {
        return name();
    }

    @ServiceProvider
    public static final class Provider implements SasFileAssumption.Provider {

        @lombok.Getter
        private final Collection<? extends SasFileAssumption> assumptions = EnumSet.allOf(LastMetaLocationAssumptions.class);
    }
}
