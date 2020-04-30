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

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A generic SAS dataset cursor that provides metadata. Rows are browsed in
 * several ways by specialized types.
 *
 * @apiNote This cursor is <u>not</u> thread-safe since it is mutable to allow
 * iteration through the content.<br>Furthermore, it might hold some resources
 * opened so it is advised to close it after use.
 *
 * @see SasForwardCursor
 * @see SasScrollableCursor
 * @see SasSplittableCursor
 *
 * @author Philippe Charles
 */
//@NotThreadSafe
public interface SasCursor extends Closeable {

    /**
     * Returns the metadata.
     *
     * @return a non-null metadata
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    SasMetaData getMetaData() throws IOException;

    /**
     * Returns the columns as an ordered list.
     *
     * @return a non-null unmodifiable list of non-null columns
     * @throws IOException if an I/O exception occurred
     */
    @NonNull
    default List<SasColumn> getColumns() throws IOException {
        return getMetaData().getColumns();
    }

    /**
     * Returns the row count.
     *
     * @return a non-negative count
     * @throws IOException if an I/O exception occurred
     */
    @NonNegative
    default int getRowCount() throws IOException {
        return getMetaData().getRowCount();
    }
}
