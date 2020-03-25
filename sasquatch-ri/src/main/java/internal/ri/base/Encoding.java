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
package internal.ri.base;

import internal.bytes.PValue;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
public class Encoding {

    public static final Encoding DEFAULT = new Encoding(0x00, "WINDOWS-1252");
    private static final Encoding[] ENCODINGS = initEncodings();

    @NonNull
    public static PValue<Encoding, Short> tryParse(short uint8) {
        Encoding result = ENCODINGS[uint8];
        return result != null ? PValue.known(result) : PValue.unknown(uint8);
    }

    @NonNegative
    private int code;

    @lombok.NonNull
    private String charsetName;

    @Override
    public String toString() {
        return charsetName;
    }

    @NonNull
    public Charset getCharset() throws IOException {
        try {
            return Charset.forName(charsetName);
        } catch (UnsupportedCharsetException ex) {
            throw new IOException(ex);
        }
    }

    private static Encoding[] initEncodings() {
        Encoding[] result = new Encoding[256];
        result[0x00] = DEFAULT;
        result[0x14] = new Encoding(0x14, "UTF-8");
        result[0x1C] = new Encoding(0x1C, "US-ASCII");
        result[0x1D] = new Encoding(0x1D, "ISO-8859-1");
        result[0x1E] = new Encoding(0x1E, "ISO-8859-2");
        result[0x1F] = new Encoding(0x1F, "ISO-8859-3");
        result[0x20] = new Encoding(0x20, "ISO-8859-4");
        result[0x21] = new Encoding(0x21, "ISO-8859-5");
        result[0x22] = new Encoding(0x22, "ISO-8859-6");
        result[0x23] = new Encoding(0x23, "ISO-8859-7");
        result[0x24] = new Encoding(0x24, "ISO-8859-8");
        result[0x25] = new Encoding(0x25, "ISO-8859-9");
        result[0x27] = new Encoding(0x27, "x-iso-8859-11");
        result[0x28] = new Encoding(0x28, "ISO-8859-15");
        result[0x2B] = new Encoding(0x2B, "IBM437");
        result[0x2C] = new Encoding(0x2C, "IBM850");
        result[0x2D] = new Encoding(0x2D, "IBM852");
        result[0x2E] = new Encoding(0x2E, "IBM00858");
        result[0x2F] = new Encoding(0x2F, "IBM862");
        result[0x33] = new Encoding(0x33, "IBM866");
        result[0x3A] = new Encoding(0x3A, "IBM857");
        result[0x3C] = new Encoding(0x3C, "windows-1250");
        result[0x3D] = new Encoding(0x3D, "windows-1251");
        result[0x3E] = new Encoding(0x3E, "windows-1252");
        result[0x3F] = new Encoding(0x3F, "windows-1253");
        result[0x40] = new Encoding(0x40, "windows-1254");
        result[0x41] = new Encoding(0x41, "windows-1255");
        result[0x42] = new Encoding(0x42, "windows-1256");
        result[0x43] = new Encoding(0x43, "windows-1257");
        result[0x44] = new Encoding(0x44, "windows-1258");
        result[0x45] = new Encoding(0x45, "x-MacRoman");
        result[0x46] = new Encoding(0x46, "x-MacArabic");
        result[0x47] = new Encoding(0x47, "x-MacHebrew");
        result[0x48] = new Encoding(0x48, "x-MacGreek");
        result[0x49] = new Encoding(0x49, "x-MacThai");
        result[0x4B] = new Encoding(0x4B, "x-MacTurkish");
        result[0x4C] = new Encoding(0x4C, "x-MacUkraine");
        result[0x4E] = new Encoding(0x4E, "IBM037");
        result[0x57] = new Encoding(0x57, "IBM424");
        result[0x58] = new Encoding(0x58, "IBM500");
        result[0x59] = new Encoding(0x59, "IBM-Thai");
        result[0x5A] = new Encoding(0x5A, "IBM870");
        result[0x5B] = new Encoding(0x5B, "x-IBM875");
        result[0x5F] = new Encoding(0x5F, "x-IBM1025");
        result[0x62] = new Encoding(0x62, "x-IBM1112");
        result[0x63] = new Encoding(0x63, "x-IBM1122");
        result[0x66] = new Encoding(0x66, "IBM424");
        result[0x67] = new Encoding(0x67, "IBM-Thai");
        result[0x68] = new Encoding(0x68, "IBM870");
        result[0x69] = new Encoding(0x69, "x-IBM875");
        result[0x6C] = new Encoding(0x6C, "x-IBM1025");
        result[0x6D] = new Encoding(0x6D, "IBM1026");
        result[0x6E] = new Encoding(0x6E, "IBM1047");
        result[0x6F] = new Encoding(0x6F, "x-IBM1112");
        result[0x70] = new Encoding(0x70, "x-IBM1122");
        result[0x75] = new Encoding(0x75, "x-IBM937");
        result[0x76] = new Encoding(0x76, "x-windows-950");
        result[0x77] = new Encoding(0x77, "x-EUC-TW");
        result[0x7B] = new Encoding(0x7B, "Big5");
        result[0x7C] = new Encoding(0x7C, "x-IBM935");
        result[0x7D] = new Encoding(0x7D, "GBK");
        result[0x7E] = new Encoding(0x7E, "x-mswin-936");
        result[0x80] = new Encoding(0x80, "x-IBM1381");
        result[0x81] = new Encoding(0x81, "x-IBM939");
        result[0x82] = new Encoding(0x82, "x-IBM930");
        result[0x86] = new Encoding(0x86, "EUC-JP");
        result[0x88] = new Encoding(0x88, "x-windows-iso2022jp");
        result[0x89] = new Encoding(0x89, "x-IBM942");
        result[0x8A] = new Encoding(0x8A, "Shift_JIS");
        result[0x8B] = new Encoding(0x8B, "x-IBM933");
        result[0x8C] = new Encoding(0x8C, "EUC-KR");
        result[0x8D] = new Encoding(0x8D, "x-windows-949");
        result[0x8E] = new Encoding(0x8E, "x-IBM949");
        result[0xA3] = new Encoding(0xA3, "x-MacIceland");
        result[0xA7] = new Encoding(0xA7, "ISO-2022-JP");
        result[0xA8] = new Encoding(0xA8, "ISO-2022-KR");
        result[0xA9] = new Encoding(0xA9, "x-ISO2022-CN-GB");
        result[0xAC] = new Encoding(0xAC, "x-ISO2022-CN-CNS");
        result[0xAD] = new Encoding(0xAD, "IBM037");
        result[0xB7] = new Encoding(0xB7, "IBM01140");
        result[0xB8] = new Encoding(0xB8, "IBM01141");
        result[0xB9] = new Encoding(0xB9, "IBM01142");
        result[0xBA] = new Encoding(0xBA, "IBM01143");
        result[0xBB] = new Encoding(0xBB, "IBM01144");
        result[0xBC] = new Encoding(0xBC, "IBM01145");
        result[0xBD] = new Encoding(0xBD, "IBM01146");
        result[0xBE] = new Encoding(0xBE, "IBM01147");
        result[0xBF] = new Encoding(0xBF, "IBM01148");
        result[0xC0] = new Encoding(0xC0, "IBM01140");
        result[0xC1] = new Encoding(0xC1, "IBM01141");
        result[0xC2] = new Encoding(0xC2, "IBM01142");
        result[0xC3] = new Encoding(0xC3, "IBM01143");
        result[0xC4] = new Encoding(0xC4, "IBM01144");
        result[0xC5] = new Encoding(0xC5, "IBM01145");
        result[0xC6] = new Encoding(0xC6, "IBM01146");
        result[0xC7] = new Encoding(0xC7, "IBM01147");
        result[0xC8] = new Encoding(0xC8, "IBM01148");
        result[0xCD] = new Encoding(0xCD, "GB18030");
        result[0xCF] = new Encoding(0xCF, "x-IBM1097");
        result[0xD0] = new Encoding(0xD0, "x-IBM1097");
        result[0xD3] = new Encoding(0xD3, "IBM01149");
        result[0xD4] = new Encoding(0xD4, "IBM01149");
        result[0xEA] = new Encoding(0xEA, "x-IBM930");
        result[0xEB] = new Encoding(0xEB, "x-IBM933");
        result[0xEC] = new Encoding(0xEC, "x-IBM935");
        result[0xED] = new Encoding(0xED, "x-IBM937");
        result[0xEE] = new Encoding(0xEE, "x-IBM939");
        result[0xF2] = new Encoding(0xF2, "ISO-8859-13");
        result[0xF5] = new Encoding(0xF5, "x-MacCroatian");
        result[0xF6] = new Encoding(0xF6, "x-MacCyrillic");
        result[0xF7] = new Encoding(0xF7, "x-MacRomania");
        result[0xF8] = new Encoding(0xF8, "JIS_X0201");
        return result;
    }
}
