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

import java.time.LocalDateTime;
import java.util.List;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * An immutable object that contains the metadata available in a SAS dataset
 * (*.sas7bdat).
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class SasMetaData {

    /**
     * The name of the SAS dataset.<br>Note that this name might be empty.
     *
     * @return a non-null name
     */
    @lombok.NonNull
    private String name;

    /**
     * The creation time of the SAS dataset.
     *
     * @return the creation time
     */
    @lombok.NonNull
    private LocalDateTime creationTime;

    /**
     * The last modification time of the SAS dataset.
     *
     * @return the last modification time
     */
    @lombok.NonNull
    private LocalDateTime lastModificationTime;

    /**
     * The SAS release used to create the SAS dataset.
     *
     * @return a non-null SAS release
     */
    @lombok.NonNull
    private String release;

    /**
     * The SAS server type used to create the SAS dataset.
     *
     * @return a non-null host
     */
    @lombok.NonNull
    private String host;

    /**
     * Returns the total number of rows.
     *
     * @return a count
     */
    @NonNegative
    private int rowCount;

    /**
     * The list of columns in the SAS dataset.
     *
     * @return a non-null unmodifiable list of non-null columns
     */
    @lombok.NonNull
    @lombok.Singular
    private List<SasColumn> columns;

    // Fix NetBeans bug with @lombok.Builder.Default
    public static Builder builder() {
        return new Builder()
                .name("")
                .creationTime(LocalDateTime.now())
                .lastModificationTime(LocalDateTime.now())
                .release("")
                .host("")
                .rowCount(0);
    }
}
