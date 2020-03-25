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

import sasquatch.SasColumn;
import static sasquatch.SasColumnType.*;
import sasquatch.SasFilenameFilter;
import sasquatch.SasMetaData;
import ec.util.chart.ColorScheme;
import ec.util.chart.impl.TangoColorScheme;
import ec.util.chart.swing.SwingColorSchemeSupport;
import ec.util.grid.swing.XTable;
import ec.util.table.swing.JTables;
import ec.util.various.swing.BasicFileViewer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Locale;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import sasquatch.SasColumnType;
import sasquatch.SasResultSet;
import sasquatch.Sasquatch;

/**
 *
 * @author Philippe Charles
 */
public final class SasBasicFileHandler implements BasicFileViewer.BasicFileHandler {

    private final Sasquatch reader = Sasquatch.ofServiceLoader();
    private final SasFilenameFilter filter = new SasFilenameFilter();
    private final View uniqueView = new View();

    @Override
    public Object asyncLoad(File file, BasicFileViewer.ProgressCallback progress) throws IOException {
        try (SasResultSet resultSet = reader.read(file.toPath())) {
            SasMetaData metaData = resultSet.getMetaData();
            int rowCount = metaData.getRowCount();
            int colCount = metaData.getColumns().size();
            int row = 0;
            Object[][] data = new Object[rowCount][colCount];
            while (resultSet.nextRow()) {
                for (int col = 0; col < colCount; col++) {
                    data[row][col] = resultSet.getValue(col);
                }
                row++;
                if (row % 1000 == 0) {
                    progress.setProgress(0, rowCount, row);
                }
            }
            return new Model(metaData, data);
        }
    }

    @Override
    public boolean accept(File file) {
        return filter.accept(file.getParentFile(), file.getName());
    }

    @Override
    public boolean isViewer(Component c) {
        return c instanceof View;
    }

    @Override
    public Component borrowViewer(Object data) {
        uniqueView.setModel((Model) data);
        return uniqueView;
    }

    @Override
    public void recycleViewer(Component c) {
        ((View) c).setModel(Model.EMPTY);
    }

    public ColorScheme getColorScheme() {
        return uniqueView.getColorScheme();
    }

    public void setColorScheme(ColorScheme colorScheme) {
        uniqueView.setColorScheme(colorScheme);
    }

    @lombok.AllArgsConstructor
    private static class Model {

        public static final Model EMPTY = new Model(SasMetaData.builder().name("EMPTY").build(), new Object[0][0]);

        final SasMetaData metaData;
        final Object[][] data;
    }

    private static class View extends JPanel {

        private final XTable headerPanel;
        private final XTable columnsPanel;
        private final XTable dataPanel;
        private final ColorScheme colorScheme;

        public View() {
            this.headerPanel = new XTable();
            this.columnsPanel = new XTable();
            this.dataPanel = new XTable();
            this.colorScheme = new TangoColorScheme();

            setColorScheme(colorScheme);
            columnsPanel.setOddBackground(null);
            dataPanel.setOddBackground(null);
            dataPanel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.add("Header", new JScrollPane(headerPanel));
            tabbedPane.add("Columns", new JScrollPane(columnsPanel));
            tabbedPane.add("Data", new JScrollPane(dataPanel));
            setLayout(new BorderLayout());
            add(tabbedPane, BorderLayout.CENTER);
        }

        final public ColorScheme getColorScheme() {
            return colorScheme;
        }

        final public void setColorScheme(ColorScheme colorScheme) {
            SwingColorSchemeSupport colorSchemeSupport = SwingColorSchemeSupport.from(colorScheme);
            columnsPanel.setDefaultRenderer(Object.class, new ColumnsCellRenderer(colorSchemeSupport));
            dataPanel.setDefaultRenderer(Double.class, new NumberCellRenderer(colorSchemeSupport));
            dataPanel.setDefaultRenderer(String.class, new StringCellRenderer(colorSchemeSupport));
            dataPanel.setDefaultRenderer(Long.class, new TimeInMillisCellRenderer(colorSchemeSupport));
        }

        final public void setModel(Model model) {
            headerPanel.setModel(new HeaderModel(model.metaData));
            columnsPanel.setModel(new ColumnsModel(model.metaData.getColumns()));
            JTables.setWidthAsPercentages(columnsPanel, .05, .2, .1, .1, .1, .25);
            dataPanel.setModel(new DataModel(model.metaData.getColumns(), model.data));
        }

        private static class HeaderModel extends DefaultTableModel {

            public HeaderModel(SasMetaData header) {
                DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                setDataVector(new Object[][]{
                    {"Dataset name", header.getName()},
                    {"Created", format.format(header.getCreationTime())},
                    {"Modified", format.format(header.getLastModificationTime())},
                    {"Release", header.getRelease()},
                    {"Host", header.getHost()},
                    {"Row count", header.getRowCount()}},
                        new String[]{"Name", "Value"});
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        }

        @lombok.RequiredArgsConstructor
        private static class ColumnsModel extends AbstractTableModel {

            private final String[] columnNames = {"#", "Name", "Type", "Length", "Format", "Label"};
            private final Class<?>[] columnClasses = {int.class, String.class, SasColumnType.class, int.class, String.class, String.class};
            private final List<SasColumn> columns;

            @Override
            public int getRowCount() {
                return columns.size();
            }

            @Override
            public int getColumnCount() {
                return columnNames.length;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                SasColumn column = columns.get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return column.getOrder() + 1;
                    case 1:
                        return column.getName();
                    case 2:
                        return column.getType();
                    case 3:
                        return column.getLength();
                    case 4:
                        return column.getFormat();
                    case 5:
                        return column.getLabel();
                }
                return null;
            }

            @Override
            public String getColumnName(int column) {
                return columnNames[column];
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnClasses[columnIndex];
            }
        }

        @lombok.RequiredArgsConstructor
        private static class DataModel extends AbstractTableModel {

            final List<SasColumn> columns;
            final Object[][] data;

            @Override
            public int getRowCount() {
                return data.length;
            }

            @Override
            public int getColumnCount() {
                return columns.size();
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return data[rowIndex][columnIndex];
            }

            @Override
            public String getColumnName(int column) {
                return columns.get(column).getName();
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                SasColumn column = columns.get(columnIndex);
                switch (column.getType()) {
                    case CHARACTER:
                        return String.class;
                    case NUMERIC:
                        return Double.class;
                    case DATE:
                    case DATETIME:
                    case TIME:
                        return Long.class;
                }
                return super.getColumnClass(columnIndex);
            }
        }

        private static class DefaultTableCellRenderer2 extends DefaultTableCellRenderer {

            protected final JToolTip toolTip;

            public DefaultTableCellRenderer2() {
                this.toolTip = super.createToolTip();
            }

            @Override
            public JToolTip createToolTip() {
                toolTip.setForeground(getBackground());
                toolTip.setBackground(getForeground());
                return toolTip;
            }

            protected void updateToopTipText(JTable table, int row, int column) {
                setToolTipText("<html><b>" + table.getColumnName(column) + "</b> #" + (row + 1) + "<br>" + getText());
            }
        }

        private static class ColumnsCellRenderer extends DefaultTableCellRenderer2 {

            protected final Color backColor;
            protected final Color dateColor;
            protected final Color stringColor;
            protected final Color numberColor;

            public ColumnsCellRenderer(SwingColorSchemeSupport colorSchemeSupport) {
                this.backColor = colorSchemeSupport.getPlotColor();
                this.dateColor = colorSchemeSupport.getLineColor(ColorScheme.KnownColor.RED);
                this.stringColor = colorSchemeSupport.getLineColor(ColorScheme.KnownColor.BLUE);
                this.numberColor = colorSchemeSupport.getLineColor(ColorScheme.KnownColor.GREEN);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                ColumnsModel model = (ColumnsModel) table.getModel();
                SasColumn col = model.columns.get(row);

                Color foreColor = null;
                switch (col.getType()) {
                    case CHARACTER:
                        foreColor = stringColor;
                        break;
                    case NUMERIC:
                        foreColor = numberColor;
                        break;
                    case DATE:
                    case DATETIME:
                    case TIME:
                        foreColor = dateColor;
                        break;
                }

                if (value instanceof Number) {
                    setHorizontalAlignment(SwingConstants.TRAILING);
                } else {
                    setHorizontalAlignment(SwingConstants.LEADING);
                }

                if (!isSelected) {
                    setForeground(foreColor);
                    setBackground(backColor);
                } else {
                    setForeground(backColor);
                    setBackground(foreColor);
                }

                updateToopTipText(table, row, column);
                return this;
            }
        }

        private static abstract class ValueCellRenderer extends DefaultTableCellRenderer2 {

            protected final Color foreColor;
            protected final Color backColor;

            public ValueCellRenderer(SwingColorSchemeSupport colorSchemeSupport, ColorScheme.KnownColor knownColor) {
                this.foreColor = colorSchemeSupport.getLineColor(knownColor);
                this.backColor = colorSchemeSupport.getPlotColor();
                setOpaque(true);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setForeground(foreColor);
                    setBackground(backColor);
                } else {
                    setForeground(backColor);
                    setBackground(foreColor);
                }
                setText(getText(table, value, isSelected, hasFocus, row, column));
                updateToopTipText(table, row, column);
                return this;
            }

            protected abstract String getText(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column);
        }

        private static class TimeInMillisCellRenderer extends ValueCellRenderer {

            final DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            final DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            final DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm:ss");

            public TimeInMillisCellRenderer(SwingColorSchemeSupport colorSchemeSupport) {
                super(colorSchemeSupport, ColorScheme.KnownColor.RED);
                setHorizontalAlignment(SwingConstants.TRAILING);
            }

            @Override
            protected String getText(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                SasColumn col = ((DataModel) table.getModel()).columns.get(column);
                switch (col.getType()) {
                    case DATE:
                        return date.format((Temporal) value);
                    case DATETIME:
                        return dateTime.format((Temporal) value);
                    case TIME:
                        return time.format((Temporal) value);
                }
                return "";
            }
        }

        private static class NumberCellRenderer extends ValueCellRenderer {

            final DecimalFormat numeric = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.ROOT));

            public NumberCellRenderer(SwingColorSchemeSupport colorSchemeSupport) {
                super(colorSchemeSupport, ColorScheme.KnownColor.GREEN);
                setHorizontalAlignment(SwingConstants.TRAILING);
            }

            @Override
            protected String getText(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if ((value instanceof Double && ((Double) value).isNaN())) {
                    return ".";
                }
                return numeric.format((Double) value);
            }
        }

        private static class StringCellRenderer extends ValueCellRenderer {

            public StringCellRenderer(SwingColorSchemeSupport colorSchemeSupport) {
                super(colorSchemeSupport, ColorScheme.KnownColor.BLUE);
                setHorizontalAlignment(SwingConstants.LEADING);
            }

            @Override
            protected String getText(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return (String) value;
            }
        }
    }
}
