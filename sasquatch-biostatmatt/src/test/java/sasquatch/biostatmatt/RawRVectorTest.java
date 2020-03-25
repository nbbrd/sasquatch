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

import sasquatch.biostatmatt.RUtils.RawRVector;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Iterator;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class RawRVectorTest {

    private static final byte B1 = 1, B2 = 2, B3 = 3;

    private static RawRVector wrap(ByteOrder byteOrder, byte... bytes) {
        return new RawRVector(ByteBuffer.wrap(bytes).order(byteOrder));
    }

    private static RawRVector wrap(byte... bytes) {
        return wrap(ByteOrder.LITTLE_ENDIAN, bytes);
    }

    private static void assertEquals2(byte expected, Byte found) {
        assertThat(found).isEqualTo(expected);
    }

    private static void assertState(RawRVector... list) {
        for (RawRVector vector : list) {
            assertThat(vector.byteBuffer.position()).isEqualTo(0);
            assertThat(vector.byteBuffer.limit()).isEqualTo(vector.byteBuffer.capacity());
            assertThat(vector.byteBuffer.order()).isEqualTo(ByteOrder.LITTLE_ENDIAN);
        }
    }

    @Test
    public void testGetLength() {
        assertThat(wrap().getLength()).isEqualTo(0);
        assertThat(wrap(B1, B2, B3).getLength()).isEqualTo(3);
    }

    @Test
    public void testGet() {
        RawRVector vector = wrap(B1, B2);
        assertEquals2(B1, vector.get(1));
        assertEquals2(B2, vector.get(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetWithLowerBounds() {
        wrap(B1, B2).get(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetWithUpperBounds() {
        wrap(B1, B2).get(3);
    }

    @Test
    public void testSet() {
        RawRVector vector = wrap(B1, B2);
        vector.set(1, B3);
        assertEquals2(B3, vector.get(1));
        assertEquals2(B2, vector.get(2));
        assertState(vector);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetWithLowerBounds() {
        wrap(B1, B2).set(0, B3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetWithUpperBounds() {
        wrap(B1, B2).set(3, B3);
    }

    @Test
    public void testIterator() {
        Iterator<Byte> iterator = wrap(B1, B2).iterator();
        assertThat(iterator.hasNext()).isTrue();
        assertEquals2(B1, iterator.next());
        assertThat(iterator.hasNext()).isTrue();
        assertEquals2(B2, iterator.next());
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    public void testEquals() {
        assertThat(wrap(B1, B2)).isEqualTo(wrap(B1, B2));
        assertThat(wrap(B1, B2)).isNotSameAs(wrap(B1, B2));
        assertThat(wrap(B1)).isNotEqualTo(wrap(B1, B2));
        assertThat(wrap(B1, B3)).isNotEqualTo(wrap(B1, B2));
    }

    @Test
    public void testConcat() {
        RawRVector l = wrap(B1, B2);
        RawRVector r = wrap(B3);
        RawRVector result = l.concat(r);
        assertState(l, r, result);
        assertThat(result.getLength()).isEqualTo(3);
        assertEquals2(B1, result.get(1));
        assertEquals2(B2, result.get(2));
        assertEquals2(B3, result.get(3));
        assertThat(wrap().concat(wrap()).getLength()).isEqualTo(0);
    }

    @Test
    public void testSub() {
        RawRVector source = wrap(B1, B2);
        RawRVector r1 = source.sub(1, 0);
        assertThat(r1.getLength()).isEqualTo(0);
        RawRVector r2 = source.sub(1, 1);
        assertThat(r2.getLength()).isEqualTo(1);
        assertThat(r2.get(1).byteValue()).isEqualTo(B1);
        RawRVector r3 = source.sub(2, 1);
        assertThat(r3.getLength()).isEqualTo(1);
        assertThat(r3.get(1).byteValue()).isEqualTo(B2);
        assertState(source, r1, r2, r3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubWithLowerBounds() {
        wrap(B1, B2).sub(0, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubWithUpperBounds() {
        wrap(B1, B2).sub(3, 1);
    }

    @Test
    public void testGetString() {
        Charset charset = Charset.forName("windows-1252");
        assertThat(wrap("hello".getBytes(charset)).getString(1, 5)).isEqualTo("hello");
        assertThat(wrap("hello".getBytes(charset)).getString(1, 4)).isEqualTo("hell");
    }
}
