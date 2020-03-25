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
import java.util.stream.Stream;
import nbbrd.service.ServiceProvider;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Index;
import static org.assertj.core.data.Index.atIndex;
import sasquatch.SasColumn;
import static sasquatch.SasColumnType.*;
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
public final class LittleEndian32Assertion extends AbstractFeatureAssertion {

    public LittleEndian32Assertion() {
        super(SasFeature.LITTLE_ENDIAN_32, SasResources.LITTLE_32);
    }

    @Override
    protected void assertSuccess(SoftAssertions s, SasReader reader) throws IOException {
        SasMetaData meta = reader.readMetaData(getFile());

        if (reader.getFeatures().contains(SasFeature.ATTRIBUTES)) {
            s.assertThat(meta.getCreationTime()).isEqualTo("2001-03-29T09:11:47.203");
            s.assertThat(meta.getLastModificationTime()).isEqualTo("2001-07-09T12:59:39.329");
            s.assertThat(meta.getRelease()).isEqualTo("8.0101M0");
            s.assertThat(meta.getHost()).isEqualTo("WIN_NT");
            s.assertThat(meta.getName()).isEqualTo("STATESEX2");
        }

        s.assertThat(meta.getRowCount()).isEqualTo(50);

        s.assertThat(meta.getColumns())
                .contains(SasColumn.builder().order(0).type(CHARACTER).name("State").length(16).format("").build(), atIndex(0))
                .contains(SasColumn.builder().order(2).type(NUMERIC).name("Size").length(8).format("").build(), atIndex(2))
                .hasSize(3);

        try (Stream<Little32> stream = rows(reader, o -> new Little32(o.getString(0), o.getNumber(2)))) {
            s.assertThat(stream)
                    .contains(new Little32("Delaware", 1955d), Index.atIndex(0))
                    .contains(new Little32("Hawaii", 6423d), Index.atIndex(49))
                    .hasSize(50);
        }
    }

    @lombok.Value
    private static class Little32 {

        String state;
        double size;
    }
}
