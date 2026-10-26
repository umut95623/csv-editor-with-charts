import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;

public class Main extends JFrame {

    private JTable dataTable;
    private DefaultTableModel tableModel;
    private ChartCanvas canvasArea;
    private String currentChartMode = "NONE";

    public Main() {
        setTitle("Pizza Shop - Data Visualization CSV Editor");
        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(220, 180, 100));
        controlPanel.setPreferredSize(new Dimension(220, 0));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel panelTitle = new JLabel("Pizza Shop Dashboard");
        panelTitle.setForeground(Color.white);
        panelTitle.setFont(new Font("Arial", Font.BOLD, 17));
        panelTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(panelTitle);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        addSidebarButton(controlPanel, "Load CSV", e -> loadCSV());
        addSidebarButton(controlPanel, "Save as CSV", e -> saveCSV());
        controlPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        addSidebarButton(controlPanel, "Add Row", e -> addRow());
        addSidebarButton(controlPanel, "Delete Row", e -> deleteRow());
        addSidebarButton(controlPanel, "Add Column", e -> addColumn());
        addSidebarButton(controlPanel, "Delete Column", e -> deleteColumn());
        controlPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        addSidebarButton(controlPanel, "Bar / Line Chart", e -> { currentChartMode = "BAR_LINE"; canvasArea.repaint(); });
        addSidebarButton(controlPanel, "Pie Chart", e -> { currentChartMode = "PIE"; canvasArea.repaint(); });
        addSidebarButton(controlPanel, "Heatmap", e -> { currentChartMode = "HEATMAP"; canvasArea.repaint(); });

        JPanel mainContentArea = new JPanel(new GridLayout(2, 1));
        mainContentArea.setBackground(new Color(250, 240, 230));

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBorder(BorderFactory.createTitledBorder("Financial Statistics Table"));
        tableContainer.setBackground(new Color(250, 240, 230));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        dataTable.setRowHeight(25);

        JTableHeader tableHeader = dataTable.getTableHeader();
        tableHeader.setBackground(new Color(255, 190, 7));
        tableHeader.setFont(new Font("Arial", Font.BOLD, 12));
        JScrollPane scrollPane = new JScrollPane(dataTable);
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        mainContentArea.add(tableContainer);

        canvasArea = new ChartCanvas();
        canvasArea.setBorder(BorderFactory.createTitledBorder("Graph Canvas"));
        mainContentArea.add(canvasArea);
        add(controlPanel, BorderLayout.WEST);
        add(mainContentArea, BorderLayout.CENTER);
    }

    private void addSidebarButton(JPanel panel, String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 35));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(120, 90, 50));
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        if (action != null) button.addActionListener(action);
        panel.add(button);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private void loadCSV() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                tableModel.setRowCount(0);
                tableModel.setColumnCount(0);

                String headerLine = br.readLine();
                if (headerLine != null) {
                    for (String h : headerLine.split(",")) tableModel.addColumn(h.trim());
                }
                String dataLine;
                while ((dataLine = br.readLine()) != null) tableModel.addRow(dataLine.split(","));
                dataTable.getTableHeader().revalidate();
                JOptionPane.showMessageDialog(this, "Data loaded successfully.");
                canvasArea.repaint();
            } catch (IOException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void saveCSV() {
        if (tableModel.getColumnCount() == 0) {
            JOptionPane.showMessageDialog(this, "There is no data to save.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) file = new File(file.getParentFile(), file.getName() + ".csv");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    bw.write(tableModel.getColumnName(i) + (i < tableModel.getColumnCount() - 1 ? "," : ""));
                }
                bw.newLine();
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object val = tableModel.getValueAt(i, j);
                        bw.write((val != null ? val.toString() : "") + (j < tableModel.getColumnCount() - 1 ? "," : ""));
                    }
                    bw.newLine();
                }
                JOptionPane.showMessageDialog(this, "File saved successfully.");
            } catch (IOException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void addRow() {
        if (tableModel.getColumnCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please add at least one column using \"Add Column\" or upload a CSV file first.", "No data in table", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object[] row = new Object[tableModel.getColumnCount()];
        java.util.Arrays.fill(row, "-");
        tableModel.addRow(row);
    }

    private void deleteRow() {
        int r = dataTable.getSelectedRow();
        if (r != -1) tableModel.removeRow(r);
    }

    private void addColumn() {
        String name = JOptionPane.showInputDialog(this, "Column name:");
        if (name != null && !name.trim().isEmpty()) tableModel.addColumn(name);
    }

    private void deleteColumn() {
        int c = dataTable.getSelectedColumn();
        if (c != -1) {
            java.util.Vector<String> headers = new java.util.Vector<>();
            java.util.Vector<java.util.Vector<Object>> data = new java.util.Vector<>();
            for (int i = 0; i < tableModel.getColumnCount(); i++) if (i != c) headers.add(tableModel.getColumnName(i));
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                java.util.Vector<Object> row = new java.util.Vector<>();
                for (int j = 0; j < tableModel.getColumnCount(); j++) if (j != c) row.add(tableModel.getValueAt(i, j));
                data.add(row);
            }
            tableModel.setDataVector(data, headers);
        }
    }

    private class ChartCanvas extends JPanel {
        public ChartCanvas() {
            setBackground(new Color(255, 250, 240));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (currentChartMode.equals("NONE")) {
                return;
            }

            if (tableModel.getRowCount() == 0 || tableModel.getColumnCount() < 2) {
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                g2.setColor(Color.RED);
                g2.drawString("Unable to generate chart: Please upload a CSV file with valid data first.", 50, 50);
                return;
            }

            if (currentChartMode.equals("BAR_LINE")) {
                drawBarLineChart(g2);
            } else if (currentChartMode.equals("PIE")) {
                drawPieChart(g2);
            } else if (currentChartMode.equals("HEATMAP")) {
                drawHeatmapChart(g2);
            }
        }

        private void drawBarLineChart(Graphics2D g2) {
            int rowCount = tableModel.getRowCount();
            double[] values = new double[rowCount];
            String[] labels = new String[rowCount];
            double maxVal = 0;

            for (int i = 0; i < rowCount; i++) {
                labels[i] = tableModel.getValueAt(i, 0).toString();
                try {
                    values[i] = Double.parseDouble(tableModel.getValueAt(i, 1).toString());
                    if (values[i] > maxVal) maxVal = values[i];
                } catch (NumberFormatException e) { values[i] = 0; }
            }
            if (maxVal == 0) maxVal = 1.0;

            int padding = 70;
            int width = getWidth() - (2 * padding);
            int height = getHeight() - (2 * padding);
            int originX = padding;
            int originY = getHeight() - padding;

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(originX, originY, originX + width, originY);
            g2.drawLine(originX, originY, originX, padding);

            String xAxisLabel = tableModel.getColumnName(0);
            String yAxisLabel = tableModel.getColumnName(1);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.setColor(new Color(60, 40, 35));
            FontMetrics fm = g2.getFontMetrics();

            int xLabelWidth = fm.stringWidth(xAxisLabel);
            g2.drawString(xAxisLabel, originX + (width / 2) - (xLabelWidth / 2), originY + 45);

            java.awt.geom.AffineTransform originalTransform = g2.getTransform();
            g2.rotate(-Math.PI / 2);
            int yLabelWidth = fm.stringWidth(yAxisLabel);
            g2.drawString(yAxisLabel, -originY + (height / 2) - (yLabelWidth / 2), originX - 45);
            g2.setTransform(originalTransform);

            int xInterval = width / rowCount;
            int barWidth = xInterval / 2;
            int[] lineX = new int[rowCount];
            int[] lineY = new int[rowCount];

            for (int i = 0; i < rowCount; i++) {
                int x = originX + (i * xInterval) + (xInterval / 4);
                int barHeight = (int) ((values[i] / maxVal) * height);
                int y = originY - barHeight;

                lineX[i] = x + (barWidth / 2);
                lineY[i] = y;

                g2.setColor(new Color(220, 70, 20));
                g2.fillRect(x, y, barWidth, barHeight);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, barWidth, barHeight);

                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.drawString(String.format("%.0f", values[i]), x, y - 8);
                g2.drawString(labels[i], x + (barWidth / 4), originY + 20);
            }

            g2.setColor(new Color(255, 190, 7));
            g2.setStroke(new BasicStroke(3f));
            for (int i = 0; i < rowCount - 1; i++) g2.drawLine(lineX[i], lineY[i], lineX[i+1], lineY[i+1]);

            g2.setColor(Color.DARK_GRAY);
            for (int i = 0; i < rowCount; i++) g2.fillOval(lineX[i] - 4, lineY[i] - 4, 8, 8);
        }

        private void drawPieChart(Graphics2D g2) {
            int rowCount = tableModel.getRowCount();

            String chartTitle = "Monthly revenues shown as pie slices";
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.setColor(new Color(60, 40, 35));
            int titleWidth = g2.getFontMetrics().stringWidth(chartTitle);
            g2.drawString(chartTitle, (getWidth() - titleWidth) / 2, 30);

            double[] values = new double[rowCount];
            String[] labels = new String[rowCount];
            double total = 0;

            for (int i = 0; i < rowCount; i++) {
                labels[i] = tableModel.getValueAt(i, 0).toString();
                try {
                    values[i] = Double.parseDouble(tableModel.getValueAt(i, 1).toString());
                    total += values[i];
                } catch (NumberFormatException e) { values[i] = 0; }
            }
            if (total == 0) return;

            int padding = 60;
            int diameter = Math.min(getWidth(), getHeight()) - (2 * padding);
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2 + 15;

            int centerX = x + (diameter / 2);
            int centerY = y + (diameter / 2);
            int radius = diameter / 2;

            Color[] colors = {
                    new Color(220, 70, 20), new Color(255, 190, 7), new Color(141, 110, 99),
                    new Color(56, 142, 60), new Color(255, 152, 0), new Color(121, 85, 72)
            };

            double[] midAngles = new double[rowCount];
            int startAngle = 0;
            for (int i = 0; i < rowCount; i++) {
                int arcAngle = (int) Math.round((values[i] / total) * 360.0);

                g2.setColor(colors[i % colors.length]);
                g2.fillArc(x, y, diameter, diameter, startAngle, arcAngle);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawArc(x, y, diameter, diameter, startAngle, arcAngle);

                midAngles[i] = startAngle + (arcAngle / 2.0);
                startAngle += arcAngle;
            }

            g2.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            double labelRadius = radius * 0.7;
            for (int i = 0; i < rowCount; i++) {
                double midAngleRadians = Math.toRadians(midAngles[i]);
                int labelX = (int) (centerX + labelRadius * Math.cos(midAngleRadians));
                int labelY = (int) (centerY - labelRadius * Math.sin(midAngleRadians));

                String labelText = labels[i] + " (" + String.format("%.0f", values[i]) + ")";
                int textWidth = fm.stringWidth(labelText);

                g2.setColor(new Color(255, 255, 255, 180));
                g2.fillRect(labelX - (textWidth / 2) - 2, labelY - 10, textWidth + 4, 14);

                g2.setColor(Color.BLACK);
                g2.drawString(labelText, labelX - (textWidth / 2), labelY);
            }
        }

        private void drawHeatmapChart(Graphics2D g2) {
            int rowCount = tableModel.getRowCount();
            int colCount = tableModel.getColumnCount();

            double[][] dataMatrix = new double[rowCount][colCount - 1];
            double minVal = Double.MAX_VALUE;
            double maxVal = Double.MIN_VALUE;

            for (int i = 0; i < rowCount; i++) {
                for (int j = 1; j < colCount; j++) {
                    try {
                        double val = Double.parseDouble(tableModel.getValueAt(i, j).toString());
                        dataMatrix[i][j - 1] = val;
                        if (val < minVal) minVal = val;
                        if (val > maxVal) maxVal = val;
                    } catch (NumberFormatException e) { dataMatrix[i][j - 1] = 0; if (0 < minVal) minVal = 0; }
                }
            }
            if (maxVal == minVal) maxVal = minVal + 1;

            String title = "Monthly Financial Heatmap";
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.setColor(new Color(60, 40, 35));
            int titleWidth = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, (getWidth() - titleWidth) / 2, 25);

            int paddingX = 80; int paddingY = 70;
            int width = getWidth() - paddingX - 40; int height = getHeight() - paddingY - 40;
            int cellWidth = width / (colCount - 1); int cellHeight = height / rowCount;

            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();

            for (int i = 0; i < rowCount; i++) {
                String rowLabel = tableModel.getValueAt(i, 0).toString();
                g2.setColor(Color.BLACK);
                g2.drawString(rowLabel, paddingX - fm.stringWidth(rowLabel) - 10, paddingY + (i * cellHeight) + (cellHeight / 2) + 5);

                for (int j = 0; j < colCount - 1; j++) {
                    if (i == 0) {
                        String colLabel = tableModel.getColumnName(j + 1);
                        int labelX = paddingX + (j * cellWidth) + (cellWidth / 2) - (fm.stringWidth(colLabel) / 2);

                        g2.setColor(new Color(60, 40, 35));
                        g2.drawString(colLabel, labelX, paddingY - 15);
                    }

                    double val = dataMatrix[i][j];
                    double ratio = (val - minVal) / (maxVal - minVal);

                    int rL = 255, gL = 245, bL = 180;
                    int rH = 216, gH = 67,  bH = 21;
                    int rC = (int) (rL + ratio * (rH - rL));
                    int gC = (int) (gL + ratio * (gH - gL));
                    int bC = (int) (bL + ratio * (bH - bL));

                    g2.setColor(new Color(rC, gC, bC));
                    int rectX = paddingX + (j * cellWidth); int rectY = paddingY + (i * cellHeight);
                    g2.fillRect(rectX, rectY, cellWidth, cellHeight);

                    g2.setColor(Color.WHITE);
                    g2.drawRect(rectX, rectY, cellWidth, cellHeight);

                    g2.setColor(ratio > 0.5 ? Color.WHITE : Color.BLACK);
                    String valStr = String.format("%.0f", val);
                    g2.drawString(valStr, rectX + (cellWidth / 2) - (fm.stringWidth(valStr) / 2), rectY + (cellHeight / 2) + 5);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}