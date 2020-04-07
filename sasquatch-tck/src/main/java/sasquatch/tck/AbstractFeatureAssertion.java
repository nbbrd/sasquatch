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
package sasquatch.tck;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import sasquatch.SasColumn;
import sasquatch.SasColumnFormat;
import sasquatch.SasColumnType;
import sasquatch.SasRow;
import sasquatch.SasRowMapper;
import sasquatch.Sasquatch;
import sasquatch.spi.SasFeature;
import sasquatch.spi.SasReader;

/**
 *
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.AllArgsConstructor
public abstract class AbstractFeatureAssertion implements SasFeatureAssertion {

    private final SasFeature feature;
    private final Path file;

    @Override
    final public void assertFeature(SoftAssertions s, SasReader reader, Set<SasFeature> features) throws IOException {
        if (features.contains(feature)) {
            assertSuccess(s, reader);
        } else {
            assertFealure(s, reader);
        }
    }

    protected <T> Stream<T> rows(SasReader reader, SasRowMapper<T> rowMapper) throws IOException {
        return Sasquatch.of(reader).rows(file, rowMapper);
    }

    protected void assertSuccess(SoftAssertions s, SasReader reader) throws IOException {
        s.assertThat(toList(reader, AbstractFeatureAssertion::rowToArray))
                .describedAs("Excepting feature '%s' to have the right row count on '%s'", feature, file)
                .hasSize(reader.readMetaData(file).getRowCount());
    }

    protected void assertFealure(SoftAssertions s, SasReader reader) throws IOException {
        assertFealure(s, reader, AbstractFeatureAssertion::rowToArray);
    }

    protected void assertFealure(SoftAssertions s, SasReader reader, SasRowMapper<?> rowMapper) throws IOException {
        s.assertThatThrownBy(() -> toList(reader, rowMapper))
                .describedAs("Excepting feature '%s' to raise IOException on '%s'", feature, file)
                .isInstanceOf(IOException.class);
    }

    private <X> List<X> toList(SasReader reader, SasRowMapper<X> rowMapper) throws IOException {
        try (Stream<X> stream = rows(reader, rowMapper)) {
            return stream.collect(Collectors.toList());
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    private static Object[] rowToArray(SasRow row) throws IOException {
        Object[] result = new Object[row.getMetaData().getColumns().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = row.getValue(i);
        }
        return result;
    }

    public static SasColumn columnOf(int order, SasColumnType type, int length, String name, String label) {
        return SasColumn.builder().order(order).type(type).length(length).name(name).label(label).build();
    }

    public static SasColumn withoutFormat(SasColumn column) {
        return column.toBuilder().format(SasColumnFormat.EMPTY).build();
    }
}
