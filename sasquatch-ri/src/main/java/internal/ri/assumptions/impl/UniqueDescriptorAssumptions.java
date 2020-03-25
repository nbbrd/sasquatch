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
import internal.ri.data.DescriptorType;
import java.nio.channels.SeekableByteChannel;
import java.util.Collection;
import java.util.EnumSet;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public enum UniqueDescriptorAssumptions implements SasFileAssumption {

    ROW_SIZE_UNIQUE_AT_0(DescriptorType.ROW_SIZE, 0),
    COL_SIZE_UNIQUE_AT_1(DescriptorType.COL_SIZE, 1),
    SUBH_CNT_UNIQUE_AT_2(DescriptorType.SUBH_CNT, 2);

    private final DescriptorType type;
    private final int index;

    @Override
    public String test(SeekableByteChannel file, SasFileStructure structure) {
        long actual = Util.indexesOf(structure.getDescriptorTypes(), type).count();
        if (actual != 1) {
            return type + " count expected:1, actual:" + actual;
        }
        if (!structure.getDescriptorTypes().get(index).isKnownAs(type)) {
            return type + " expected at index " + index;
        }
        return null;
    }

    @Override
    public String getName() {
        return name();
    }

    @ServiceProvider
    public static final class Provider implements SasFileAssumption.Provider {

        @lombok.Getter
        private final Collection<? extends SasFileAssumption> assumptions = EnumSet.allOf(UniqueDescriptorAssumptions.class);
    }
}
