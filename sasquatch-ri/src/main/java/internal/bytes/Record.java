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
package internal.bytes;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Record {

    public static <X> List<X> toList(int count, int offset, int length, IntFunction<X> factory) {
        return toList(count, offset, length, (i, base) -> factory.apply(base));
    }

    public static <X> List<X> toList(int count, int offset, int length, BiIntFunction<X> factory) {
        return IntStream.range(0, count)
                .mapToObj(i -> factory.apply(i, getBase(offset, length, i)))
                .collect(Collectors.toList());
    }

    public static int getBase(int offset, int length, int index) {
        return offset + length * index;
    }

    public interface BiIntFunction<X> {

        X apply(int i, int base);
    }
}
