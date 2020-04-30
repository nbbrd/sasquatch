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
package sasquatch.spi;

/**
 * List of features that a reader can implement.
 *
 * @author Philippe Charles
 */
public enum SasFeature {

    BIG_ENDIAN_32, LITTLE_ENDIAN_32,
    BIG_ENDIAN_64, LITTLE_ENDIAN_64,
    ATTRIBUTES, LABEL_META,
    FIELD_ENCODING, COLUMN_ENCODING,
    CHAR_COMP, BIN_COMP,
    DATE_TYPE, DATE_TIME_TYPE, TIME_TYPE,
    CUSTOM_NUMERIC,
    COLUMN_FORMAT;
}
