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
import sasquatch.SasForwardCursor;
import sasquatch.SasMetaData;
import sasquatch.SasRow;
import sasquatch.SasScrollableCursor;
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

    protected <T> Stream<T> rows(SasReader reader, SasRow.Factory<T> factory) throws IOException {
        return Sasquatch.of(reader).rows(file, factory);
    }

    protected <T> Stream<T> rowsWithMapper(SasReader reader, SasRow.Mapper<T> mapper) throws IOException {
        return rows(reader, cursor -> mapper);
    }

    protected void assertSuccess(SoftAssertions s, SasReader reader) throws IOException {
        s.assertThat(toList(reader, cursor -> SasRow::getValues))
                .describedAs("Excepting feature '%s' to have the right row count on '%s'", feature, file)
                .hasSize(reader.readMetaData(file).getRowCount());
    }

    protected void assertFealure(SoftAssertions s, SasReader reader) throws IOException {
        assertFealure(s, reader, cursor -> SasRow::getValues);
    }

    protected <T> void assertFealure(SoftAssertions s, SasReader reader, SasRow.Factory<T> mapper) throws IOException {
        s.assertThatThrownBy(() -> toList(reader, mapper))
                .describedAs("Excepting feature '%s' to raise IOException on '%s'", feature, file)
                .isInstanceOf(IOException.class);
    }

    protected void assertSameContent(SoftAssertions s, SasReader reader) throws IOException {
        SasMetaData meta = reader.readMetaData(file);
        try (SasForwardCursor forward = reader.readForward(file)) {
            s.assertThat(forward.getRowCount()).isEqualTo(meta.getRowCount());
            s.assertThat(forward.getColumns()).isEqualTo(meta.getColumns());
            try (SasScrollableCursor scrollable = reader.readScrollable(file)) {
                s.assertThat(scrollable.getRowCount()).isEqualTo(meta.getRowCount());
                s.assertThat(scrollable.getColumns()).isEqualTo(meta.getColumns());
                int row = -1;
                while (forward.next()) {
                    row++;
                    s.assertThat(scrollable.moveTo(row)).isTrue();
                    s.assertThat(scrollable.getRow()).isEqualTo(row);
                    for (int j = 0; j < forward.getColumns().size(); j++) {
                        s.assertThat(forward.getValue(j)).isEqualTo(scrollable.getValue(j));
                    }
                }
                s.assertThat(scrollable.moveTo(row + 1)).isFalse();
                s.assertThat(scrollable.moveTo(-1)).isFalse();
                s.assertThat(row).isEqualTo(meta.getRowCount() - 1);
            }
        }
    }

    protected <T> List<T> toList(SasReader reader, SasRow.Factory<T> mapper) throws IOException {
        try (Stream<T> stream = rows(reader, mapper)) {
            return stream.collect(Collectors.toList());
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    public static SasColumn columnOf(int order, SasColumnType type, int length, String name, String label) {
        return SasColumn.builder().order(order).type(type).length(length).name(name).label(label).build();
    }

    public static SasColumn withoutFormat(SasColumn column) {
        return column.toBuilder().format(SasColumnFormat.EMPTY).build();
    }
}
