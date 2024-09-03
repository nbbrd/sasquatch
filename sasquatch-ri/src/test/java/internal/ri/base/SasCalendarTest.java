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
package internal.ri.base;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Philippe Charles
 */
public class SasCalendarTest {

    @Test
    public void testGetDate() {
        assertThat(SasCalendar.getDate(0)).isEqualTo(LocalDate.of(1960, 1, 1));
        assertThat(SasCalendar.getDate(1)).isEqualTo(LocalDate.of(1960, 1, 2));
        assertThat(SasCalendar.getDate(366)).isEqualTo(LocalDate.of(1961, 1, 1));
        assertThat(SasCalendar.getDate(-1)).isEqualTo(LocalDate.of(1959, 12, 31));
        assertThat(SasCalendar.getDate(Double.NaN)).isNull();
    }

    @Test
    public void testGetDateTime() {
        assertThat(SasCalendar.getDateTime(0)).isEqualTo(LocalDateTime.of(1960, 1, 1, 0, 0));
        assertThat(SasCalendar.getDateTime(3)).isEqualTo(LocalDateTime.of(1960, 1, 1, 0, 0, 3));
        assertThat(SasCalendar.getDateTime(-1)).isEqualTo(LocalDateTime.of(1959, 12, 31, 23, 59, 59));
        assertThat(SasCalendar.getDateTime(60 * 60 * 24)).isEqualTo(LocalDateTime.of(1960, 1, 2, 0, 0));
        assertThat(SasCalendar.getDateTime(Double.NaN)).isNull();
        assertThat(SasCalendar.getDateTime(0.123)).isEqualTo(LocalDateTime.of(1960, 1, 1, 0, 0, 0, 123000000));
    }

    @Test
    public void testGetTime() {
        assertThat(SasCalendar.getTime(0)).isEqualTo(LocalTime.of(0, 0));
        assertThat(SasCalendar.getTime(1)).isEqualTo(LocalTime.of(0, 0, 1));
        assertThat(SasCalendar.getTime(Double.NaN)).isNull();
        assertThat(SasCalendar.getTime(0.123)).isEqualTo(LocalTime.of(0, 0, 0, 123000000));
    }
}
