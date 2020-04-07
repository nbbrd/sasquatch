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
import nbbrd.service.ServiceProvider;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Index;
import static sasquatch.SasColumnType.CHARACTER;
import static sasquatch.SasColumnType.NUMERIC;
import sasquatch.SasMetaData;
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
public final class LittleEndian64Assertion extends AbstractFeatureAssertion {

    public LittleEndian64Assertion() {
        super(SasFeature.LITTLE_ENDIAN_64, SasResources.LITTLE_64);
    }

    @Override
    protected void assertSuccess(SoftAssertions s, SasReader reader) throws IOException {
        SasMetaData meta = reader.readMetaData(getFile());

        if (reader.getFeatures().contains(SasFeature.ATTRIBUTES)) {
            s.assertThat(meta.getCreationTime()).isEqualTo("2019-09-26T21:23:26.860");
            s.assertThat(meta.getCreationTime()).isEqualTo("2019-09-26T21:23:26.860");
            s.assertThat(meta.getRelease()).isEqualTo("9.0401M1");
            s.assertThat(meta.getHost()).isEqualTo("Linux");
            s.assertThat(meta.getName()).isEqualTo("TEST7");
        }

        s.assertThat(meta.getRowCount()).isEqualTo(10);

        s.assertThat(meta.getColumns())
                .extracting(AbstractFeatureAssertion::withoutFormat)
                .contains(columnOf(1, CHARACTER, 9, "Column2", "Column 2 label"), Index.atIndex(1))
                .contains(columnOf(4, NUMERIC, 8, "Column5", ""), Index.atIndex(4))
                .hasSize(100);

        super.assertSuccess(s, reader);
    }
}
