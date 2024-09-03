/*
 * Copyright 2017 National Bank of Belgium
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
package internal.samples;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import internal.samples.DumbmatterContent.StatTransfer13NumberFormat;

/**
 *
 * @author Philippe Charles
 */
public class DumbmatterContentTest {

    @Test
    public void testStatTransfer13NumberFormat() {

        assertThat(StatTransfer13NumberFormat.getDigitShift("15.49899959564209")).isEqualTo(2);
        assertThat(StatTransfer13NumberFormat.getDigitShift("15.4989995956421")).isEqualTo(2);
        assertThat(StatTransfer13NumberFormat.getDigitShift("0.479999989271164")).isEqualTo(0);
        assertThat(StatTransfer13NumberFormat.getDigitShift("0.00499999988824129")).isEqualTo(-2);

        assertThat(StatTransfer13NumberFormat.INSTANCE.apply(Double.NaN)).isEqualTo("");
        assertThat(StatTransfer13NumberFormat.INSTANCE.apply(15.49899959564209)).isEqualTo("15.4989995956421");
        assertThat(StatTransfer13NumberFormat.INSTANCE.apply(0.479999989271164)).isEqualTo("0.479999989271164");
        assertThat(StatTransfer13NumberFormat.INSTANCE.apply(0.00499999988824129)).isEqualTo("0.00499999988824129");
        assertThat(StatTransfer13NumberFormat.INSTANCE.apply(0.000025)).isEqualTo("2.5e-05");
        assertThat(StatTransfer13NumberFormat.INSTANCE.apply(-1.889787108563402e-06)).isEqualTo("-1.8897871085634e-06");
        assertThat(StatTransfer13NumberFormat.INSTANCE.apply(-0.0999999999999996)).isEqualTo("-0.0999999999999996");
    }
}
