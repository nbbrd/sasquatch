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
 * @apiNote This class is immutable.
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class SasColumn {

    /**
     * A zero-based index that defines the column order. This order is unique by
     * dataset.
     *
     * @return a zero-based index
     */
    @NonNegative
    private int order;

    /**
     * The column's name. This name is unique by dataset.
     *
     * @return a non-null name.
     */
    @lombok.NonNull
    private String name;

    /**
     * The column's type.
     *
     * @return a non-null type.
     */
    @lombok.NonNull
    private SasColumnType type;

    /**
     * The column's length (in bytes) in a row.
     *
     * @return a length
     */
    @NonNegative
    private int length;

    /**
     * The column's format.
     *
     * @return a non-null format
     */
    @lombok.NonNull
    private SasColumnFormat format;

    /**
     * The column's label.
     *
     * @return a non-null label
     */
    @lombok.NonNull
    private String label;

    // Fix NetBeans bug with @lombok.Builder.Default
    public static Builder builder() {
        return new Builder()
                .order(0)
                .name("")
                .type(SasColumnType.CHARACTER)
                .length(0)
                .format(SasColumnFormat.EMPTY)
                .label("");
    }
}
