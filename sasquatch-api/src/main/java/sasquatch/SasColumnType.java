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
package sasquatch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * The type of a column in a SAS dataset.
 */
public enum SasColumnType {

    /**
     * Character string as {@link String} class.
     */
    CHARACTER,
    /**
     * Numeric value as {@link double} primitive.
     */
    NUMERIC,
    /**
     * Date value as {@link LocalDate} class.
     */
    DATE,
    /**
     * Date and time value as {@link LocalDateTime} class.
     */
    DATETIME,
    /**
     * Time value as {@link LocalTime} class.
     */
    TIME;
}
