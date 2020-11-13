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
package internal.ri.data.rows;

import internal.bytes.BytesReader;
import internal.ri.base.PageCursor;
import internal.ri.base.PageHeader;

import java.io.IOException;

/**
 * @author Philippe Charles
 */
abstract class ForwardingCursor implements RowCursor {

    @Override
    public boolean next() throws IOException {
        do {
            if (!moveToNextRow()) {
                return false;
            }
        } while (isDeleted());
        return true;
    }

    private boolean moveToNextRow() throws IOException {
        if (hasNextRow()) {
            if (hasNextRowInCurrentPage()) {
                moveToNextRowInCurrentPage();
            } else {
                moveToFirstRowInNextPage();
            }
            return true;
        }
        return false;
    }

    abstract protected boolean hasNextRow() throws IOException;

    abstract protected boolean hasNextRowInCurrentPage() throws IOException;

    abstract protected void moveToNextRowInCurrentPage() throws IOException;

    abstract protected void moveToFirstRowInNextPage() throws IOException;

    abstract protected boolean isDeleted() throws IOException;

    interface HasData {

        boolean hasData(BytesReader pageBytes, PageHeader page, boolean u64);
    }

    static PageHeader nextPageWithData(PageCursor pageCursor, boolean u64, HasData hasData) throws IOException {
        while (pageCursor.next()) {
            BytesReader pageData = pageCursor.getBytes();
            PageHeader page = PageHeader.parse(pageData, u64, pageCursor.getIndex());
            if (hasData.hasData(pageData, page, u64)) {
                return page;
            }
        }
        throw new IOException("No data page found");
    }
}
