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

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * A column in a SAS dataset.
 *
 * @author Philippe Charles
 * @apiNote This class is immutable.
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class SasColumn {

    /**
     * A zero-based index that defines the column order. This order is unique by
     * dataset.
     *
     * @return a zero-based index
     */
    @NonNegative
    @lombok.Builder.Default
    private int order = 0;

    /**
     * The column's name. This name is unique by dataset.
     *
     * @return a non-null name.
     */
    @lombok.NonNull
    @lombok.Builder.Default
    private String name = "";

    /**
     * The column's type.
     *
     * @return a non-null type.
     */
    @lombok.NonNull
    @lombok.Builder.Default
    private SasColumnType type = SasColumnType.CHARACTER;

    /**
     * The column's length (in bytes) in a row.
     *
     * @return a length
     */
    @NonNegative
    @lombok.Builder.Default
    private int length = 0;

    /**
     * The column's format.
     *
     * @return a non-null format
     */
    @lombok.NonNull
    @lombok.Builder.Default
    private SasColumnFormat format = SasColumnFormat.EMPTY;

    /**
     * The column's label.
     *
     * @return a non-null label
     */
    @lombok.NonNull
    @lombok.Builder.Default
    private String label = "";
}
