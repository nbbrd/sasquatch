/*
 * Copyright 2016 National Bank of Belgium
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.Condition;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class Util {

    void assertValue(Class<?> type) {
        assertThat(type.getDeclaredFields()).are(new Condition<Field>() {
            @Override
            public boolean matches(Field o) {
                return Modifier.isFinal(o.getModifiers()) && Modifier.isPrivate(o.getModifiers());
            }
        });
        assertThat(type.getModifiers()).has(new Condition<Integer>() {
            @Override
            public boolean matches(Integer o) {
                return Modifier.isFinal(o);
            }
        });
    }

    void assertValueBuilder(Class<?> type) {
        assertThat(type.getDeclaredFields()).are(new Condition<Field>("Builder field must be private") {
            @Override
            public boolean matches(Field o) {
                return Modifier.isPrivate(o.getModifiers());
            }
        });
        assertThat(type.getModifiers()).has(new Condition<Integer>() {
            @Override
            public boolean matches(Integer o) {
                return Modifier.isFinal(o);
            }
        });
    }
}
