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
package sasquatch.tck;

import sasquatch.spi.SasFeature;
import java.util.Set;
import org.assertj.core.api.SoftAssertions;
import sasquatch.spi.SasReader;

/**
 *
 * @author Philippe Charles
 */
public final class SasReaderAssert {

    public static void assertCompliance(SasReader reader) {
        SoftAssertions s = new SoftAssertions();
        try {
            assertCompliance(s, reader, reader.getFeatures());
        } catch (Exception ex) {
            s.fail("Unexpected exception of type '" + ex.getClass() + "'", ex);
        }
        s.assertAll();
    }

    private static void assertCompliance(SoftAssertions s, SasReader reader, Set<SasFeature> features) throws Exception {
        s.assertThat(reader.getName()).isNotBlank();
        s.assertThat(reader.getCost()).isGreaterThanOrEqualTo(0);
        s.assertThat(reader.getFeatures()).isNotNull();
        s.assertThatThrownBy(() -> reader.readForward(null)).isInstanceOf(NullPointerException.class);
        s.assertThatThrownBy(() -> reader.readScrollable(null)).isInstanceOf(NullPointerException.class);
        s.assertThatThrownBy(() -> reader.readMetaData(null)).isInstanceOf(NullPointerException.class);

        for (SasFeatureAssertion x : SasFeatureAssertionLoader.get()) {
            x.assertFeature(s, reader, features);
        }
    }
}
