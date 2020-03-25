/*
 * Copyright 2017 National Bank of Belgium
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
package com.epam.parso.impl;

import sasquatch.SasColumnType;

/**
 *
 * @author Philippe Charles
 */
public final class ColumnUtil {

    public static SasColumnType getType(Class<?> type, String format) {
        if (Number.class.isAssignableFrom(type)) {
            if (SasFileConstants.DATE_TIME_FORMAT_STRINGS.contains(format)) {
                return SasColumnType.DATETIME;
            }
            if (SasFileConstants.DATE_FORMAT_STRINGS.contains(format)) {
                return SasColumnType.DATE;
            }
            if (format.equals("TIME")) {
                return SasColumnType.TIME;
            }
            return SasColumnType.NUMERIC;
        }
        if (String.class.isAssignableFrom(type)) {
            return SasColumnType.CHARACTER;
        }
        throw new RuntimeException("???");
    }
}
