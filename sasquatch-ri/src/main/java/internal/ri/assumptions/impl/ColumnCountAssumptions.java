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
import internal.ri.base.SubHeaderPointer;
import internal.ri.data.ColAttrs;
import internal.ri.data.ColNames;
import internal.ri.data.ColSize;
import internal.ri.data.DescriptorType;
import nbbrd.service.ServiceProvider;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.Collection;
import java.util.EnumSet;
import java.util.PrimitiveIterator;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public enum ColumnCountAssumptions implements SasFileAssumption {

    COL_NAMES_COUNT(DescriptorType.COL_NAMES) {
        @Override
        int countActual(SeekableByteChannel file, SasFileStructure structure) throws IOException {
            int result = 0;
            for (PrimitiveIterator.OfInt iter = Util.indexesOf(structure.getDescriptorTypes(), DescriptorType.COL_NAMES).iterator(); iter.hasNext(); ) {
                ColNames x = (ColNames) DescriptorType.COL_NAMES.parse(file, structure.getHeader(), structure.getPointers().get(iter.next()));
                result += x.getItems().size();
            }
            return result;
        }
    },
    COL_ATTRS_COUNT(DescriptorType.COL_ATTRS) {
        @Override
        int countActual(SeekableByteChannel file, SasFileStructure structure) throws IOException {
            int result = 0;
            for (PrimitiveIterator.OfInt iter = Util.indexesOf(structure.getDescriptorTypes(), DescriptorType.COL_ATTRS).iterator(); iter.hasNext(); ) {
                ColAttrs x = (ColAttrs) DescriptorType.COL_ATTRS.parse(file, structure.getHeader(), structure.getPointers().get(iter.next()));
                result += x.getItems().size();
            }
            return result;
        }
    },
    COL_LABS_COUNT(DescriptorType.COL_LABS) {
        @Override
        int countActual(SeekableByteChannel file, SasFileStructure structure) throws IOException {
            return (int) Util.indexesOf(structure.getDescriptorTypes(), DescriptorType.COL_LABS).count();
        }
    };

    private final DescriptorType type;

    abstract int countActual(SeekableByteChannel file, SasFileStructure structure) throws IOException;

    @Override
    public String test(SeekableByteChannel file, SasFileStructure structure) throws IOException {
        int expected = getColCount(file, structure);
        long actual = countActual(file, structure);
        if (actual != expected) {
            return type + " count expected:" + expected + ", actual:" + actual;
        }
        return null;
    }

    @Override
    public String getName() {
        return name();
    }

    private int getColCount(SeekableByteChannel file, SasFileStructure structure) throws IOException {
        if (structure.getPointers().size() < 2) {
            throw new IOException("Expecting structure pointer size >= 2");
        }
        SubHeaderPointer colSizePointer = structure.getPointers().get(1);
        return ((ColSize) DescriptorType.COL_SIZE.parse(file, structure.getHeader(), colSizePointer)).getCount();
    }

    @ServiceProvider
    public static final class Provider implements SasFileAssumption.Provider {

        @lombok.Getter
        private final Collection<? extends SasFileAssumption> assumptions = EnumSet.allOf(ColumnCountAssumptions.class);
    }
}
