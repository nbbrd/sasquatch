/*
 * Copyright 2013 National Bank of Belgium
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class SasColumnTest {

    @Test
    @SuppressWarnings("null")
    public void testBuilder() {
        final SasColumn.Builder b = SasColumn.builder();
        assertThatThrownBy(() -> b.name(null).build()).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> b.type(null).build()).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> b.format(null).build()).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> b.label(null).build()).isInstanceOf(NullPointerException.class);
        assertThat(b.order(0).name("col1").type(SasColumnType.NUMERIC).length(8).format(SasColumnFormat.EMPTY).label("My Column").build())
                .extracting("order", "name", "type", "length", "format", "label")
                .containsExactly(0, "col1", SasColumnType.NUMERIC, 8, SasColumnFormat.EMPTY, "My Column");
    }

    @Test
    public void testHashCode() {
        SasColumn.Builder b = SasColumn.builder();
        assertThat(b.build().hashCode())
                .isEqualTo(b.build().hashCode())
                .isNotEqualTo(b.format(helloFormat).build().hashCode());
    }

    @Test
    public void testEquals() {
        SasColumn.Builder b = SasColumn.builder();
        assertThat(b.build())
                .isNotSameAs(b.build())
                .isEqualTo(b.build())
                .isNotEqualTo(b.format(helloFormat).build());
    }

    private final SasColumnFormat helloFormat = SasColumnFormat.builder().name("hello").build();
}
