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
package sasquatch.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Locale;

/**
 * @author Philippe Charles
 */
public final class SasFilenameFilter implements FilenameFilter, FileFilter, DirectoryStream.Filter<Path> {

    @Override
    public boolean accept(File dir, String name) {
        return isValidFileName(name);
    }

    @Override
    public boolean accept(File pathname) {
        return isValidFileName(pathname.getPath());
    }

    @Override
    public boolean accept(Path entry) throws IOException {
        return isValidFileName(entry.toString());
    }

    private boolean isValidFileName(String name) {
        return name.toLowerCase(Locale.ROOT).endsWith(".sas7bdat");
    }
}
