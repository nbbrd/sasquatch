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
package sasquatch.desktop;

import ec.util.chart.ColorScheme;
import ec.util.chart.impl.TangoColorScheme;
import ec.util.chart.swing.SwingColorSchemeSupport;
import ec.util.grid.swing.XTable;
import ec.util.table.swing.JTables;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import sasquatch.SasColumn;
import sasquatch.SasColumnType;
import sasquatch.SasMetaData;

/**
 *
 * @author Philippe Charles
 */
final class SasBasicFileView extends JPanel {

    public static final String COLOR_SCHEME_PROPERTY = "colorScheme";
    public static final String MODEL_PROPERTY = "model";

    private final XTable headerPanel;
    private final XTable columnsPanel;
    private final XTable dataPanel;
    private final JTabbedPane tabbedPane;

    private ColorScheme colorScheme;
    private SasBasicFileModel model;

    public SasBasicFileView() {
        this.headerPanel = new XTable();
        this.columnsPanel = new XTable();
        this.dataPanel = new XTable();
        this.tabbedPane = new JTabbedPane();
        this.colorScheme = new TangoColorScheme();
        this.model = SasBasicFileModel.EMPTY;
        initComponents();
    }

    private void initComponents() {
        setColorScheme(colorScheme);

        columnsPanel.setOddBackground(null);
        dataPanel.setOddBackground(null);
        dataPanel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tabbedPane.add("Header", new JScrollPane(headerPanel));
        tabbedPane.add("Columns", new JScrollPane(columnsPanel));
        tabbedPane.add("Data", new JScrollPane(dataPanel));

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        onColorSchemeChange();
        onModelChange();

        addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case COLOR_SCHEME_PROPERTY:
                    onColorSchemeChange();
                    break;
                case MODEL_PROPERTY:
                    onModelChange();
                    break;
            }
        });
    }

    private void onColorSchemeChange() {
        Colors colors = new Colors(SwingColorSchemeSupport.from(colorScheme));
        columnsPanel.setDefaultRenderer(Object.class, new ColumnsCellRenderer(colors));
        dataPanel.setDefaultRenderer(Object.class, new ValueCellRenderer(colors));
    }

    private void onModelChange() {
        headerPanel.setModel(new HeaderModel(model.getMetaData()));
        columnsPanel.setModel(new ColumnsModel(model.getMetaData().getColumns()));
        JTables.setWidthAsPercentages(columnsPanel, .05, .2, .1, .1, .15, .40);
        dataPanel.setModel(new DataModel(model.getMetaData().getColumns(), model.getData()));
    }

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(ColorScheme colorScheme) {
        ColorScheme old = this.colorScheme;
        this.colorScheme = Objects.requireNonNull(colorScheme);
        firePropertyChange(COLOR_SCHEME_PROPERTY, old, this.colorScheme);
    }

    public SasBasicFileModel getModel() {
        return model;
    }

    public void setModel(SasBasicFileModel model) {
        SasBasicFileModel old = this.model;
        this.model = Objects.requireNonNull(model);
        firePropertyChange(MODEL_PROPERTY, old, this.model);
    }
    //</editor-fold>

    private static class HeaderModel extends DefaultTableModel {

        public HeaderModel(SasMetaData header) {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            setDataVector(new Object[][]{
                {"Dataset name", header.getName()},
                {"Dataset label", header.getLabel()},
                {"Created", format.format(header.getCreationTime())},
                {"Modified", format.format(header.getLastModificationTime())},
                {"Release", header.getRelease()},
                {"Host", header.getHost()},
                {"Row count", header.getRowCount()}
            }, new String[]{"Name", "Value"});
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

        public SasColumnType getRowType(int rowIndex) {
            return columns.get(rowIndex).getType();
        }

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
                    return column.getFormat().toString();
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

        public SasColumnType getColumnType(int columnIndex) {
            return columns.get(columnIndex).getType();
        }

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

    @lombok.AllArgsConstructor
    private static final class Colors {

        @lombok.NonNull
        private final SwingColorSchemeSupport colorScheme;

        public Color getBackColor() {
            return colorScheme.getPlotColor();
        }

        public Color getForecolor(SasColumnType type) {
            switch (type) {
                case CHARACTER:
                    return colorScheme.getLineColor(ColorScheme.KnownColor.BLUE);
                case NUMERIC:
                    return colorScheme.getLineColor(ColorScheme.KnownColor.GREEN);
                case DATE:
                    return colorScheme.getLineColor(ColorScheme.KnownColor.RED);
                case DATETIME:
                    return colorScheme.getLineColor(ColorScheme.KnownColor.RED);
                case TIME:
                    return colorScheme.getLineColor(ColorScheme.KnownColor.RED);
            }
            return null;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class ColumnsCellRenderer extends DefaultTableCellRenderer2 {

        private final Colors colors;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            SasColumnType type = ((ColumnsModel) table.getModel()).getRowType(row);

            if (value instanceof Number) {
                setHorizontalAlignment(SwingConstants.TRAILING);
            } else {
                setHorizontalAlignment(SwingConstants.LEADING);
            }

            if (!isSelected) {
                setForeground(colors.getForecolor(type));
                setBackground(colors.getBackColor());
            } else {
                setForeground(colors.getBackColor());
                setBackground(colors.getForecolor(type));
            }

            updateToopTipText(table, row, column);

            return this;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class ValueCellRenderer extends DefaultTableCellRenderer2 {

        private final DecimalFormat numeric = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.ROOT));
        private final DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private final DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        private final DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm:ss");

        private final Colors colors;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            SasColumnType type = ((DataModel) table.getModel()).getColumnType(column);

            switch (type) {
                case CHARACTER:
                    setText((String) value);
                    setHorizontalAlignment(SwingConstants.LEADING);
                    break;
                case NUMERIC:
                    setText(((Double) value).isNaN() ? "." : numeric.format(value));
                    setHorizontalAlignment(SwingConstants.TRAILING);
                    break;
                case DATE:
                    setText(date.format((Temporal) value));
                    setHorizontalAlignment(SwingConstants.TRAILING);
                    break;
                case DATETIME:
                    setText(dateTime.format((Temporal) value));
                    setHorizontalAlignment(SwingConstants.TRAILING);
                    break;
                case TIME:
                    setText(time.format((Temporal) value));
                    setHorizontalAlignment(SwingConstants.TRAILING);
                    break;
            }

            if (!isSelected) {
                setForeground(colors.getForecolor(type));
                setBackground(colors.getBackColor());
            } else {
                setForeground(colors.getBackColor());
                setBackground(colors.getForecolor(type));
            }

            updateToopTipText(table, row, column);

            return this;
        }
    }
}
