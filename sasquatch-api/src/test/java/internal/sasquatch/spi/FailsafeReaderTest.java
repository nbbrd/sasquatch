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
package internal.sasquatch.spi;

import _test.EOFCursor;
import _test.EOFForward;
import _test.EOFReader;
import _test.EOFRowCursor;
import _test.EOFScrollable;
import _test.EOFSplittable;
import _test.FailingSasReader;
import _test.InvalidSasReader;
import _test.Sample;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import sasquatch.SasForwardCursor;
import sasquatch.spi.SasReader;

/**
 *
 * @author Philippe Charles
 */
public class FailsafeReaderTest {

    @Test
    public void testName() {
        reset();
        assertThat(valid.getName()).isEqualTo("valid");
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThat(failing.getName()).isEqualTo(FailingSasReader.class.getName());
        assertThat(errors).hasSize(1).containsKey("Unexpected error while calling 'getName' on '_test.FailingSasReader'");
        assertThat(values).isEmpty();

        reset();
        assertThat(invalid.getName()).isEqualTo(InvalidSasReader.class.getName());
        assertThat(errors).isEmpty();
        assertThat(values).hasSize(1).contains("Unexpected null value while calling 'getName' on '_test.InvalidSasReader'");
    }

    @Test
    public void testIsAvailable() {
        reset();
        assertThat(valid.isAvailable()).isTrue();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThat(failing.isAvailable()).isEqualTo(false);
        assertThat(errors).hasSize(1).containsKey("Unexpected error while calling 'isAvailable' on '_test.FailingSasReader'");
        assertThat(values).isEmpty();
    }

    @Test
    public void testGetCost() {
        reset();
        assertThat(valid.getCost()).isEqualTo(SasReader.ADVANCED_SUPPORT);
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThat(failing.getCost()).isEqualTo(Integer.MAX_VALUE);
        assertThat(errors).hasSize(1).containsKey("Unexpected error while calling 'getCost' on '_test.FailingSasReader'");
        assertThat(values).isEmpty();

        reset();
        assertThat(invalid.getCost()).isEqualTo(Integer.MAX_VALUE);
        assertThat(errors).isEmpty();
        assertThat(values).hasSize(1).contains("Unexpected negative value while calling 'getCost' on '_test.InvalidSasReader'");
    }

    @Test
    public void testGetFeatures() {
        reset();
        assertThat(valid.getFeatures()).isNotNull();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThat(failing.getFeatures()).isEmpty();
        assertThat(errors).hasSize(1).containsKey("Unexpected error while calling 'getFeatures' on '_test.FailingSasReader'");
        assertThat(values).isEmpty();

        reset();
        assertThat(invalid.getFeatures()).isEmpty();
        assertThat(errors).isEmpty();
        assertThat(values).hasSize(1).contains("Unexpected null value while calling 'getFeatures' on '_test.InvalidSasReader'");
    }

    @Test
    public void testReadForward() throws IOException {
        reset();
        try (SasForwardCursor cursor = valid.readForward(Sample.FILE)) {
            assertThat(cursor).isNotNull();
        }
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> failing.readForward(Sample.FILE))
                .withCauseExactlyInstanceOf(UnsupportedOperationException.class);
        assertThat(errors).containsKey("Unexpected error while calling 'readForward' on '_test.FailingSasReader'");
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> invalid.readForward(Sample.FILE))
                .withNoCause();
        assertThat(errors).isEmpty();
        assertThat(values).hasSize(1).contains("Unexpected null value while calling 'readForward' on '_test.InvalidSasReader'");

        reset();
        assertThatIOException()
                .isThrownBy(() -> eof.readForward(Sample.FILE))
                .isExactlyInstanceOf(EOFException.class)
                .withNoCause();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();
    }

    @Test
    public void testReadMetaData() throws IOException {
        reset();
        assertThat(valid.readMetaData(Sample.FILE)).isNotNull();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> failing.readMetaData(Sample.FILE))
                .withCauseExactlyInstanceOf(UnsupportedOperationException.class);
        assertThat(errors).containsKey("Unexpected error while calling 'readMetaData' on '_test.FailingSasReader'");
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> invalid.readMetaData(Sample.FILE))
                .withNoCause();
        assertThat(errors).isEmpty();
        assertThat(values).hasSize(1).contains("Unexpected null value while calling 'readMetaData' on '_test.InvalidSasReader'");

        reset();
        assertThatIOException()
                .isThrownBy(() -> eof.readMetaData(Sample.FILE))
                .isExactlyInstanceOf(EOFException.class)
                .withNoCause();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();
    }

    private final Map<String, RuntimeException> errors = new HashMap<>();
    private final List<String> values = new ArrayList<>();

    private final Failsafe failsafe = new Failsafe(errors::put, values::add);

    private final FailsafeReader valid = new FailsafeReader(Sample.VALID_READER, failsafe);
    private final FailsafeReader failing = new FailsafeReader(new FailingSasReader(), failsafe);
    private final FailsafeReader invalid = new FailsafeReader(new InvalidSasReader(), failsafe);
    private final FailsafeReader eof = new FailsafeReader(new EOFReader(Sample.VALID_READER, EOFReader.Opts.NONE, EOFCursor.Opts.NONE, EOFRowCursor.Opts.NONE, EOFForward.Opts.NONE, EOFScrollable.Opts.NONE, EOFSplittable.Opts.NONE), failsafe);

    private void reset() {
        errors.clear();
        values.clear();
    }
}
