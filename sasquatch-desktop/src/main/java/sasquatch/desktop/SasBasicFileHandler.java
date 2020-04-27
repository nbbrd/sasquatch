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

import ec.util.chart.ColorScheme;
import ec.util.various.swing.BasicFileViewer;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import sasquatch.SasForwardCursor;
import sasquatch.Sasquatch;
import sasquatch.util.SasFilenameFilter;

/**
 *
 * @author Philippe Charles
 */
public final class SasBasicFileHandler implements BasicFileViewer.BasicFileHandler {

    private final Sasquatch reader = Sasquatch.ofServiceLoader();
    private final SasFilenameFilter filter = new SasFilenameFilter();
    private final SasBasicFileView uniqueView = new SasBasicFileView();

    @Override
    public Object asyncLoad(File file, BasicFileViewer.ProgressCallback progress) throws IOException {
        try ( SasForwardCursor cursor = reader.readForward(file.toPath())) {
            int rowCount = cursor.getRowCount();
            int colCount = cursor.getColumns().size();
            int row = 0;
            Object[][] data = new Object[rowCount][colCount];
            while (cursor.next()) {
                for (int col = 0; col < colCount; col++) {
                    data[row][col] = cursor.getValue(col);
                }
                row++;
                if (row % 1000 == 0) {
                    progress.setProgress(0, rowCount, row);
                }
            }
            return new SasBasicFileModel(cursor.getMetaData(), data);
        }
    }

    @Override
    public boolean accept(File file) {
        return filter.accept(file.getParentFile(), file.getName());
    }

    @Override
    public boolean isViewer(Component c) {
        return c instanceof SasBasicFileView;
    }

    @Override
    public Component borrowViewer(Object data) {
        uniqueView.setModel((SasBasicFileModel) data);
        return uniqueView;
    }

    @Override
    public void recycleViewer(Component c) {
        ((SasBasicFileView) c).setModel(SasBasicFileModel.EMPTY);
    }

    public ColorScheme getColorScheme() {
        return uniqueView.getColorScheme();
    }

    public void setColorScheme(ColorScheme colorScheme) {
        uniqueView.setColorScheme(colorScheme);
    }
}
