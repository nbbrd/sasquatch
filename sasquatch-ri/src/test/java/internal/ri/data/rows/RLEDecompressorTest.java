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
package internal.ri.data.rows;

import internal.bytes.Bytes;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Philippe Charles
 */
public class RLEDecompressorTest {

    @Test
    public void testChar() {
        RLEDecompressor c = RLEDecompressor.INSTANCE;

        String inputString = "87" + "0102030405060708" + "F2" + "8A" + "0102030405060708091011" + "D0";
        String outputString = "0102030405060708" + "00000000" + "0102030405060708091011" + "4040";

        byte[] input = decode(inputString);
        byte[] output = uncompress(c, input, 25);

        assertThat(encode(output)).isEqualTo(outputString);
    }

    private static byte[] uncompress(Decompressor decompressor, byte[] input, int length) {
        byte[] result = new byte[length];
        decompressor.uncompress(Bytes.wrap(input, ByteOrder.nativeOrder()), 0, Bytes.wrap(result, ByteOrder.nativeOrder()), input.length);
        return result;
    }

    private static String encode(byte[] bytes) {
        BigInteger bigInteger = new BigInteger(1, bytes);
        return String.format(Locale.ROOT, "%0" + (bytes.length << 1) + "x", bigInteger);
    }

    private static byte[] decode(String hexString) {
        byte[] byteArray = new BigInteger(hexString, 16).toByteArray();
        if (byteArray[0] == 0) {
            byte[] output = new byte[byteArray.length - 1];
            System.arraycopy(byteArray, 1, output, 0, output.length);
            return output;
        }
        return byteArray;
    }
}
