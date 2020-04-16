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
package sasquatch;

import _test.EOFCursor;
import _test.EOFReader;
import _test.Sample;
import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.data.Index;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SasquatchTest {

    @Test
    public void testFactories() {
        assertThatNullPointerException()
                .isThrownBy(() -> Sasquatch.of(null));

        assertThat(Sasquatch.ofServiceLoader()).isNotNull();
    }

    @Test
    public void testRead() throws IOException {
        assertThatNullPointerException()
                .isThrownBy(() -> empty.read(null));

        assertThatIOException()
                .isThrownBy(() -> empty.read(Sample.FILE));

        List<Sample.Record> records = new ArrayList<>();
        try (SasResultSet rs = sample.read(Sample.FILE)) {
            while (rs.next()) {
                records.add(Sample.parseRecord(rs));
            }
        }
        assertThat(records)
                .hasSize(1)
                .contains(Sample.ROW1, Index.atIndex(0));

        assertThatIOException()
                .isThrownBy(() -> eof.read(Sample.FILE))
                .isExactlyInstanceOf(EOFException.class)
                .withMessageContaining("read");
    }

    @Test
    public void testReadMetaData() throws IOException {
        assertThatNullPointerException()
                .isThrownBy(() -> empty.readMetaData(null));

        assertThatIOException()
                .isThrownBy(() -> empty.readMetaData(Sample.FILE));

        assertThat(sample.readMetaData(Sample.FILE)).isNotNull();

        assertThatIOException()
                .isThrownBy(() -> eof.readMetaData(Sample.FILE))
                .isExactlyInstanceOf(EOFException.class)
                .withMessageContaining("readMetaData");
    }

    @Test
    public void testRows() throws IOException {
        assertThatNullPointerException()
                .isThrownBy(() -> empty.rows(null, Sample::parseRecord));

        assertThatNullPointerException()
                .isThrownBy(() -> empty.rows(Sample.FILE, null));

        assertThatIOException()
                .isThrownBy(() -> rowsToList(empty));

        assertThat(rowsToList(sample))
                .hasSize(1)
                .contains(Sample.ROW1, Index.atIndex(0));

        assertThatIOException()
                .isThrownBy(() -> {
                    EOFReader.Behavior behavior = EOFReader.Behavior.NONE;
                    rowsToList(eof(behavior));
                })
                .isExactlyInstanceOf(EOFException.class)
                .withMessageContaining("read");

        assertThatIOException()
                .isThrownBy(() -> {
                    EOFReader.Behavior behavior = EOFReader.Behavior.NONE
                            .withAllowRead(true);
                    rowsToList(eof(behavior));
                })
                .isExactlyInstanceOf(EOFException.class)
                .withMessageContaining("getMetaData");

        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> {
                    EOFReader.Behavior behavior = EOFReader.Behavior.NONE
                            .withAllowRead(true)
                            .withResultSet(EOFCursor.Behavior.NONE.withAllowGetMetaData(true));
                    rowsToList(eof(behavior));
                })
                .withCauseExactlyInstanceOf(EOFException.class)
                .withStackTraceContaining("close");
    }

    @Test
    public void testGetAllRows() throws IOException {
        assertThatNullPointerException()
                .isThrownBy(() -> empty.getAllRows(null, Sample::parseRecord));

        assertThatNullPointerException()
                .isThrownBy(() -> empty.getAllRows(Sample.FILE, null));

        assertThatIOException()
                .isThrownBy(() -> getAllRows(empty));

        assertThat(getAllRows(sample))
                .hasSize(1)
                .contains(Sample.ROW1, Index.atIndex(0));

        assertThatIOException()
                .isThrownBy(() -> {
                    EOFReader.Behavior behavior = EOFReader.Behavior.NONE;
                    getAllRows(eof(behavior));
                })
                .isExactlyInstanceOf(EOFException.class)
                .withMessageContaining("read");

        assertThatIOException()
                .isThrownBy(() -> {
                    EOFReader.Behavior behavior = EOFReader.Behavior.NONE
                            .withAllowRead(true);
                    getAllRows(eof(behavior));
                })
                .isExactlyInstanceOf(EOFException.class)
                .withMessageContaining("getMetaData");

        assertThatIOException()
                .isThrownBy(() -> {
                    EOFReader.Behavior behavior = EOFReader.Behavior.NONE
                            .withAllowRead(true)
                            .withResultSet(EOFCursor.Behavior.NONE.withAllowGetMetaData(true));
                    getAllRows(eof(behavior));
                })
                .isExactlyInstanceOf(EOFException.class)
                .withMessageContaining("nextRow")
                .withStackTraceContaining("close");
    }

    private final Sasquatch empty = Sasquatch.ofServiceLoader();
    private final Sasquatch sample = Sasquatch.of(Sample.VALID_READER);
    private final Sasquatch eof = Sasquatch.of(new EOFReader(Sample.VALID_READER, EOFReader.Behavior.NONE));

    private Sasquatch eof(EOFReader.Behavior behavior) {
        return Sasquatch.of(new EOFReader(Sample.VALID_READER, behavior));
    }

    private List<Sample.Record> getAllRows(Sasquatch sasquatch) throws IOException {
        return sasquatch.getAllRows(Sample.FILE, Sample::parseRecord);
    }

    private List<Sample.Record> rowsToList(Sasquatch sasquatch) throws IOException {
        try (Stream<Sample.Record> stream = sasquatch.rows(Sample.FILE, Sample::parseRecord)) {
            return stream.collect(Collectors.toList());
        }
    }
}
