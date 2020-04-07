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
 * An immutable object that describes a column in a .sas7bdat file.
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class SasColumn {

    /**
     * A zero-based index that defines the column order in a sas table. This
     * order is unique by table.
     *
     * @return a zero-based index
     */
    @NonNegative
    private int order;

    /**
     * This column's name. This name is unique by table.
     *
     * @return a non-null name.
     */
    @lombok.NonNull
    private String name;

    /**
     * This column's type.
     *
     * @return a non-null type.
     */
    @lombok.NonNull
    private SasColumnType type;

    /**
     * This column's length (in bytes) in a row.
     *
     * @return a length
     */
    @NonNegative
    private int length;

    /**
     * This column's format used for formatting. Note that it also determines
     * the column's type.
     *
     * @return a non-null format
     */
    @lombok.NonNull
    private SasColumnFormat format;

    /**
     * This column's label.
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
