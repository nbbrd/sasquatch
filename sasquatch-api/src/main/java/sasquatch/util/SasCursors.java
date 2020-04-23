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
package sasquatch.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import sasquatch.SasCursor;
import sasquatch.SasForwardCursor;
import sasquatch.SasMetaData;
import sasquatch.SasRow;
import sasquatch.SasScrollableCursor;
import sasquatch.SasSplittableCursor;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SasCursors {

    @NonNull
    public <T> List<T> toList(@NonNull SasForwardCursor forward, SasRow.@NonNull Mapper<T> mapper) throws IOException {
        List<T> result = new ArrayList<>(forward.getRowCount());
        while (forward.next()) {
            result.add(mapper.apply(forward));
        }
        return result;
    }

    @NonNull
    public SasScrollableCursor asScrollable(@NonNull SasForwardCursor forward) throws IOException {
        try (SasForwardCursor cursor = forward) {
            return scrollableOf(cursor.getMetaData(), toList(forward, SasRow::getValues));
        }
    }

    @NonNull
    public SasSplittableCursor asSplittable(@NonNull SasForwardCursor forward) throws IOException {
        return new ForwardSplittable(forward);
    }

    @NonNull
    public SasForwardCursor forwardOf(@NonNull SasMetaData meta, @NonNull List<Object[]> data) {
        return new ListForwardCursor(meta, data);
    }

    @NonNull
    public SasScrollableCursor scrollableOf(@NonNull SasMetaData meta, @NonNull List<Object[]> data) {
        return new ListScrollableCursor(meta, data);
    }

    @NonNull
    public SasSplittableCursor splittableOf(@NonNull SasMetaData meta, @NonNull List<Object[]> data) {
        return new ListSplittableCursor(meta, data);
    }

    @lombok.AllArgsConstructor
    private static final class ForwardSplittable implements SasSplittableCursor {

        @lombok.NonNull
        @lombok.experimental.Delegate(types = SasCursor.class)
        private final SasForwardCursor cursor;

        @Override
        public Spliterator<SasRow> getSpliterator() throws IOException {
            return new ForwardSpliterator(cursor);
        }
    }

    @lombok.AllArgsConstructor
    private static final class ForwardSpliterator implements Spliterator<SasRow> {

        @lombok.NonNull
        private final SasForwardCursor cursor;

        @Override
        public boolean tryAdvance(Consumer<? super SasRow> action) {
            try {
                if (cursor.next()) {
                    action.accept(cursor);
                    return true;
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return false;
        }

        @Override
        public Spliterator<SasRow> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            try {
                return cursor.getRowCount();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        @Override
        public int characteristics() {
            return Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.SIZED;
        }
    }

    private static abstract class ListCursor implements SasCursor {

        protected final SasMetaData metaData;
        protected final List<Object[]> rows;

        public ListCursor(SasMetaData metaData, List<Object[]> rows) {
            this.metaData = metaData;
            this.rows = rows;
            if (metaData.getRowCount() != rows.size()) {
                throw new IllegalArgumentException("Meta row count and rows size are different");
            }
        }

        @Override
        public SasMetaData getMetaData() throws IOException {
            return metaData;
        }

        @Override
        public void close() throws IOException {
        }
    }

    private static final class ListForwardCursor extends ListCursor implements SasForwardCursor, ArrayRow {

        private int row = -1;

        public ListForwardCursor(SasMetaData meta, List<Object[]> data) {
            super(meta, data);
        }

        @Override
        public Object[] getCurrentRow() {
            return rows.get(row);
        }

        @Override
        public boolean next() throws IOException {
            row++;
            return 0 <= row && row < rows.size();
        }
    }

    private static final class ListScrollableCursor extends ListCursor implements SasScrollableCursor, ArrayRow {

        private int row = -1;

        public ListScrollableCursor(SasMetaData meta, List<Object[]> data) {
            super(meta, data);
        }

        @Override
        public Object[] getCurrentRow() {
            return rows.get(row);
        }

        @Override
        public int getRow() throws IOException {
            return row;
        }

        @Override
        public boolean moveTo(int row) throws IOException {
            this.row = row;
            return 0 <= row && row < rows.size();
        }
    }

    private static final class ListSplittableCursor extends ListCursor implements SasSplittableCursor {

        public ListSplittableCursor(SasMetaData meta, List<Object[]> data) {
            super(meta, data);
        }

        @Override
        public Spliterator<SasRow> getSpliterator() throws IOException {
            return new ListSpliterator(rows.spliterator());
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class ListSpliterator implements Spliterator<SasRow>, ArrayRow {

        private final Spliterator<Object[]> delegate;
        private Object[] currentRow;

        @Override
        public Object[] getCurrentRow() {
            return currentRow;
        }

        private SasRow apply(Object[] row) {
            this.currentRow = row;
            return this;
        }

        @Override
        public boolean tryAdvance(Consumer<? super SasRow> action) {
            return delegate.tryAdvance(o -> action.accept(apply(o)));
        }

        @Override
        public void forEachRemaining(Consumer<? super SasRow> action) {
            delegate.forEachRemaining(o -> action.accept(apply(o)));
        }

        @Override
        public Spliterator<SasRow> trySplit() {
            Spliterator<Object[]> result = delegate.trySplit();
            return result != null ? new ListSpliterator(result) : null;
        }

        @Override
        public long estimateSize() {
            return delegate.estimateSize();
        }

        @Override
        public long getExactSizeIfKnown() {
            return delegate.getExactSizeIfKnown();
        }

        @Override
        public int characteristics() {
            return delegate.characteristics();
        }

        @Override
        public boolean hasCharacteristics(int characteristics) {
            return delegate.hasCharacteristics(characteristics);
        }

        @Override
        public Comparator<? super SasRow> getComparator() {
            return null;
        }
    }

    private interface ArrayRow extends SasRow {

        Object[] getCurrentRow();

        default <T> T getValue(Class<T> type, int columnIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
            try {
                return type.cast(getCurrentRow()[columnIndex]);
            } catch (ClassCastException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @Override
        default Object getValue(int columnIndex) throws IOException {
            return getValue(Object.class, columnIndex);
        }

        @Override
        default double getNumber(int columnIndex) throws IOException {
            return getValue(Double.class, columnIndex);
        }

        @Override
        default String getString(int columnIndex) throws IOException {
            return getValue(String.class, columnIndex);
        }

        @Override
        default LocalDate getDate(int columnIndex) throws IOException {
            return getValue(LocalDate.class, columnIndex);
        }

        @Override
        default LocalDateTime getDateTime(int columnIndex) throws IOException {
            return getValue(LocalDateTime.class, columnIndex);
        }

        @Override
        default LocalTime getTime(int columnIndex) throws IOException {
            return getValue(LocalTime.class, columnIndex);
        }

        @Override
        default Object[] getValues() throws IOException {
            return getCurrentRow().clone();
        }
    }
}
