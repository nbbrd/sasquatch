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

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Philippe Charles
 */
public class RecordLengthTest {

    @Test
    public void test() {
        assertThatThrownBy(() -> RecordLength.of()).isInstanceOf(RuntimeException.class);

        assertThat(RecordLength.of(8).getOffset(0)).isEqualTo(0);
        assertThat(RecordLength.of(8).getTotalLength()).isEqualTo(8);

        assertThat(RecordLength.of(8, 20, 4).getOffset(0)).isEqualTo(0);
        assertThat(RecordLength.of(8, 20, 4).getOffset(1)).isEqualTo(8);
        assertThat(RecordLength.of(8, 20, 4).getOffset(2)).isEqualTo(28);
        assertThat(RecordLength.of(8, 20, 4).getTotalLength()).isEqualTo(32);

        assertThat(RecordLength.of(8, 20, 4).and(3).getOffset(3)).isEqualTo(32);
    }
}
