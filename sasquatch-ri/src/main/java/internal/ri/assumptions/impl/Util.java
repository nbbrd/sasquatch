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

import internal.bytes.PValue;
import internal.ri.data.Compression;
import internal.ri.data.DescriptorType;
import internal.ri.data.Document;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class Util {

    <T> boolean isSorted(List<T> list, Comparator<T> comparator) {
        return list.stream().sorted(comparator).collect(Collectors.toList()).equals(list);
    }

    IntStream indexesOf(List<PValue<DescriptorType, String>> list, DescriptorType type) {
        return IntStream.range(0, list.size())
                .filter(i -> list.get(i).isKnownAs(type));
    }

    boolean isCompressed(SeekableByteChannel file) throws IOException {
        return isCompressed(Document.parse(file));
    }

    boolean isCompressed(Document doc) {
        return doc.getCompression().isKnownAs(Compression.CHAR)
                || doc.getCompression().isKnownAs(Compression.BIN);
    }
}
