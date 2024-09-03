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
import _test.EOFForward;
import _test.EOFReader;
import _test.EOFRowCursor;
import _test.EOFScrollable;
import _test.EOFSplittable;
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
import org.junit.jupiter.api.Test;

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
    public void testReadForward() throws IOException {
        assertThatNullPointerException()
                .isThrownBy(() -> empty.readForward(null));

        assertThatIOException()
                .isThrownBy(() -> empty.readForward(Sample.FILE));

        List<Sample.Record> records = new ArrayList<>();
        try (SasForwardCursor cursor = sample.readForward(Sample.FILE)) {
            while (cursor.next()) {
                records.add(Sample.parseRecord(cursor));
            }
        }
        assertThat(records)
                .hasSize(1)
                .contains(Sample.ROW1, Index.atIndex(0));

        assertThatIOException()
                .isThrownBy(() -> eof.readForward(Sample.FILE))
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
                .isThrownBy(() -> empty.rows(null, meta -> Sample::parseRecord));

        assertThatNullPointerException()
                .isThrownBy(() -> empty.rows(Sample.FILE, null));

        assertThatIOException()
                .isThrownBy(() -> rowsToList(empty));

        assertThat(rowsToList(sample))
                .hasSize(1)
                .contains(Sample.ROW1, Index.atIndex(0));

        assertThatIOException()
                .isThrownBy(() -> {
                    rowsToList(eof(
                            EOFReader.Opts.NONE,
                            EOFCursor.Opts.NONE,
                            EOFRowCursor.Opts.NONE,
                            EOFForward.Opts.NONE,
                            EOFScrollable.Opts.NONE,
                            EOFSplittable.Opts.NONE
                    ));
                })
                .isExactlyInstanceOf(EOFException.class)
                .withMessageContaining("readSplittable");

        assertThatIOException()
                .isThrownBy(() -> {
                    rowsToList(eof(
                            EOFReader.Opts.ALL,
                            EOFCursor.Opts.NONE,
                            EOFRowCursor.Opts.NONE,
                            EOFForward.Opts.NONE,
                            EOFScrollable.Opts.NONE,
                            EOFSplittable.Opts.NONE
                    ));
                })
                .isExactlyInstanceOf(EOFException.class)
                .withMessageContaining("getSpliterator");

        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> {
                    rowsToList(eof(
                            EOFReader.Opts.ALL,
                            EOFCursor.Opts.META,
                            EOFRowCursor.Opts.NONE,
                            EOFForward.Opts.NONE,
                            EOFScrollable.Opts.NONE,
                            EOFSplittable.Opts.ALL
                    ));
                })
                .withCauseExactlyInstanceOf(EOFException.class)
                .withStackTraceContaining("close");
    }

    @Test
    public void testGetAllRows() throws IOException {
        assertThatNullPointerException()
                .isThrownBy(() -> empty.getAllRows(null, columns -> Sample::parseRecord));

        assertThatNullPointerException()
                .isThrownBy(() -> empty.getAllRows(Sample.FILE, null));

        assertThatIOException()
                .isThrownBy(() -> getAllRows(empty));

        assertThat(getAllRows(sample))
                .hasSize(1)
                .contains(Sample.ROW1, Index.atIndex(0));

        assertThatIOException()
                .isThrownBy(() -> {
                    getAllRows(eof(
                            EOFReader.Opts.NONE,
                            EOFCursor.Opts.NONE,
                            EOFRowCursor.Opts.NONE,
                            EOFForward.Opts.NONE,
                            EOFScrollable.Opts.NONE,
                            EOFSplittable.Opts.NONE
                    ));
                })
                .isExactlyInstanceOf(EOFException.class)
                .withMessageContaining("readForward");

        assertThatIOException()
                .isThrownBy(() -> {
                    getAllRows(eof(
                            EOFReader.Opts.ALL,
                            EOFCursor.Opts.NONE,
                            EOFRowCursor.Opts.NONE,
                            EOFForward.Opts.NONE,
                            EOFScrollable.Opts.NONE,
                            EOFSplittable.Opts.NONE
                    ));
                })
                .isExactlyInstanceOf(EOFException.class)
                .withMessageContaining("getRowCount");

        assertThatIOException()
                .isThrownBy(() -> {
                    getAllRows(eof(
                            EOFReader.Opts.ALL,
                            EOFCursor.Opts.META,
                            EOFRowCursor.Opts.NONE,
                            EOFForward.Opts.NONE,
                            EOFScrollable.Opts.NONE,
                            EOFSplittable.Opts.NONE
                    ));
                })
                .isExactlyInstanceOf(EOFException.class)
                .withMessageContaining("next")
                .withStackTraceContaining("close");
    }

    private final Sasquatch empty = Sasquatch.ofServiceLoader();
    private final Sasquatch sample = Sasquatch.of(Sample.VALID_READER);
    private final Sasquatch eof = eof(EOFReader.Opts.NONE, EOFCursor.Opts.NONE, EOFRowCursor.Opts.NONE, EOFForward.Opts.NONE, EOFScrollable.Opts.NONE, EOFSplittable.Opts.NONE);

    private Sasquatch eof(EOFReader.Opts reader, EOFCursor.Opts cursor, EOFRowCursor.Opts row, EOFForward.Opts forward, EOFScrollable.Opts scrollable, EOFSplittable.Opts splittable) {
        return Sasquatch.of(new EOFReader(Sample.VALID_READER, reader, cursor, row, forward, scrollable, splittable));
    }

    private List<Sample.Record> getAllRows(Sasquatch sasquatch) throws IOException {
        return sasquatch.getAllRows(Sample.FILE, columns -> Sample::parseRecord);
    }

    private List<Sample.Record> rowsToList(Sasquatch sasquatch) throws IOException {
        try (Stream<Sample.Record> stream = sasquatch.rows(Sample.FILE, meta -> Sample::parseRecord)) {
            return stream.collect(Collectors.toList());
        }
    }
}
