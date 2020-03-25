/*
 * Copyright 2018 National Bank of Belgium
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
package picocli.ext;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import picocli.CommandLine.IVersionProvider;

/**
 *
 * @author Philippe Charles
 */
public final class ManifestVersionProvider implements IVersionProvider {

    @Override
    public String[] getVersion() throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(MANIFEST_PATH)) {
            Manifest manifest = new Manifest(stream);
            Attributes attr = manifest.getMainAttributes();
            return new String[]{getVersion(attr)};
        } catch (IOException | NullPointerException ex) {
            return new String[]{"Unable to read manifest: " + ex};
        }
    }

    private static final String MANIFEST_PATH = "/META-INF/MANIFEST.MF";
    private static final String IMPL_TITLE_HEADER = "Implementation-Title";
    private static final String IMPL_VERSION_HEADER = "Implementation-Version";

    private static String getVersion(Attributes attr) {
        return attr.getValue(IMPL_TITLE_HEADER) + " version \""
                + attr.getValue(IMPL_VERSION_HEADER) + "\"";
    }
}
