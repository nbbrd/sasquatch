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
import _test.FailingSasCursor;
import _test.InvalidSasCursor;
import _test.Sample;
import java.io.EOFException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class FailsafeForwardCursorTest {

    @Test
    public void testNextRow() throws IOException {
        reset();
        assertThat(valid().next()).isEqualTo(true);
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> failing().next())
                .withCauseInstanceOf(UnsupportedOperationException.class);
        assertThat(errors).hasSize(1).containsKey("Unexpected error while calling 'next' on '_test.FailingSasCursor'");
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> eof().next())
                .isExactlyInstanceOf(EOFException.class)
                .withNoCause();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();
    }

    @Test
    public void testClose() throws IOException {
        reset();
        assertThatCode(() -> valid().close()).doesNotThrowAnyException();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> failing().close())
                .withCauseInstanceOf(UnsupportedOperationException.class);
        assertThat(errors).hasSize(1).containsKey("Unexpected error while calling 'close' on '_test.FailingSasCursor'");
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> eof().close())
                .isExactlyInstanceOf(EOFException.class)
                .withNoCause();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();
    }

    @Test
    public void testGetMetaData() throws IOException {
        reset();
        assertThat(valid().getMetaData()).isNotNull();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> failing().getMetaData())
                .withCauseInstanceOf(UnsupportedOperationException.class);
        assertThat(errors).hasSize(1).containsKey("Unexpected error while calling 'getMetaData' on '_test.FailingSasCursor'");
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> invalid().getMetaData())
                .withNoCause();
        assertThat(errors).isEmpty();
        assertThat(values).contains("Unexpected null value while calling 'getMetaData' on '_test.InvalidSasCursor'");

        reset();
        assertThatIOException()
                .isThrownBy(() -> eof().getMetaData())
                .isExactlyInstanceOf(EOFException.class)
                .withNoCause();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();
    }

    @Test
    public void testGetNumber() throws IOException {
        reset();
        assertThat(withNext(valid()).getNumber(0)).isNotNull();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> withNext(valid()).getNumber(1));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> withNext(valid()).getNumber(-1));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> withNext(valid()).getNumber(5));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> withNext(failing()).getNumber(0))
                .withCauseInstanceOf(UnsupportedOperationException.class);
        assertThat(errors).hasSize(1).containsKey("Unexpected error while calling 'getNumber' on '_test.FailingSasCursor'");
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> eof().getNumber(0))
                .isExactlyInstanceOf(EOFException.class)
                .withNoCause();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();
    }

    @Test
    public void testGetString() throws IOException {
        reset();
        assertThat(withNext(valid()).getString(1)).isNotNull();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> withNext(valid()).getString(0));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> withNext(valid()).getString(-1));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> withNext(valid()).getString(5));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> withNext(failing()).getString(1))
                .withCauseInstanceOf(UnsupportedOperationException.class);
        assertThat(errors).hasSize(1).containsKey("Unexpected error while calling 'getString' on '_test.FailingSasCursor'");
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> eof().getString(1))
                .isExactlyInstanceOf(EOFException.class)
                .withNoCause();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();
    }

    @Test
    public void testGetDate() throws IOException {
        reset();
        assertThat(withNext(valid()).getDate(2)).isNotNull();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> withNext(valid()).getDate(0));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> withNext(valid()).getDate(-1));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> withNext(valid()).getDate(5));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> withNext(failing()).getDate(2))
                .withCauseInstanceOf(UnsupportedOperationException.class);
        assertThat(errors).hasSize(1).containsKey("Unexpected error while calling 'getDate' on '_test.FailingSasCursor'");
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> eof().getDate(2))
                .isExactlyInstanceOf(EOFException.class)
                .withNoCause();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();
    }

    @Test
    public void testGetDateTime() throws IOException {
        reset();
        assertThat(withNext(valid()).getDateTime(3)).isNotNull();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> withNext(valid()).getDateTime(0));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> withNext(valid()).getDateTime(-1));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> withNext(valid()).getDateTime(5));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> withNext(failing()).getDateTime(3))
                .withCauseInstanceOf(UnsupportedOperationException.class);
        assertThat(errors).hasSize(1).containsKey("Unexpected error while calling 'getDateTime' on '_test.FailingSasCursor'");
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> eof().getDateTime(3))
                .isExactlyInstanceOf(EOFException.class)
                .withNoCause();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();
    }

    @Test
    public void testGetTime() throws IOException {
        reset();
        assertThat(withNext(valid()).getTime(4)).isNotNull();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> withNext(valid()).getTime(0));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> withNext(valid()).getTime(-1));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> withNext(valid()).getTime(5));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> withNext(failing()).getTime(4))
                .withCauseInstanceOf(UnsupportedOperationException.class);
        assertThat(errors).hasSize(1).containsKey("Unexpected error while calling 'getTime' on '_test.FailingSasCursor'");
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> eof().getTime(4))
                .isExactlyInstanceOf(EOFException.class)
                .withNoCause();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();
    }

    @Test
    public void testGetValue() throws IOException {
        reset();
        assertThat(withNext(valid()).getValue(0)).isInstanceOf(Double.class);
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThat(withNext(valid()).getValue(1)).isInstanceOf(String.class);
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThat(withNext(valid()).getValue(2)).isInstanceOf(LocalDate.class);
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThat(withNext(valid()).getValue(3)).isInstanceOf(LocalDateTime.class);
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThat(withNext(valid()).getValue(4)).isInstanceOf(LocalTime.class);
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> withNext(valid()).getValue(-1));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> withNext(valid()).getValue(5));
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> withNext(failing()).getValue(0))
                .withCauseInstanceOf(UnsupportedOperationException.class);
        assertThat(errors).hasSize(1).containsKey("Unexpected error while calling 'getValue' on '_test.FailingSasCursor'");
        assertThat(values).isEmpty();

        reset();
        assertThatIOException()
                .isThrownBy(() -> eof().getValue(0))
                .isExactlyInstanceOf(EOFException.class)
                .withNoCause();
        assertThat(errors).isEmpty();
        assertThat(values).isEmpty();
    }

    private FailsafeForwardCursor withNext(FailsafeForwardCursor o) {
        try {
            o.getDelegate().next();
        } catch (Exception ex) {
        }
        return o;
    }

    private final Map<String, RuntimeException> errors = new HashMap<>();
    private final List<String> values = new ArrayList<>();

    private final Failsafe failsafe = new Failsafe(errors::put, values::add);

    private FailsafeForwardCursor valid() {
        return new FailsafeForwardCursor(Sample.VALID_TABLE.readForward(), failsafe);
    }

    private FailsafeForwardCursor failing() {
        return new FailsafeForwardCursor(new FailingSasCursor(), failsafe);
    }

    private FailsafeForwardCursor invalid() {
        return new FailsafeForwardCursor(new InvalidSasCursor(), failsafe);
    }

    private FailsafeForwardCursor eof() {
        return new FailsafeForwardCursor(new EOFForward(Sample.VALID_TABLE.readForward(), EOFCursor.Opts.NONE, EOFForward.Opts.NONE), failsafe);
    }

    private void reset() {
        errors.clear();
        values.clear();
    }
}
