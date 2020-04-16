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
import java.io.UncheckedIOException;
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
public final class CustomNumericAssertion extends AbstractFeatureAssertion {

    public CustomNumericAssertion() {
        super(SasFeature.CUSTOM_NUMERIC, SasResources.PPHAM27.getRoot().resolve("br.sas7bdat"));
    }

    @Override
    protected void assertSuccess(SoftAssertions s, SasReader reader) throws IOException {
        try (Stream<Number> stream = rows(reader, o -> o.getNumber(0))) {
            s.assertThat(stream)
                    .contains(66500d, Index.atIndex(0))
                    .contains(374000d, Index.atIndex(1079))
                    .hasSize(1080);
        }
    }

    @Override
    protected void assertFealure(SoftAssertions s, SasReader reader) throws IOException {
        try {
            try (Stream<Number> stream = rows(reader, o -> o.getNumber(0))) {
                s.assertThat(stream)
                        .doesNotContain(374000d, Index.atIndex(1079))
                        .hasSize(1080);
            }
        } catch (IOException | UncheckedIOException ex) {
            // can throw exception instead
        }
    }
}
