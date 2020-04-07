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

import org.checkerframework.checker.index.qual.NonNegative;

/**
 *
 * @author Philippe Charles
 * @see
 * https://documentation.sas.com/?docsetId=leforinforref&docsetTarget=n134ahpcz8murvn1x7went6p2czo.htm&docsetVersion=9.4&locale=en
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class SasColumnFormat {

    public static final SasColumnFormat EMPTY = new SasColumnFormat("", 0, 0);

    /**
     * Format name.
     */
    @lombok.NonNull
    private String name;

    /**
     * Format width.
     */
    @NonNegative
    private int width;

    /**
     * Optional decimal scaling factor in the numeric formats.
     */
    @NonNegative
    private int precision;

    @Override
    public String toString() {
        return width == 0 && precision == 0
                ? name
                : (name + width + "." + precision);
    }
}
