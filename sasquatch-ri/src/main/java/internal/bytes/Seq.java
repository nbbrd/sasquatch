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

    @lombok.Getter
    @lombok.NonNull
    private final List<Item> items;

    @lombok.NonNull
    private final RecordLength length32;

    @lombok.NonNull
    private final RecordLength length64;

    public RecordLength getLength(boolean u64) {
        return u64 ? length64 : length32;
    }

    private Seq(List<Item> items) {
        this.items = items;
        this.length32 = RecordLength.of(items.stream().mapToInt(Item::getLength32).toArray());
        this.length64 = RecordLength.of(items.stream().mapToInt(Item::getLength64).toArray());
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
            return item(Item.bytes(name, length));
        }

        public Builder and(String name, int length32, int length64) {
            return item(Item.bytes(name, length32, length64));
        }

        public Builder and(String name, Item item) {
            return item(Item.bytes(name, item.getLength32(), item.getLength64()));
        }

        public Builder and(String name, Seq seq) {
            return item(Item.bytes(name, seq.getLength(false).getTotalLength(), seq.getLength(true).getTotalLength()));
        }

        public Seq build() {
            return new Seq(Collections.unmodifiableList(new ArrayList<>(items)));
        }
    }

    @lombok.Value
    public static class Item {

        public static final Item U2 = bytes("int16", 2);
        public static final Item U4U8 = bytes("int32/64", 4, 8);

        public static Item bytes(String name, int length) {
            return bytes(name, length, length);
        }

        public static Item bytes(String name, int length32, int length64) {
            return new Item(name, length32, length64);
        }

        @lombok.NonNull
        private String name;

        @NonNegative
        private int length32;

        @NonNegative
        private int length64;
    }
}
