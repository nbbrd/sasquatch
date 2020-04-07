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
import org.assertj.core.data.Index;
import sasquatch.SasColumn;
import sasquatch.SasColumnFormat;
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
public final class ColumnFormatAssertion extends AbstractFeatureAssertion {

    public ColumnFormatAssertion() {
        super(SasFeature.COLUMN_FORMAT, SasResources.EPAM.getRoot().resolve("test-columnar.sas7bdat"));
    }

    @Override
    protected void assertSuccess(SoftAssertions s, SasReader reader) throws IOException {
        s.assertThat(reader.readMetaData(getFile()).getColumns())
                .extracting(SasColumn::getFormat)
                .contains(SasColumnFormat.EMPTY, Index.atIndex(2))
                .contains(SasColumnFormat.builder().name("PERCENT").width(8).precision(0).build(), Index.atIndex(3))
                .contains(SasColumnFormat.builder().name("PERCENT").width(7).precision(1).build(), Index.atIndex(4));
    }

    @Override
    protected void assertFealure(SoftAssertions s, SasReader reader) throws IOException {
        try {
            s.assertThat(reader.readMetaData(getFile()).getColumns())
                    .extracting(SasColumn::getFormat)
                    .doesNotContain(SasColumnFormat.builder().name("PERCENT").width(8).precision(0).build(), Index.atIndex(3))
                    .doesNotContain(SasColumnFormat.builder().name("PERCENT").width(7).precision(1).build(), Index.atIndex(4));
        } catch (IOException | UncheckedIOException ex) {
            // can throw exception instead
        }
    }
}
