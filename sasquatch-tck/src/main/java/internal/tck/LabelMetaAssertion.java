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
import nbbrd.service.ServiceProvider;
import org.assertj.core.api.SoftAssertions;
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
public final class LabelMetaAssertion extends AbstractFeatureAssertion {

    public LabelMetaAssertion() {
        super(SasFeature.LABEL_META, SasResources.EPAM.getRoot().resolve("file_with_label.sas7bdat"));
    }

    @Override
    protected void assertSuccess(SoftAssertions s, SasReader reader) throws IOException {
        s.assertThat(reader.readMetaData(getFile()).getLabel()).isEqualTo("test");
    }

    @Override
    protected void assertFealure(SoftAssertions s, SasReader reader) throws IOException {
        try {
            s.assertThat(reader.readMetaData(getFile()).getLabel()).isNotEqualTo("test");
        } catch (IOException | UncheckedIOException ex) {
            // can throw exception instead
        }
    }
}
