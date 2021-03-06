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

import java.io.IOException;

/**
 * A SAS dataset cursor that browses rows in scrollable mode by using their
 * position.
 *
 * @apiNote This cursor is <u>not</u> thread-safe since it is mutable to allow
 * iteration through the content.<br>Furthermore, it might hold some resources
 * opened so it is advised to close it after use.
 *
 * @author Philippe Charles
 */
public interface SasScrollableCursor extends SasCursor, SasRow {

    /**
     * Gets the current zero-based row position.<br>Note that this position can
     * be outside bounds <code>[0, rowCount[</code>.
     *
     * @return the current row position
     * @throws IOException if an I/O exception occurred
     */
    int getRow() throws IOException;

    /**
     * Sets the cursor to a new zero-based row position.
     *
     * @param row the zero-based row position
     * @return true if there is a row at the specified position, false otherwise
     * @throws IOException if an I/O exception occurred
     */
    boolean moveTo(int row) throws IOException;
}
