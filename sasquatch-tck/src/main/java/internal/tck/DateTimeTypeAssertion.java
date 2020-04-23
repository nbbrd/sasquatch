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
import java.time.LocalDateTime;
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
public final class DateTimeTypeAssertion extends AbstractFeatureAssertion {

    public DateTimeTypeAssertion() {
        super(SasFeature.DATE_TIME_TYPE, SasResources.EPAM.getRoot().resolve("date_formats.sas7bdat"));
    }

    @Override
    protected void assertSuccess(SoftAssertions s, SasReader reader) throws IOException {
//                s.assertThat(rs.getMetaData().getColumns())
//                        .allMatch(o -> !o.getSubType().equals(SasColumn.SubType.NONE))
//                        .hasSize(67);

        try (Stream<LocalDateTime> stream = rowsWithMapper(reader, o -> o.getDateTime(60))) {
            s.assertThat(stream)
                    .contains(LocalDateTime.parse("2017-03-14T15:36:56.546"), Index.atIndex(0))
                    .hasSize(1);
        }
    }

    @Override
    protected void assertFealure(SoftAssertions s, SasReader reader) throws IOException {
        s.assertThatThrownBy(() -> toList(reader, meta -> o -> o.getDateTime(60)))
                .describedAs("Excepting feature '%s' to raise IllegalArgumentException or IOException on '%s'", getFeature(), getFile())
                .isInstanceOfAny(IllegalArgumentException.class, IOException.class);
    }
}
