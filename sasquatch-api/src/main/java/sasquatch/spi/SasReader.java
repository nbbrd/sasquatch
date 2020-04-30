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
package sasquatch.spi;

import internal.sasquatch.spi.FailsafeReader;
import sasquatch.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceFilter;
import nbbrd.service.ServiceSorter;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A reader of SAS dataset. This class is part of SPI, do not use it directly.
 *
 * @implNote This class must be thread-safe.
 *
 * @author Philippe Charles
 */
//@ThreadSafe
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        wrapper = FailsafeReader.class
)
public interface SasReader {

    /**
     * The name of the reader.
     *
     * @return a non-null not-empty name
     */
    @NonNull
    String getName();

    /**
     * Checks if the reader is available or not. This check is used to filter
     * implementations at runtime.
     *
     * @return true if available, false otherwise
     */
    @ServiceFilter
    boolean isAvailable();

    /**
     * Gets the cost of using this reader (the lower the better). This cost is
     * used to sort implementations at runtime.
     *
     * @return a non-negative value
     */
    @ServiceSorter
    @NonNegative
    int getCost();

    /**
     * Gets the supported features of this reader.
     *
     * @return a non-null set of features
     */
    @NonNull
    Set<SasFeature> getFeatures();

    /**
     * Reads a SAS dataset into a forward cursor.
     *
     * @param file the SAS dataset to read
     * @return a non-null cursor
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    SasForwardCursor readForward(@NonNull Path file) throws IOException;

    /**
     * Reads a SAS dataset into a scrollable cursor.
     *
     * @param file the SAS dataset to read
     * @return a non-null cursor
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    SasScrollableCursor readScrollable(@NonNull Path file) throws IOException;

    /**
     * Reads a SAS dataset into a splittable cursor.
     *
     * @param file the SAS dataset to read
     * @return a non-null cursor
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    SasSplittableCursor readSplittable(@NonNull Path file) throws IOException;

    /**
     * Reads the metadata of a SAS dataset.
     *
     * @param file the SAS dataset to read
     * @return a non-null metadata
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    SasMetaData readMetaData(@NonNull Path file) throws IOException;

    public static final int ADVANCED_SUPPORT = 100;
    public static final int BASIC_SUPPORT = 1000;
}
