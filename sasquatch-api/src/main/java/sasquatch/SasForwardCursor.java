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

import java.io.IOException;

/**
 * A SAS dataset cursor that browses rows in forward-only mode from first to
 * last.
 *
 * @apiNote This cursor is <u>not</u> thread-safe since it is mutable to allow
 * iteration through the content.<br>Furthermore, it might hold some resources
 * opened so it is advised to close it after use.
 *
 * @author Philippe Charles
 */
public interface SasForwardCursor extends SasCursor, SasRow {

    /**
     * Moves to the next row.
     *
     * @return true if there is a next row; false otherwise
     * @throws IOException if an I/O exception occurred
     */
    boolean next() throws IOException;
}
