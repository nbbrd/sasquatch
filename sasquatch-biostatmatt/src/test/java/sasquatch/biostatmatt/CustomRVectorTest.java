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
package sasquatch.biostatmatt;

import org.junit.jupiter.api.Test;
import sasquatch.biostatmatt.RUtils.CustomRVector;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIndexOutOfBoundsException;

/**
 * @author Philippe Charles
 */
public class CustomRVectorTest {

    private static final double D1 = 1, D2 = 2, D3 = 3;

    private static CustomRVector wrap(double... values) {
        CustomRVector result = new CustomRVector(RUtils.DataType.NUMERIC, values.length);
        for (int i = 0; i < values.length; i++) {
            result.set(i + 1, values[i]);
        }
        return result;
    }

    private static void assertEquals2(double expected, Object found) {
        assertThat(((Double) found)).isEqualTo(expected);
    }

    @Test
    public void testGetLength() {
        assertThat(wrap().getLength()).isEqualTo(0);
        assertThat(wrap(D1, D2, D3).getLength()).isEqualTo(3);

        CustomRVector vector = new CustomRVector(RUtils.DataType.NUMERIC, 100);
        assertThat(vector.getLength()).isEqualTo(100);
        assertThat(vector.data.size()).isEqualTo(0);
        vector.set(3, D1);
        assertThat(vector.data.size()).isEqualTo(3);
    }

    @Test
    public void testGet() {
        CustomRVector vector = wrap(D1, D2);
        assertEquals2(D1, vector.get(1));
        assertEquals2(D2, vector.get(2));
    }

    @Test
    public void testGetWithLowerBounds() {
        assertThatIndexOutOfBoundsException()
                .isThrownBy(() -> wrap(D1, D2).get(0));
    }

    @Test
    public void testGetWithUpperBounds() {
        assertThatIndexOutOfBoundsException()
                .isThrownBy(() -> wrap(D1, D2).get(3));
    }

    @Test
    public void testSet() {
        CustomRVector vector = wrap(D1, D2);
        vector.set(1, D3);
        assertEquals2(D3, vector.get(1));
        assertEquals2(D2, vector.get(2));
    }

    @Test
    public void testSetWithLowerBounds() {
        assertThatIndexOutOfBoundsException()
                .isThrownBy(() -> wrap(D1, D2).set(0, D3));
    }

    @Test
    public void testSetWithUpperBounds() {
        assertThatIndexOutOfBoundsException()
                .isThrownBy(() -> wrap(D1, D2).set(3, D3));
    }

    @Test
    public void testIterator() {
        Iterator<Object> iterator = wrap(D1, D2).iterator();
        assertThat(iterator.hasNext()).isTrue();
        assertEquals2(D1, iterator.next());
        assertThat(iterator.hasNext()).isTrue();
        assertEquals2(D2, iterator.next());
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    public void testEquals() {
        assertThat(wrap(D1, D2)).isEqualTo(wrap(D1, D2));
        assertThat(wrap(D1, D2)).isNotSameAs(wrap(D1, D2));
        assertThat(wrap(D1)).isNotEqualTo(wrap(D1, D2));
        assertThat(wrap(D1, D3)).isNotEqualTo(wrap(D1, D2));
    }
}
