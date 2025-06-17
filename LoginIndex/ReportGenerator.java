

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Date;
import java.util.Calendar;
import java.util.Vector;
import java.util.HashMap; // For Sales by Category/Supplier
import java.util.Map;     // For Sales by Category/Supplier
import java.util.List;    // For generic list of data points
import java.util.ArrayList; // For generic list of data points
import java.util.Comparator; // For sorting data points

public class ReportGenerator extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/memistore";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private final InventoryDashboard parentDashboard;

    // UI Components for Report Generator
    private final JPanel mainPanel;
    private final JComboBox<String> reportTypeComboBox;
    private final JComboBox<String> fixedPeriodComboBox;
    private final JTextField dateFromField; 
    private final JTextField dateToField;  
    private final JLabel totalSalesLabel;
    private final JLabel totalItemsSoldLabel;
    private final JButton generateReportButton;
    private final JButton exportPdfButton;
    private final JButton backButton;

    private final JLabel fromLabel; 
    private final JLabel toLabel;  

    public ReportGenerator(InventoryDashboard parentDashboard) {
        this.parentDashboard = parentDashboard;
        setTitle("Memi's Treats - Generate Reports");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(139, 69, 19)); // Dark brown background

        // --- Header Panel ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(67, 42, 32)); // Darker brown
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("Generate Reports", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Back Button
        backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(new Color(220, 20, 60)); // Crimson
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createLineBorder(new Color(180, 0, 40), 2));
        backButton.setPreferredSize(new Dimension(180, 35));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Set pointer cursor
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBackToDashboard();
            }
        });
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButtonPanel.setOpaque(false);
        backButtonPanel.add(backButton);
        headerPanel.add(backButtonPanel, BorderLayout.WEST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Control Panel (Top Section: Report Type, Date Range) ---
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(new Color(134, 100, 79)); // Medium brown
        controlPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Report Type Label and ComboBox
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel reportTypeLabel = new JLabel("Report type:");
        reportTypeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        reportTypeLabel.setForeground(Color.BLACK);
        controlPanel.add(reportTypeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 4; // Span across more columns for report type
        gbc.anchor = GridBagConstraints.WEST;
        reportTypeComboBox = new JComboBox<>(new String[]{"Sales by Category", "Sales over Time", "Items Sold by Supplier", "Stock Levels"});
        reportTypeComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        reportTypeComboBox.setPreferredSize(new Dimension(200, 30));
        reportTypeComboBox.setBackground(Color.WHITE);
        controlPanel.add(reportTypeComboBox, gbc);

        // Date Range Label and Fixed Period ComboBox
        gbc.gridwidth = 1; // Reset gridwidth
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel dateRangeLabel = new JLabel("Date range:");
        dateRangeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        dateRangeLabel.setForeground(Color.BLACK);
        controlPanel.add(dateRangeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 4; // Span across to the end of the current row layout
        gbc.anchor = GridBagConstraints.WEST;
        // Removed "Custom Range" option
        fixedPeriodComboBox = new JComboBox<>(new String[]{"Last 7 Days", "Last 30 Days", "This Month", "Last Month", "This Year", "All Time"});
        fixedPeriodComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        fixedPeriodComboBox.setPreferredSize(new Dimension(150, 28));
        fixedPeriodComboBox.setBackground(Color.WHITE);
        controlPanel.add(fixedPeriodComboBox, gbc);
        fixedPeriodComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        gbc.gridwidth = 1;
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        fromLabel = new JLabel("From (yyyy-MM-dd):");
        fromLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        fromLabel.setForeground(Color.BLACK);
        controlPanel.add(fromLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        dateFromField = new JTextField(10);
        dateFromField.setPreferredSize(new Dimension(120, 28));
        dateFromField.setFont(new Font("Arial", Font.PLAIN, 14));
        controlPanel.add(dateFromField, gbc);

        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        toLabel = new JLabel("To (yyyy-MM-dd):");
        toLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        toLabel.setForeground(Color.BLACK);
        controlPanel.add(toLabel, gbc);

        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        dateToField = new JTextField(10);
        dateToField.setPreferredSize(new Dimension(120, 28));
        dateToField.setFont(new Font("Arial", Font.PLAIN, 14));
        controlPanel.add(dateToField, gbc);

        // Generate Report Button
        generateReportButton = new JButton("Generate Report");
        generateReportButton.setFont(new Font("Arial", Font.BOLD, 16));
        generateReportButton.setBackground(new Color(60, 179, 113)); 
        generateReportButton.setForeground(Color.WHITE);
        generateReportButton.setFocusPainted(false);
        generateReportButton.setBorder(BorderFactory.createLineBorder(new Color(46, 139, 87), 2));
        generateReportButton.setPreferredSize(new Dimension(180, 45));
        generateReportButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Set pointer cursor
        generateReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateReport();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 10, 5);
        controlPanel.add(generateReportButton, gbc);

        // Export PDF Button
        exportPdfButton = new JButton("Export PDF");
        exportPdfButton.setFont(new Font("Arial", Font.BOLD, 16));
        exportPdfButton.setBackground(new Color(255, 165, 0)); // Orange
        exportPdfButton.setForeground(Color.WHITE);
        exportPdfButton.setFocusPainted(false);
        exportPdfButton.setBorder(BorderFactory.createLineBorder(new Color(200, 130, 0), 2));
        exportPdfButton.setPreferredSize(new Dimension(180, 45));
        exportPdfButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Set pointer cursor
        exportPdfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportPdf();
            }
        });
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        controlPanel.add(exportPdfButton, gbc);

        mainPanel.add(controlPanel, BorderLayout.CENTER);

        // --- Summary and Graph Placeholder Panel (Bottom Section) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(139, 69, 19)); // Dark brown to match main panel

        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
        summaryPanel.setBackground(new Color(67, 42, 32)); // Darker brown
        summaryPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        totalSalesLabel = new JLabel("Total Sales: P 0.00");
        totalSalesLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalSalesLabel.setForeground(Color.WHITE);
        summaryPanel.add(totalSalesLabel);

        totalItemsSoldLabel = new JLabel("Total items sold: 0");
        totalItemsSoldLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalItemsSoldLabel.setForeground(Color.WHITE);
        summaryPanel.add(totalItemsSoldLabel);

        bottomPanel.add(summaryPanel, BorderLayout.NORTH);

        // Placeholder for graph - Actual graph will be in a new dialog
        JPanel graphPlaceholderPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(134, 100, 79)); // Medium brown
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.PLAIN, 24));
                String text = "Graph will appear in a new window";
                FontMetrics fm = g.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(text, x, y);
            }
        };
        graphPlaceholderPanel.setPreferredSize(new Dimension(800, 300)); // Placeholder size
        bottomPanel.add(graphPlaceholderPanel, BorderLayout.CENTER);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        fromLabel.setVisible(false);
        dateFromField.setVisible(false);
        toLabel.setVisible(false);
        dateToField.setVisible(false);
    }

    private void goBackToDashboard() {
        parentDashboard.setVisible(true); 
        dispose(); 
    }

    class SalesByDate {
        public Date date;
        public double totalSales;
        public int totalItems;

        public SalesByDate(Date date, double totalSales, int totalItems) {
            this.date = date;
            this.totalSales = totalSales;
            this.totalItems = totalItems;
        }
    }

    private Map<String, Double> fetchSalesByCategory(Date fromDate, Date toDate, Map<String, Integer> itemsSoldByCategory) {
        Map<String, Double> salesByCategory = new HashMap<>();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = dbDateFormat.format(fromDate);
        String toDateStr = dbDateFormat.format(toDate);

        double overallTotalSales = 0.0;
        int overallTotalItemsSold = 0;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT p.category, SUM(s.sale_price) AS total_sales, SUM(s.quantity_sold) AS total_items " +
                         "FROM sales s JOIN products p ON s.product_id = p.product_id " +
                         "WHERE s.sale_date BETWEEN ? AND ? " +
                         "GROUP BY p.category ORDER BY total_sales DESC";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, fromDateStr);
            preparedStatement.setString(2, toDateStr);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String category = resultSet.getString("category");
                double totalSales = resultSet.getDouble("total_sales");
                int totalItems = resultSet.getInt("total_items");

                salesByCategory.put(category, totalSales);
                itemsSoldByCategory.put(category, totalItems);

                overallTotalSales += totalSales;
                overallTotalItemsSold += totalItems;
            }
            // Update the summary labels in the main ReportGenerator window
            totalSalesLabel.setText(String.format("Total Sales: P %.2f", overallTotalSales));
            totalItemsSoldLabel.setText(String.format("Total items sold: %d", overallTotalItemsSold));

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error fetching sales by category: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        return salesByCategory;
    }

    private List<SalesByDate> fetchSalesOverTime(Date fromDate, Date toDate) {
        List<SalesByDate> salesOverTime = new ArrayList<>();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = dbDateFormat.format(fromDate);
        String toDateStr = dbDateFormat.format(toDate);

        double overallTotalSales = 0.0;
        int overallTotalItemsSold = 0;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT sale_date, SUM(sale_price) AS daily_sales, SUM(quantity_sold) AS daily_items " +
                         "FROM sales " +
                         "WHERE sale_date BETWEEN ? AND ? " +
                         "GROUP BY sale_date ORDER BY sale_date ASC";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, fromDateStr);
            preparedStatement.setString(2, toDateStr);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Date saleDate = resultSet.getDate("sale_date");
                double dailySales = resultSet.getDouble("daily_sales");
                int dailyItems = resultSet.getInt("daily_items");
                salesOverTime.add(new SalesByDate(saleDate, dailySales, dailyItems));

                overallTotalSales += dailySales;
                overallTotalItemsSold += dailyItems;
            }
            totalSalesLabel.setText(String.format("Total Sales: P %.2f", overallTotalSales));
            totalItemsSoldLabel.setText(String.format("Total items sold: %d", overallTotalItemsSold));

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error fetching sales over time: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        return salesOverTime;
    }

    private Map<String, Integer> fetchItemsSoldBySupplier(Date fromDate, Date toDate) {
        Map<String, Integer> itemsSoldBySupplier = new HashMap<>();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = dbDateFormat.format(fromDate);
        String toDateStr = dbDateFormat.format(toDate);
        
        double overallTotalSales = 0.0; // To calculate and update main summary
        int overallTotalItemsSold = 0; // To calculate and update main summary

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT p.supplier, SUM(s.quantity_sold) AS total_items, SUM(s.sale_price) AS total_sales_by_supplier " +
                         "FROM sales s JOIN products p ON s.product_id = p.product_id " +
                         "WHERE s.sale_date BETWEEN ? AND ? " +
                         "GROUP BY p.supplier ORDER BY total_items DESC";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, fromDateStr);
            preparedStatement.setString(2, toDateStr);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String supplier = resultSet.getString("supplier");
                int totalItems = resultSet.getInt("total_items");
                double totalSales = resultSet.getDouble("total_sales_by_supplier");

                itemsSoldBySupplier.put(supplier, totalItems);
                overallTotalSales += totalSales;
                overallTotalItemsSold += totalItems;
            }
            totalSalesLabel.setText(String.format("Total Sales: P %.2f", overallTotalSales));
            totalItemsSoldLabel.setText(String.format("Total items sold: %d", overallTotalItemsSold));

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error fetching items sold by supplier: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        return itemsSoldBySupplier;
    }

    private Map<String, Integer> fetchStockLevels() {
        Map<String, Integer> stockLevels = new HashMap<>();
        // No date range needed for stock levels report, so summary labels will be 0.00 / 0
        totalSalesLabel.setText("Total Sales: P 0.00");
        totalItemsSoldLabel.setText("Total items sold: 0");

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT product_name, stock FROM products ORDER BY product_name ASC";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                stockLevels.put(resultSet.getString("product_name"), resultSet.getInt("stock"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error fetching stock levels: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        return stockLevels;
    }

    private void generateReport() {
        String reportType = (String) reportTypeComboBox.getSelectedItem();
        String selectedPeriod = (String) fixedPeriodComboBox.getSelectedItem();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateFromDate = null;
        Date dateToDate = null;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23); // End of day for 'to' date
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        dateToDate = calendar.getTime(); 

        calendar.set(Calendar.HOUR_OF_DAY, 0); // Start of day for 'from' date
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        switch (selectedPeriod) {
            case "Last 7 Days":
                calendar.add(Calendar.DAY_OF_MONTH, -6); 
                dateFromDate = calendar.getTime();
                break;
            case "Last 30 Days":
                calendar.add(Calendar.DAY_OF_MONTH, -29);
                dateFromDate = calendar.getTime();
                break;
            case "This Month":
                calendar.set(Calendar.DAY_OF_MONTH, 1); 
                dateFromDate = calendar.getTime();
                break;
            case "Last Month":
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                dateFromDate = calendar.getTime();
                Calendar lastDayOfLastMonth = (Calendar) calendar.clone();
                lastDayOfLastMonth.set(Calendar.DAY_OF_MONTH, lastDayOfLastMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
                dateToDate = lastDayOfLastMonth.getTime(); // Ensure 'To' is end of last month
                break;
            case "This Year":
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                dateFromDate = calendar.getTime();
                break;
            case "All Time": 
                Calendar earliest = Calendar.getInstance();
                earliest.set(1900, Calendar.JANUARY, 1, 0, 0, 0); // Start from a very early date
                earliest.set(Calendar.MILLISECOND, 0);
                dateFromDate = earliest.getTime();

                Calendar latest = Calendar.getInstance();
                latest.set(Calendar.HOUR_OF_DAY, 23); // End of day
                latest.set(Calendar.MINUTE, 59);
                latest.set(Calendar.SECOND, 59);
                latest.set(Calendar.MILLISECOND, 999);
                dateToDate = latest.getTime();
                break;
            default:
                // This case should ideally not be reached since "Custom Range" is removed.
                JOptionPane.showMessageDialog(this, "Invalid date period selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
        }

        // Fetch actual data from database based on report type and dates
        Object reportData = null; // Generic object to hold different report data types
        String reportTitle = reportType + " Report"; // Title without date range for Stock Levels

        // Handle Stock Levels report which is not date-dependent
        if ("Stock Levels".equals(reportType)) {
            reportData = fetchStockLevels();
            reportTitle = "Current Stock Levels Report";
        } else {
            // For date-dependent reports, include the date range in the title
            reportTitle += " (" + dateFormat.format(dateFromDate) + " to " + dateFormat.format(dateToDate) + ")";

            switch (reportType) {
                case "Sales by Category":
                    Map<String, Integer> itemsSoldByCategory = new HashMap<>(); // This will be filled by the method
                    reportData = fetchSalesByCategory(dateFromDate, dateToDate, itemsSoldByCategory);
                    break;
                case "Sales over Time":
                    reportData = fetchSalesOverTime(dateFromDate, dateToDate);
                    break;
                case "Items Sold by Supplier":
                    reportData = fetchItemsSoldBySupplier(dateFromDate, dateToDate);
                    break;
                default:
                    // Should not happen as reportTypeComboBox is controlled
                    JOptionPane.showMessageDialog(this, "Unknown report type selected.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
            }
        }

        if (reportData == null || (reportData instanceof Map && ((Map) reportData).isEmpty()) || (reportData instanceof List && ((List) reportData).isEmpty())) {
             JOptionPane.showMessageDialog(this, "No data available for the selected period and report type.", "No Data", JOptionPane.INFORMATION_MESSAGE);
             // Reset summary labels if no data found
             totalSalesLabel.setText("Total Sales: P 0.00");
             totalItemsSoldLabel.setText("Total items sold: 0");
             return;
        }

        // Open the generated report dialog with real data
        GeneratedReportDialog reportDialog = new GeneratedReportDialog(this, reportType, reportTitle, reportData);
        reportDialog.setVisible(true);
    }
    
    private void exportPdf() {
        JOptionPane.showMessageDialog(this, "Export PDF functionality will be implemented here.", "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }
    
    class GeneratedReportDialog extends JDialog {
        private String reportType;
        private String dialogTitle;
        private Object reportData; 

        public GeneratedReportDialog(JFrame parent, String reportType, String dialogTitle, Object reportData) {
            super(parent, dialogTitle, true); // Title and modal
            this.reportType = reportType;
            this.dialogTitle = dialogTitle;
            this.reportData = reportData;

            setSize(700, 550); // Adjusted size for the graph dialog
            setLocationRelativeTo(parent);
            setUndecorated(true);
            getRootPane().setBorder(BorderFactory.createLineBorder(new Color(67, 42, 32), 5));

            JPanel dialogPanel = new JPanel(new BorderLayout());
            dialogPanel.setBackground(new Color(134, 100, 79));
            dialogPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

            // --- Title Panel ---
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setBackground(new Color(67, 42, 32));
            titlePanel.setPreferredSize(new Dimension(660, 60));
            titlePanel.setBorder(new EmptyBorder(5, 0, 5, 0));
            titlePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel dialogTitleLabel = new JLabel(dialogTitle, SwingConstants.CENTER);
            dialogTitleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            dialogTitleLabel.setForeground(Color.WHITE);
            titlePanel.add(dialogTitleLabel, BorderLayout.CENTER);
            dialogPanel.add(titlePanel, BorderLayout.NORTH);

            // Make dialog draggable by title bar
            MouseAdapter dragListener = new MouseAdapter() {
                int mouseX, mouseY;
                @Override
                public void mousePressed(MouseEvent e) {
                    mouseX = e.getX();
                    mouseY = e.getY();
                }
                @Override
                public void mouseDragged(MouseEvent e) {
                    setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
                }
            };
            titlePanel.addMouseListener(dragListener);
            titlePanel.addMouseMotionListener(dragListener);

            JPanel contentDisplayPanel = new JPanel(new BorderLayout());
            contentDisplayPanel.setBackground(new Color(240, 230, 140)); // Light yellow background

            if ("Stock Levels".equals(reportType)) {
                // For Stock Levels, use a JTable with JScrollPane
                Map<String, Integer> stockData = (Map<String, Integer>) reportData;
                DefaultTableModel stockTableModel = new DefaultTableModel(new String[]{"Product Name", "Stock"}, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                List<Map.Entry<String, Integer>> sortedStockData = new ArrayList<>(stockData.entrySet());
                sortedStockData.sort(Map.Entry.comparingByKey()); // Sort by product name

                for (Map.Entry<String, Integer> entry : sortedStockData) {
                    stockTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
                }

                JTable stockTable = new JTable(stockTableModel);
                stockTable.setFont(new Font("Arial", Font.PLAIN, 14));
                stockTable.setRowHeight(24);
                stockTable.setBackground(new Color(240, 230, 140)); // Match dialog background
                stockTable.setForeground(Color.BLACK);
                stockTable.setGridColor(new Color(180, 180, 180)); // Lighter grid for table

                JTableHeader tableHeader = stockTable.getTableHeader();
                tableHeader.setFont(new Font("Arial", Font.BOLD, 16));
                tableHeader.setBackground(new Color(67, 42, 32));
                tableHeader.setForeground(Color.WHITE);
                tableHeader.setReorderingAllowed(false);
                tableHeader.setResizingAllowed(true);


                JScrollPane scrollPane = new JScrollPane(stockTable);
                scrollPane.getViewport().setBackground(new Color(240, 230, 140)); // Match table background
                scrollPane.setBorder(BorderFactory.createLineBorder(new Color(67, 42, 32), 1)); // Small border
                contentDisplayPanel.add(scrollPane, BorderLayout.CENTER);

            } else {
                // For other report types, use the custom drawing JPanel (graphPanel)
                JPanel graphPanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                        FontMetrics fm = g2d.getFontMetrics(); 
                        int panelWidth = getWidth();
                        int panelHeight = getHeight();
                        int padding = 50; // Padding from panel edges

                        g2d.setColor(new Color(240, 230, 140)); // Light yellow background for graph area
                        g2d.fillRect(0, 0, panelWidth, panelHeight);
                        
                        switch (reportType) {
                            case "Sales by Category":
                                drawBarChart(g2d, (Map<String, Double>) reportData, "Total Sales (P)", padding, panelWidth, panelHeight, fm);
                                break;
                            case "Sales over Time":
                                drawLineChart(g2d, (List<SalesByDate>) reportData, "Total Sales (P)", padding, panelWidth, panelHeight, fm);
                                break;
                            case "Items Sold by Supplier":
                                drawBarChartItemsSold(g2d, (Map<String, Integer>) reportData, "Total Items Sold", padding, panelWidth, panelHeight, fm);
                                break;
                            // Stock levels handled by JTable, so not here
                            default:
                                g2d.setColor(Color.BLACK);
                                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                                String msg = "Report type not supported for graph display.";
                                int msgWidth = fm.stringWidth(msg);
                                g2d.drawString(msg, (panelWidth - msgWidth) / 2, panelHeight / 2);
                                break;
                        }
                    }

                    // Helper to draw a generic bar chart from Map<String, Double> data
                    private void drawBarChart(Graphics2D g2d, Map<String, Double> data, String yAxisLabel, int padding, int panelWidth, int panelHeight, FontMetrics fm) {
                        if (data.isEmpty()) {
                            g2d.setColor(Color.BLACK);
                            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
                            String noData = "No sales data for selected period.";
                            g2d.drawString(noData, (panelWidth - fm.stringWidth(noData)) / 2, panelHeight / 2);
                            return;
                        }

                        List<Map.Entry<String, Double>> sortedData = new ArrayList<>(data.entrySet());
                        // Sort by value (sales) in descending order
                        sortedData.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                        double maxValue = sortedData.stream().mapToDouble(Map.Entry::getValue).max().orElse(1.0);
                        if (maxValue == 0) maxValue = 1.0; // Avoid division by zero

                        int barAreaHeight = panelHeight - 2 * padding - fm.getHeight() * 2; // Space for X labels and padding
                        int barAreaWidth = panelWidth - 2 * padding;
                        int barCount = sortedData.size();
                        int barWidth = (barCount > 0) ? Math.max(10, (barAreaWidth / barCount) - 10) : 0; // Ensure min width
                        int gap = (barCount > 1) ? (barAreaWidth - barCount * barWidth) / (barCount - 1) : 0;
                        if (barCount == 1) gap = (barAreaWidth - barWidth) / 2; // Center single bar

                        int xOrigin = padding;
                        int yOrigin = panelHeight - padding - fm.getHeight(); // Bottom line for bars

                        // Draw X and Y axes
                        g2d.setColor(Color.BLACK);
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawLine(xOrigin, yOrigin, xOrigin + barAreaWidth, yOrigin); // X-axis
                        g2d.drawLine(xOrigin, yOrigin, xOrigin, yOrigin - barAreaHeight); // Y-axis

                        // Draw Y-axis labels and scale
                        int numYLabels = 5;
                        for (int i = 0; i <= numYLabels; i++) {
                            double yValue = (maxValue / numYLabels) * i;
                            int yPos = yOrigin - (int) ((yValue / maxValue) * barAreaHeight);
                            String label = String.format("P%.0f", yValue);
                            g2d.drawString(label, xOrigin - fm.stringWidth(label) - 5, yPos + fm.getHeight() / 2);
                            if (i > 0) g2d.drawLine(xOrigin - 3, yPos, xOrigin, yPos); // Tick marks
                        }
                        // Y-axis label
                        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                        g2d.drawString(yAxisLabel, xOrigin + 5, padding - fm.getHeight());


                        // Draw bars
                        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                        int currentX = xOrigin + (panelWidth - 2*padding - (barWidth*barCount + gap*(barCount-1))) / 2; // Center the group of bars
                        if (barCount == 1) currentX = xOrigin + (barAreaWidth - barWidth) / 2;


                        for (int i = 0; i < barCount; i++) {
                            Map.Entry<String, Double> entry = sortedData.get(i);
                            String label = entry.getKey();
                            double value = entry.getValue();

                            int barHeight = (int) ((value / maxValue) * barAreaHeight);
                            int y = yOrigin - barHeight;

                            // Bar color cycling
                            Color barColor;
                            switch (i % 5) {
                                case 0: barColor = new Color(255, 99, 71); break; // Tomato
                                case 1: barColor = new Color(60, 179, 113); break; // Medium Sea Green
                                case 2: barColor = new Color(255, 215, 0); break; // Gold
                                case 3: barColor = new Color(100, 149, 237); break; // Cornflower Blue
                                case 4: barColor = new Color(138, 43, 226); break; // Blue Violet
                                default: barColor = Color.GRAY; break;
                            }
                            g2d.setColor(barColor);
                            g2d.fillRect(currentX, y, barWidth, barHeight);
                            g2d.setColor(Color.BLACK);
                            g2d.drawRect(currentX, y, barWidth, barHeight);

                            // Draw value on top of bar
                            String valueStr = String.format("P%.2f", value);
                            int valueWidth = fm.stringWidth(valueStr);
                            g2d.drawString(valueStr, currentX + (barWidth - valueWidth) / 2, y - 5);

                            // Draw category label below bar
                            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                            int labelWidth = fm.stringWidth(label);
                            g2d.drawString(label, currentX + (barWidth - labelWidth) / 2, yOrigin + fm.getHeight() + 5);

                            currentX += (barWidth + gap);
                        }
                    }

                    // Helper to draw a bar chart for items sold (Map<String, Integer>)
                    private void drawBarChartItemsSold(Graphics2D g2d, Map<String, Integer> data, String yAxisLabel, int padding, int panelWidth, int panelHeight, FontMetrics fm) {
                        if (data.isEmpty()) {
                            g2d.setColor(Color.BLACK);
                            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
                            String noData = "No items sold data for selected period.";
                            g2d.drawString(noData, (panelWidth - fm.stringWidth(noData)) / 2, panelHeight / 2);
                            return;
                        }

                        List<Map.Entry<String, Integer>> sortedData = new ArrayList<>(data.entrySet());
                        sortedData.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                        int maxValue = sortedData.stream().mapToInt(Map.Entry::getValue).max().orElse(1);
                        if (maxValue == 0) maxValue = 1;

                        int barAreaHeight = panelHeight - 2 * padding - fm.getHeight() * 2;
                        int barAreaWidth = panelWidth - 2 * padding;
                        int barCount = sortedData.size();
                        int barWidth = (barCount > 0) ? Math.max(10, (barAreaWidth / barCount) - 10) : 0;
                        int gap = (barCount > 1) ? (barAreaWidth - barCount * barWidth) / (barCount - 1) : 0;
                        if (barCount == 1) gap = (barAreaWidth - barWidth) / 2;

                        int xOrigin = padding;
                        int yOrigin = panelHeight - padding - fm.getHeight();

                        // Draw X and Y axes
                        g2d.setColor(Color.BLACK);
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawLine(xOrigin, yOrigin, xOrigin + barAreaWidth, yOrigin);
                        g2d.drawLine(xOrigin, yOrigin, xOrigin, yOrigin - barAreaHeight);

                        // Draw Y-axis labels and scale
                        int numYLabels = 5;
                        for (int i = 0; i <= numYLabels; i++) {
                            int yValue = (maxValue / numYLabels) * i;
                            int yPos = yOrigin - (int) ((double) yValue / maxValue * barAreaHeight);
                            String label = String.valueOf(yValue);
                            g2d.drawString(label, xOrigin - fm.stringWidth(label) - 5, yPos + fm.getHeight() / 2);
                            if (i > 0) g2d.drawLine(xOrigin - 3, yPos, xOrigin, yPos);
                        }
                        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                        g2d.drawString(yAxisLabel, xOrigin + 5, padding - fm.getHeight());


                        // Draw bars
                        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                        int currentX = xOrigin + (panelWidth - 2*padding - (barWidth*barCount + gap*(barCount-1))) / 2;
                        if (barCount == 1) currentX = xOrigin + (barAreaWidth - barWidth) / 2;


                        for (int i = 0; i < barCount; i++) {
                            Map.Entry<String, Integer> entry = sortedData.get(i);
                            String label = entry.getKey();
                            int value = entry.getValue();

                            int barHeight = (int) ((double) value / maxValue * barAreaHeight);
                            int y = yOrigin - barHeight;

                            Color barColor;
                            switch (i % 5) {
                                case 0: barColor = new Color(255, 99, 71); break;
                                case 1: barColor = new Color(60, 179, 113); break;
                                case 2: barColor = new Color(255, 215, 0); break;
                                case 3: barColor = new Color(100, 149, 237); break;
                                case 4: barColor = new Color(138, 43, 226); break;
                                default: barColor = Color.GRAY; break;
                            }
                            g2d.setColor(barColor);
                            g2d.fillRect(currentX, y, barWidth, barHeight);
                            g2d.setColor(Color.BLACK);
                            g2d.drawRect(currentX, y, barWidth, barHeight);

                            String valueStr = String.valueOf(value);
                            int valueWidth = fm.stringWidth(valueStr);
                            g2d.drawString(valueStr, currentX + (barWidth - valueWidth) / 2, y - 5);

                            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                            int labelWidth = fm.stringWidth(label);
                            g2d.drawString(label, currentX + (barWidth - labelWidth) / 2, yOrigin + fm.getHeight() + 5);

                            currentX += (barWidth + gap);
                        }
                    }


                    private void drawLineChart(Graphics2D g2d, List<SalesByDate> data, String yAxisLabel, int padding, int panelWidth, int panelHeight, FontMetrics fm) {
                        if (data.isEmpty()) {
                            g2d.setColor(Color.BLACK);
                            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
                            String noData = "No sales data for selected period.";
                            g2d.drawString(noData, (panelWidth - fm.stringWidth(noData)) / 2, panelHeight / 2);
                            return;
                        }

                        // Sort data by date
                        data.sort(Comparator.comparing(s -> s.date));

                        double maxSales = data.stream().mapToDouble(s -> s.totalSales).max().orElse(1.0);
                        if (maxSales == 0) maxSales = 1.0;

                        SimpleDateFormat axisDateFormat = new SimpleDateFormat("MM-dd"); // For X-axis labels

                        int chartAreaWidth = panelWidth - 2 * padding;
                        int chartAreaHeight = panelHeight - 2 * padding - fm.getHeight() * 2; // Space for X-axis labels

                        int xOrigin = padding;
                        int yOrigin = panelHeight - padding - fm.getHeight();

                        // Draw X and Y axes
                        g2d.setColor(Color.BLACK);
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawLine(xOrigin, yOrigin, xOrigin + chartAreaWidth, yOrigin); // X-axis
                        g2d.drawLine(xOrigin, yOrigin, xOrigin, yOrigin - chartAreaHeight); // Y-axis

                        // Draw Y-axis labels
                        int numYLabels = 5;
                        for (int i = 0; i <= numYLabels; i++) {
                            double yValue = (maxSales / numYLabels) * i;
                            int yPos = yOrigin - (int) ((yValue / maxSales) * chartAreaHeight);
                            String label = String.format("P%.0f", yValue);
                            g2d.drawString(label, xOrigin - fm.stringWidth(label) - 5, yPos + fm.getHeight() / 2);
                            if (i > 0) g2d.drawLine(xOrigin - 3, yPos, xOrigin, yPos);
                        }
                        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                        g2d.drawString(yAxisLabel, xOrigin + 5, padding - fm.getHeight());

                        // Draw data points and lines
                        g2d.setColor(new Color(0, 102, 204)); // Blue for line
                        g2d.setStroke(new BasicStroke(3)); // Thicker line

                        Point prevPoint = null;
                        for (int i = 0; i < data.size(); i++) {
                            SalesByDate point = data.get(i);

                            // Calculate x position (evenly distributed for now, could be time-scaled)
                            int x = xOrigin + (int) ((double) i / (data.size() - 1) * chartAreaWidth);
                            if (data.size() == 1) x = xOrigin + chartAreaWidth / 2; // Center if only one point

                            int y = yOrigin - (int) ((point.totalSales / maxSales) * chartAreaHeight);

                            // Draw line segment
                            if (prevPoint != null) {
                                g2d.drawLine(prevPoint.x, prevPoint.y, x, y);
                            }
                            prevPoint = new Point(x, y);

                            // Draw point (circle)
                            g2d.fillOval(x - 5, y - 5, 10, 10);

                            // Draw X-axis date label below point
                            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                            String dateLabel = axisDateFormat.format(point.date);
                            int dateLabelWidth = fm.stringWidth(dateLabel);
                            g2d.drawString(dateLabel, x - dateLabelWidth / 2, yOrigin + fm.getHeight() + 5);

                            // Draw sales value above point
                            String valueStr = String.format("P%.0f", point.totalSales);
                            int valueWidth = fm.stringWidth(valueStr);
                            g2d.drawString(valueStr, x - valueWidth / 2, y - 10);
                        }
                    }
                };
                contentDisplayPanel.add(graphPanel, BorderLayout.CENTER);
            }
            
            dialogPanel.add(contentDisplayPanel, BorderLayout.CENTER);

            // --- Close Button ---
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            buttonPanel.setBackground(new Color(134, 100, 79));

            JButton closeButton = new JButton("Close");
            closeButton.setFont(new Font("Arial", Font.BOLD, 14));
            closeButton.setBackground(new Color(220, 20, 60)); // Crimson
            closeButton.setForeground(Color.WHITE);
            closeButton.setFocusPainted(false);
            closeButton.setBorder(BorderFactory.createLineBorder(new Color(180, 0, 40), 2));
            closeButton.setPreferredSize(new Dimension(100, 35));
            closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Set pointer cursor
            closeButton.addActionListener(e -> dispose());
            buttonPanel.add(closeButton);
            dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(dialogPanel);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InventoryDashboard dummyDashboard = new InventoryDashboard() {
                @Override
                public void setVisible(boolean b) { /* do nothing */ }
                @Override
                public void dispose() { /* do nothing */ }
            };
            dummyDashboard.setVisible(false);

            new ReportGenerator(dummyDashboard).setVisible(true);
        });
    }
}
