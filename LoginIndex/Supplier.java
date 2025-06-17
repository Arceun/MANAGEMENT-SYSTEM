

import javax.swing.*;
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
import java.util.Vector;
import javax.swing.border.EmptyBorder; 
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable; 
import javax.swing.table.JTableHeader; 
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;

public class Supplier extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/memistore";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private InventoryDashboard parentDashboard; 

    // GUI Components for Supplier management
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JButton addSupplierButton;
    private JButton backButton; 

    private DefaultTableModel supplierTableModel;
    private JTable supplierTable;


    public Supplier(InventoryDashboard parentDashboard) {
        this.parentDashboard = parentDashboard;
        setTitle("Memi's Treats - Supplier Management");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose on close, don't exit entire app
        setLocationRelativeTo(null);
        setResizable(false);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(139, 69, 19)); // Dark brown background

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(67, 42, 32)); // Darker brown
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20)); // Padding

        titleLabel = new JLabel("Supplier List", SwingConstants.CENTER);
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
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Already has pointer cursor
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBackToDashboard();
            }
        });
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButtonPanel.setOpaque(false); // Make it transparent
        backButtonPanel.add(backButton);
        headerPanel.add(backButtonPanel, BorderLayout.WEST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Supplier Table Section ---
        String[] columnNames = {"#", "Supplier Name", "Supplier ID", "Agent", "Contact", "Actions"};
        supplierTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        supplierTable = new JTable(supplierTableModel);
        supplierTable.setFont(new Font("Arial", Font.PLAIN, 14));
        supplierTable.setRowHeight(28);

        JTableHeader tableHeader = supplierTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 16));
        tableHeader.setBackground(new Color(67, 42, 32));
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setReorderingAllowed(false);
        tableHeader.setResizingAllowed(true);

        supplierTable.setBackground(new Color(134, 100, 79));
        supplierTable.setForeground(Color.BLACK);
        supplierTable.setGridColor(new Color(0, 0, 0));
        supplierTable.setSelectionBackground(new Color(255, 165, 0));
        supplierTable.setSelectionForeground(Color.WHITE);


        supplierTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonCellRenderer());
        supplierTable.getColumnModel().getColumn(5).setCellEditor(new ButtonCellEditor(this));

        JScrollPane scrollPane = new JScrollPane(supplierTable);
        scrollPane.getViewport().setBackground(new Color(134, 100, 79));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        // --- End Supplier Table Section ---

        // Footer Panel (for Add Supplier Button)
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        footerPanel.setBackground(new Color(67, 42, 32));
        footerPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        addSupplierButton = new JButton("Add New Supplier");
        addSupplierButton.setFont(new Font("Arial", Font.BOLD, 16));
        addSupplierButton.setBackground(new Color(60, 179, 113)); // Medium Sea Green
        addSupplierButton.setForeground(Color.WHITE);
        addSupplierButton.setFocusPainted(false);
        addSupplierButton.setBorder(BorderFactory.createLineBorder(new Color(46, 139, 87), 2));
        addSupplierButton.setPreferredSize(new Dimension(180, 45));
        addSupplierButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addSupplierButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the new SupplierFormDialog
                SupplierFormDialog addSupplierDialog = new SupplierFormDialog(Supplier.this);
                addSupplierDialog.setVisible(true);
                loadSupplierData(); // Reload data after dialog closes
            }
        });
        footerPanel.add(addSupplierButton);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
        loadSupplierData(); // Load initial data
    }

    private void goBackToDashboard() {
        parentDashboard.setVisible(true); // Show the parent dashboard
        dispose(); // Close this Supplier window
    }

    private void loadSupplierData() {
        supplierTableModel.setRowCount(0); // Clear existing data

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT id, supplier_name, supplier_id, agent_name, contact_info FROM suppliers ORDER BY id";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            int rowNum = 1;
            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rowNum++);
                row.add(resultSet.getString("supplier_name"));
                row.add(resultSet.getString("supplier_id"));
                row.add(resultSet.getString("agent_name"));
                row.add(resultSet.getString("contact_info"));
                row.add("VIEW_BUTTON"); // Placeholder for the "View" button
                supplierTableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading supplier data: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    class SupplierFormDialog extends JDialog {
        private JTextField supplierNameField;
        private JTextField supplierIdField;
        private JTextField agentField;
        private JTextField contactField;
        private JLabel dialogTitleLabel; 

        public SupplierFormDialog(JFrame parent) { 
            super(parent, "Add Supplier", true); 
            setSize(400, 450);
            setLocationRelativeTo(parent);
            setUndecorated(true); 
            getRootPane().setBorder(BorderFactory.createLineBorder(new Color(67, 42, 32), 5)); 

            JPanel dialogPanel = new JPanel(new GridBagLayout());
            dialogPanel.setBackground(new Color(134, 100, 79)); // Main dialog background
            dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); 

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 5, 8, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // --- Title Panel Section ---
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setBackground(new Color(67, 42, 32)); // Dark brown for title bar
            titlePanel.setPreferredSize(new Dimension(360, 60));
            titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            titlePanel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Make title bar draggable

            dialogTitleLabel = new JLabel("Add Supplier", SwingConstants.CENTER); // Initial title
            dialogTitleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            dialogTitleLabel.setForeground(Color.WHITE);
            titlePanel.add(dialogTitleLabel, BorderLayout.CENTER);

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0; // Title row does not grow vertically
            gbc.insets = new Insets(0, 0, 15, 0);
            dialogPanel.add(titlePanel, gbc);
            // --- End Title Panel Section ---

            // --- Form Fields ---
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0; // Form field rows do not grow vertically
            gbc.insets = new Insets(8, 5, 8, 5);

            // Supplier Name
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel supplierNameLabel = new JLabel("Supplier Name:");
            supplierNameLabel.setForeground(Color.BLACK);
            dialogPanel.add(supplierNameLabel, gbc);
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            supplierNameField = new JTextField(15);
            supplierNameField.setPreferredSize(new Dimension(200, 28));
            dialogPanel.add(supplierNameField, gbc);

            // Supplier ID
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel supplierIdLabel = new JLabel("Supplier_ID:");
            supplierIdLabel.setForeground(Color.BLACK);
            dialogPanel.add(supplierIdLabel, gbc);
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            supplierIdField = new JTextField(15);
            supplierIdField.setPreferredSize(new Dimension(200, 28));
            dialogPanel.add(supplierIdField, gbc);

            // Agent
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel agentLabel = new JLabel("Agent:");
            agentLabel.setForeground(Color.BLACK);
            dialogPanel.add(agentLabel, gbc);
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            agentField = new JTextField(15);
            agentField.setPreferredSize(new Dimension(200, 28));
            dialogPanel.add(agentField, gbc);

            // Contact
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel contactLabel = new JLabel("Contact:");
            contactLabel.setForeground(Color.BLACK);
            dialogPanel.add(contactLabel, gbc);
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            contactField = new JTextField(15);
            contactField.setPreferredSize(new Dimension(200, 28));
            dialogPanel.add(contactField, gbc);

            // --- Buttons Panel (Save, Cancel) ---
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            buttonPanel.setBackground(new Color(134, 100, 79)); // Match dialog background

            JButton saveButton = new JButton("Save");
            saveButton.setFont(new Font("Arial", Font.BOLD, 14));
            saveButton.setBackground(new Color(60, 179, 113)); // Medium Sea Green
            saveButton.setForeground(Color.WHITE);
            saveButton.setFocusPainted(false);
            saveButton.setBorder(BorderFactory.createLineBorder(new Color(46, 139, 87), 2));
            saveButton.setPreferredSize(new Dimension(100, 35));
            saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Pointer cursor for Save
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveSupplier();
                }
            });
            buttonPanel.add(saveButton);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
            cancelButton.setBackground(new Color(220, 20, 60)); // Crimson
            cancelButton.setForeground(Color.WHITE);
            cancelButton.setFocusPainted(false);
            cancelButton.setBorder(BorderFactory.createLineBorder(new Color(180, 0, 40), 2));
            cancelButton.setPreferredSize(new Dimension(100, 35));
            cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Pointer cursor for Cancel
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose(); // Close the dialog
                }
            });
            buttonPanel.add(cancelButton);

            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.HORIZONTAL; 
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.insets = new Insets(15, 0, 0, 0);
            dialogPanel.add(buttonPanel, gbc);
            // --- End Buttons Panel ---

            Container contentPane = getContentPane();
            contentPane.setLayout(new BorderLayout());
            contentPane.setBackground(new Color(134, 100, 79));
            contentPane.add(dialogPanel, BorderLayout.CENTER);

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
        }

       
        private void saveSupplier() {
            String supplierName = supplierNameField.getText().trim();
            String supplierId = supplierIdField.getText().trim();
            String agent = agentField.getText().trim();
            String contact = contactField.getText().trim();

            if (supplierName.isEmpty() || supplierId.isEmpty() || agent.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "INSERT INTO suppliers (supplier_name, supplier_id, agent_name, contact_info) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, supplierName);
                preparedStatement.setString(2, supplierId);
                preparedStatement.setString(3, agent);
                preparedStatement.setString(4, contact);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Supplier added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // Close the dialog after successful save
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add supplier.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                if (ex.getSQLState().startsWith("23")) { // SQLState for integrity constraint violation
                    JOptionPane.showMessageDialog(this,
                            "Supplier ID '" + supplierId + "' already exists. Please use a unique ID.",
                            "Duplicate Entry Error",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Database error: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                ex.printStackTrace();
            }
        }
    }

    
    class ButtonCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton viewButton;

        public ButtonCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0)); // Align buttons horizontally
            setOpaque(true); // Ensure background is painted

            viewButton = new JButton("View");
            viewButton.setFont(new Font("Arial", Font.BOLD, 12));
            viewButton.setBackground(new Color(70, 130, 180)); // Steel Blue
            viewButton.setForeground(Color.WHITE);
            viewButton.setFocusPainted(false);
            viewButton.setBorderPainted(false);
            viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Already has pointer cursor
            viewButton.setPreferredSize(new Dimension(80, 24)); // Size for table cell button
            add(viewButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                viewButton.setBackground(new Color(100, 150, 200)); // Slightly different color when selected
            } else {
                setBackground(table.getBackground());
                viewButton.setBackground(new Color(70, 130, 180)); // Reset to default colors
            }
            return this;
        }
    }

    
    class ButtonCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private JPanel panel;
        private JButton viewButton;
        private JTable table;
        private int row;
        private Supplier parentFrame; // Reference to the Supplier frame

        public ButtonCellEditor(Supplier parentFrame) {
            this.parentFrame = parentFrame;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setOpaque(true);

            viewButton = new JButton("View");
            viewButton.setFont(new Font("Arial", Font.BOLD, 12));
            viewButton.setBackground(new Color(70, 130, 180)); // Steel Blue
            viewButton.setForeground(Color.WHITE);
            viewButton.setFocusPainted(false);
            viewButton.setBorderPainted(false);
            viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Already has pointer cursor
            viewButton.setPreferredSize(new Dimension(80, 24));
            viewButton.setActionCommand("view"); // Set action command
            viewButton.addActionListener(this); // Register editor as listener

            panel.add(viewButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.table = table;
            this.row = row;

            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
                viewButton.setBackground(new Color(100, 150, 200));
            } else {
                panel.setBackground(table.getBackground());
                viewButton.setBackground(new Color(70, 130, 180));
            }
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null; // No specific value to return from button click
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped(); // Stop editing when button is clicked

            // Get supplier_id and supplier_name from the current row
            String supplierName = (String) table.getModel().getValueAt(row, 1); // Supplier Name is at index 1
            // String supplierId = (String) table.getModel().getValueAt(row, 2);   // Supplier ID is at index 2 (Original, but products table uses name)

            if ("view".equals(e.getActionCommand())) {
                System.out.println("View button clicked for Supplier: " + supplierName); // No longer printing ID
                // Open the SupplierProductsDialog, passing the supplierName
                SupplierProductsDialog productsDialog = new SupplierProductsDialog(parentFrame, supplierName);
                productsDialog.setVisible(true);
            }
        }
    }

    class SupplierProductsDialog extends JDialog {
        private DefaultTableModel productsTableModel;
        private JTable productsTable;
        private JLabel dialogTitleLabel;

        // Constructor now only takes supplierName
        public SupplierProductsDialog(JFrame parent, String supplierName) {
            super(parent, "Products Supplied by: " + supplierName, true);
            setSize(500, 400); // Adjust size as needed
            setLocationRelativeTo(parent);
            setUndecorated(true);
            getRootPane().setBorder(BorderFactory.createLineBorder(new Color(67, 42, 32), 5));

            JPanel dialogPanel = new JPanel(new BorderLayout());
            dialogPanel.setBackground(new Color(134, 100, 79));
            dialogPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

            // --- Title Panel ---
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setBackground(new Color(67, 42, 32));
            titlePanel.setPreferredSize(new Dimension(460, 60));
            titlePanel.setBorder(new EmptyBorder(5, 0, 5, 0));
            titlePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            dialogTitleLabel = new JLabel("Product Supplied by: " + supplierName, SwingConstants.CENTER);
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

            // --- Products Table ---
            String[] columnNames = {"Product Name", "Product_Id", "Price", "Stock"};
            productsTableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            productsTable = new JTable(productsTableModel);
            productsTable.setFont(new Font("Arial", Font.PLAIN, 14));
            productsTable.setRowHeight(28);

            JTableHeader tableHeader = productsTable.getTableHeader();
            tableHeader.setFont(new Font("Arial", Font.BOLD, 16));
            tableHeader.setBackground(new Color(67, 42, 32));
            tableHeader.setForeground(Color.WHITE);
            tableHeader.setReorderingAllowed(false);
            tableHeader.setResizingAllowed(true);

            productsTable.setBackground(new Color(134, 100, 79));
            productsTable.setForeground(Color.BLACK);
            productsTable.setGridColor(new Color(0, 0, 0));
            productsTable.setSelectionBackground(new Color(255, 165, 0));
            productsTable.setSelectionForeground(Color.WHITE);

            JScrollPane scrollPane = new JScrollPane(productsTable);
            scrollPane.getViewport().setBackground(new Color(134, 100, 79));
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            dialogPanel.add(scrollPane, BorderLayout.CENTER);

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
            closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Pointer cursor for Close button
            closeButton.addActionListener(e -> dispose());
            buttonPanel.add(closeButton);
            dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(dialogPanel);

            // Now passing supplierName to load the products
            loadSupplierProducts(supplierName); // Load products for this supplier
        }

        private void loadSupplierProducts(String supplierName) { // Changed parameter to supplierName
            productsTableModel.setRowCount(0); // Clear existing data

            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Modified SQL to filter by supplier_name
                String sql = "SELECT product_name, product_id, price, stock FROM products WHERE supplier = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, supplierName); // FIX: Use supplierName for the query
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(resultSet.getString("product_name"));
                    row.add(resultSet.getString("product_id"));
                    row.add(resultSet.getString("price"));
                    row.add(resultSet.getInt("stock"));
                    productsTableModel.addRow(row);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading products for supplier: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InventoryDashboard dummyDashboard = new InventoryDashboard() {
                @Override
                public void setVisible(boolean b) { }
                @Override
                public void dispose() { }
            };
            dummyDashboard.setVisible(false);

            Supplier supplierFrame = new Supplier(dummyDashboard);
            supplierFrame.setVisible(true);
        });
    }
}
