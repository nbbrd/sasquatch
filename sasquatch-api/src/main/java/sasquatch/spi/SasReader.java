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
 * A thread-safe reader used to read SAS datasets (*.sas7bdat).
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
     * @return a non-null name
     */
    @NonNull
    String getName();

    @ServiceFilter
    boolean isAvailable();

    @ServiceSorter
    @NonNegative
    int getCost();

    @NonNull
    Set<SasFeature> getFeatures();

    /**
     * Read a SAS dataset into a forward cursor.
     * <p>
     * The result set might hold some resources opened so it is advised to call
     * the close method after use.
     * <br>The cursor is <u>not</u> thread-safe.
     *
     * @param file the SAS dataset to read
     * @return a non-null cursor
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    SasForwardCursor readForward(@NonNull Path file) throws IOException;

    /**
     * Read a SAS dataset into a scrollable cursor.
     * <p>
     * The result set might hold some resources opened so it is advised to call
     * the close method after use.
     * <br>The cursor is <u>not</u> thread-safe.
     *
     * @param file the SAS dataset to read
     * @return a non-null cursor
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    SasScrollableCursor readScrollable(@NonNull Path file) throws IOException;

    /**
     * Read a SAS dataset into a splittable cursor.
     * <p>
     * The result set might hold some resources opened so it is advised to call
     * the close method after use.
     * <br>The cursor is <u>not</u> thread-safe.
     *
     * @param file the SAS dataset to read
     * @return a non-null cursor
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    SasSplittableCursor readSplittable(@NonNull Path file) throws IOException;

    /**
     * Read the metadata of a SAS dataset.
     * <p>
     * Note that this same metadata can also be obtained by using the read
     * method. <br>This is a shortcut when you don't need data (the resources
     * are automatically released).
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
