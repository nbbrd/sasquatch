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
package _test;

import java.io.EOFException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import sasquatch.SasCursor;
import sasquatch.SasRow;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public abstract class EOFRowCursor<T extends SasCursor & SasRow> extends EOFCursor<T> implements SasRow {

    @lombok.NonNull
    private final Opts opts;

    public EOFRowCursor(T delegate, EOFCursor.Opts cursor, Opts opts) {
        super(delegate, cursor);
        this.opts = opts;
    }

    @Override
    public Object getValue(int columnIndex) throws IOException, IndexOutOfBoundsException {
        if (opts.isAllowGetValue()) {
            return delegate.getValue(columnIndex);
        }
        throw new EOFException("getValue");
    }

    @Override
    public double getNumber(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        if (opts.isAllowGetNumber()) {
            return delegate.getNumber(columnIndex);
        }
        throw new EOFException("getNumber");
    }

    @Override
    public String getString(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        if (opts.isAllowGetString()) {
            return delegate.getString(columnIndex);
        }
        throw new EOFException("getString");
    }

    @Override
    public LocalDate getDate(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        if (opts.isAllowGetDate()) {
            return delegate.getDate(columnIndex);
        }
        throw new EOFException("getDate");
    }

    @Override
    public LocalDateTime getDateTime(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        if (opts.allowGetDateTime) {
            return delegate.getDateTime(columnIndex);
        }
        throw new EOFException("getDateTime");
    }

    @Override
    public LocalTime getTime(int columnIndex) throws IOException, IndexOutOfBoundsException, IllegalArgumentException {
        if (opts.isAllowGetTime()) {
            return delegate.getTime(columnIndex);
        }
        throw new EOFException("getTime");
    }

    @Override
    public Object[] getValues() throws IOException {
        if (opts.isAllowGetValues()) {
            return delegate.getValues();
        }
        throw new EOFException("getValues");
    }

    @lombok.Value
    @lombok.Builder
    @lombok.With
    public static class Opts {

        public static final Opts NONE = Opts.builder().build();

        private boolean allowGetValue;
        private boolean allowGetNumber;
        private boolean allowGetString;
        private boolean allowGetDate;
        private boolean allowGetDateTime;
        private boolean allowGetTime;
        private boolean allowGetValues;
    }
}
