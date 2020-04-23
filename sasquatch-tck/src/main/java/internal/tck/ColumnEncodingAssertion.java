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
import sasquatch.SasRow;
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
public final class ColumnEncodingAssertion extends AbstractFeatureAssertion {

    public ColumnEncodingAssertion() {
        super(SasFeature.COLUMN_ENCODING, SasResources.EPAM.getRoot().resolve("chinese_column_works.sas7bdat"));
    }

    @Override
    protected void assertSuccess(SoftAssertions s, SasReader reader) throws IOException {
        s.assertThat(reader.readMetaData(getFile()).getColumns().get(0).getName())
                .isEqualTo("测试");
    }

    @Override
    protected <T> void assertFealure(SoftAssertions s, SasReader reader, SasRow.Factory<T> mapper) throws IOException {
        try {
            s.assertThat(reader.readMetaData(getFile()).getColumns().get(0).getName())
                    .isNotEqualTo("测试");
        } catch (IOException | UncheckedIOException ex) {
            // can throw exception instead
        }
    }
}
