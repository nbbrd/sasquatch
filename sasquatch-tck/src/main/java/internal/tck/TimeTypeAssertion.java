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
package internal.tck;

import java.io.IOException;
import java.time.LocalTime;
import java.util.stream.Stream;
import nbbrd.service.ServiceProvider;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Index;
import sasquatch.samples.SasResources;
import sasquatch.spi.SasFeature;
import sasquatch.spi.SasReader;
import sasquatch.tck.AbstractFeatureAssertion;
import sasquatch.tck.SasFeatureAssertion;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(SasFeatureAssertion.class)
public final class TimeTypeAssertion extends AbstractFeatureAssertion {

    public TimeTypeAssertion() {
        super(SasFeature.TIME_TYPE, SasResources.PPHAM27.getRoot().resolve("marchflights.sas7bdat"));
    }

    @Override
    protected void assertSuccess(SoftAssertions s, SasReader reader) throws IOException {
        try (Stream<LocalTime> stream = rows(reader, o -> o.getTime(2))) {
            s.assertThat(stream)
                    .contains(LocalTime.of(8, 21, 0), Index.atIndex(0))
                    .contains(LocalTime.of(21, 6, 0), Index.atIndex(634))
                    .hasSize(635);
        }
    }

    @Override
    protected void assertFealure(SoftAssertions s, SasReader reader) throws IOException {
        s.assertThatThrownBy(() -> toList(reader, o -> o.getTime(2)))
                .describedAs("Excepting feature '%s' to raise IllegalArgumentException or IOException on '%s'", getFeature(), getFile())
                .isInstanceOfAny(IllegalArgumentException.class, IOException.class);
    }
}
