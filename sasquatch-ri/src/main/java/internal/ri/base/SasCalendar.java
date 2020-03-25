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
import java.time.temporal.ChronoUnit;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public final class SasCalendar {

    private static final LocalDate DATE_EPOCH = LocalDate.of(1960, 1, 1);
    private static final LocalDateTime DATE_TIME_EPOCH = LocalDateTime.of(1960, 1, 1, 0, 0);

    /**
     * Gets the calendar's time with the number of seconds since midnight.
     *
     * @param numberOfSecondsSinceMidnight the number of seconds since midnight
     * @return a time if available, null otherwise
     */
    public LocalTime getTime(double numberOfSecondsSinceMidnight) {
        return !Double.isNaN(numberOfSecondsSinceMidnight)
                ? LocalTime.MIDNIGHT.plus((long) (numberOfSecondsSinceMidnight * 1000), ChronoUnit.MILLIS)
                : null;
    }

    /**
     * Gets the calendar's time with the number of seconds since SAS epoch
     * (1960/01/01).
     *
     * @param numberOfSecondsSinceEpoch the number of seconds since SAS epoch
     * @return a date time if available, null otherwise
     */
    public LocalDateTime getDateTime(double numberOfSecondsSinceEpoch) {
        return !Double.isNaN(numberOfSecondsSinceEpoch)
                ? DATE_TIME_EPOCH.plus((long) (numberOfSecondsSinceEpoch * 1000), ChronoUnit.MILLIS)
                : null;
    }

    /**
     * Gets the calendar's time with the number of days since SAS epoch
     * (1960/01/01).
     *
     * @param numberOfDaysSinceEpoch the number of days since SAS epoch
     * @return a date if available, null otherwise
     */
    public LocalDate getDate(double numberOfDaysSinceEpoch) {
        return !Double.isNaN(numberOfDaysSinceEpoch)
                ? DATE_EPOCH.plusDays((long) numberOfDaysSinceEpoch)
                : null;
    }
}
