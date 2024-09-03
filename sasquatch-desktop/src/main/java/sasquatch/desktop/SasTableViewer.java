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
package sasquatch.desktop;

import com.formdev.flatlaf.FlatLightLaf;
import ec.util.various.swing.BasicFileViewer;
import ec.util.various.swing.BasicSwingLauncher;
import ec.util.various.swing.FontAwesome;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.io.File;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public final class SasTableViewer {

    public static void main(String[] args) {

        disableDefaultConsoleLogger();
        FlatLightLaf.setup();

        new BasicSwingLauncher()
                .lookAndFeel(FlatLightLaf.class.getName())
                .title("SAS Table Viewer")
                .icons(SasTableViewer::icons)
                .content(() -> content(getFile(args)))
                .logLevel(Level.FINE)
                .launch();
    }

    private static List<Image> icons() {
        return FontAwesome.FA_DATABASE.getImages(Color.BLACK, 16, 32, 64);
    }

    private static File getFile(String[] args) {
        return args.length > 0 ? new File(args[0]) : null;
    }

    private static Component content(File file) {
        BasicFileViewer main = new BasicFileViewer();
        main.setFileHandler(new SasBasicFileHandler());
        main.setFile(file);
        return main;
    }

    private void disableDefaultConsoleLogger() {
        if (System.getProperty("java.util.logging.config.file") == null) {
            Logger global = Logger.getLogger("");
            for (Handler o : global.getHandlers()) {
                if (o instanceof ConsoleHandler) {
                    global.removeHandler(o);
                }
            }
        }
    }
}
