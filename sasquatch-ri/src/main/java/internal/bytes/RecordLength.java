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

import lombok.AccessLevel;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RecordLength {

    public static RecordLength of(int... lengths) {
        int offsets[] = new int[lengths.length];
        System.arraycopy(lengths, 0, offsets, 1, offsets.length - 1);
        int totalLength = lengths[0];
        for (int i = 1; i < offsets.length; i++) {
            totalLength += lengths[i];
            offsets[i] += offsets[i - 1];
        }
        return new RecordLength(lengths, offsets, totalLength);
    }

    private final int[] lengths;
    private final int[] offsets;

    @lombok.Getter
    private final int totalLength;

    public int getLength(int index) {
        return lengths[index];
    }

    public int getOffset(int index) {
        return offsets[index];
    }

    public RecordLength and(int... lengths) {
        return and(RecordLength.of(lengths));
    }

    public RecordLength and(RecordLength that) {
        int[] newLengths = new int[this.lengths.length + that.lengths.length];
        System.arraycopy(this.lengths, 0, newLengths, 0, this.lengths.length);
        System.arraycopy(that.lengths, 0, newLengths, this.lengths.length, that.lengths.length);
        return of(newLengths);
    }
}
