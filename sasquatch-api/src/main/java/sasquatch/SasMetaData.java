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

import java.time.LocalDateTime;
import java.util.List;

/**
 * The metadata of a SAS dataset.
 *
 * @apiNote This class is immutable.
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class SasMetaData {

    /**
     * The dataset's name.<br>Note that this name might be empty.
     *
     * @return a non-null name
     */
    @lombok.NonNull
    @lombok.Builder.Default
    private String name = "";

    /**
     * The dataset's label.<br>Note that this label might be empty.
     *
     * @return a non-null label
     */
    @lombok.NonNull
    @lombok.Builder.Default
    private String label = "";

    /**
     * The dataset's creation time.
     *
     * @return a non-null creation time
     */
    @lombok.NonNull
    @lombok.Builder.Default
    private LocalDateTime creationTime = LocalDateTime.now();

    /**
     * The dataset's last modification time.
     *
     * @return a non-null last modification time
     */
    @lombok.NonNull
    @lombok.Builder.Default
    private LocalDateTime lastModificationTime = LocalDateTime.now();

    /**
     * The SAS release used to create the dataset.
     *
     * @return a non-null SAS release
     */
    @lombok.NonNull
    @lombok.Builder.Default
    private String release = "";

    /**
     * The SAS server type used to create the dataset.
     *
     * @return a non-null host
     */
    @lombok.NonNull
    @lombok.Builder.Default
    private String host = "";

    /**
     * Returns the total number of rows.
     *
     * @return a non-negative count
     */
    @NonNegative
    @lombok.Builder.Default
    private int rowCount = 0;

    /**
     * The list of columns in the dataset.
     *
     * @return a non-null unmodifiable list of non-null columns
     */
    @lombok.NonNull
    @lombok.Singular
    private List<SasColumn> columns;
}
