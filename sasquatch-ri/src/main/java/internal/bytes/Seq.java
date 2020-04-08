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
package internal.bytes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 *
 * @author Philippe Charles
 */
public final class Seq {

    public static int getU4U8(BytesReader bytes, int offset, boolean u64) {
        return u64 ? ((int) bytes.getInt64(offset)) : bytes.getInt32(offset);
    }

    public static final Seq U4U8 = builder().and("int32/64", 4, 8).build();

    @lombok.Getter
    @lombok.NonNull
    private final List<Item> items;

    @lombok.NonNull
    private final RecordLength length32;

    @lombok.NonNull
    private final RecordLength length64;

    private Seq(List<Item> items) {
        this.items = items;
        this.length32 = RecordLength.of(items.stream().mapToInt(Item::getLength32).toArray());
        this.length64 = RecordLength.of(items.stream().mapToInt(Item::getLength64).toArray());
    }

    private RecordLength get(boolean u64) {
        return u64 ? length64 : length32;
    }

    public int getOffset(boolean u64, int index) {
        return get(u64).getOffset(index);
    }

    public int getTotalLength(boolean u64) {
        return get(u64).getTotalLength();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final List<Item> items = new ArrayList<>();

        private Builder item(Item item) {
            items.add(item);
            return this;
        }

        public Builder and(String name, int length) {
            return item(new Item(name, length, length));
        }

        public Builder and(String name, int length32, int length64) {
            return item(new Item(name, length32, length64));
        }

        public Builder and(String name, Seq seq) {
            return item(new Item(name, seq.get(false).getTotalLength(), seq.get(true).getTotalLength()));
        }

        public Seq build() {
            switch (items.size()) {
                case 0:
                    return new Seq(Collections.emptyList());
                case 1:
                    return new Seq(Collections.singletonList(items.get(0)));
                default:
                    return new Seq(Collections.unmodifiableList(new ArrayList<>(items)));
            }
        }
    }

    @lombok.Value
    public static class Item {

        @lombok.NonNull
        private String name;

        @NonNegative
        private int length32;

        @NonNegative
        private int length64;
    }
}
