import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.net.URL;
import java.util.ArrayList; 
import java.util.List;      

public class InventoryDashboard extends JFrame {

    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/memistore";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private final DefaultTableModel tableModel;
    private JTable productTable; 

    // Image paths for bone buttons
    private static final String BONE_ADD_PRODUCT_IMAGE_PATH = "/images/BoneAddProduct.png";
    private static final String BONE_SUPPLIER_LIST_IMAGE_PATH = "/images/BoneSupplierList.png";
    private static final String BONE_GENERATE_REPORT_IMAGE_PATH = "/images/BoneGenerateReport.png";
    private static final String BONE_SAVE_BUTTON_IMAGE_PATH = "/images/BoneSaveButton.png"; // Still kept for other BoneButton uses
    private static final String BONE_CANCEL_BUTTON_IMAGE_PATH = "/images/BoneCancelButton.png"; // Still kept for other BoneButton uses

    
    public InventoryDashboard() {
        setTitle("Memi's Treats - Inventory Dashboard");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(new Color(139, 69, 19));

        // --- Product Table Section ---
        String[] columnNames = {"#", "Product name", "Product_Id", "Category", "Price", "Stock", "Suppplier", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };
        
        productTable = new JTable(tableModel);

        // --- Table Styling ---
        productTable.setFont(new Font("Arial", Font.PLAIN, 14));
        productTable.setRowHeight(28);

        JTableHeader tableHeader = productTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 16));
        tableHeader.setBackground(new Color(67, 42, 32));
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setReorderingAllowed(false);
        tableHeader.setResizingAllowed(true);

        productTable.setBackground(new Color(134, 100, 79));
        productTable.setForeground(Color.BLACK);
        productTable.setGridColor(new Color(0, 0, 0));
        productTable.setSelectionBackground(new Color(255, 165, 0));
        productTable.setSelectionForeground(Color.WHITE);

        productTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(50);
        productTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        productTable.getColumnModel().getColumn(7).setPreferredWidth(150); 
        
        // Set custom renderer and editor for the "Actions" column
        productTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonCellRenderer());
        productTable.getColumnModel().getColumn(7).setCellEditor(new ButtonCellEditor(this)); 
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Default cursor unless hovering over specific UI elements that change it
                productTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });


        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.getViewport().setBackground(new Color(134, 100, 79));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        dashboardPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Buttons Panel Section ---
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        buttonsPanel.setBackground(new Color(67, 42, 32));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Add New Product Button
        BoneButton addProductButton = new BoneButton("Add new product", BONE_ADD_PRODUCT_IMAGE_PATH);
        buttonsPanel.add(addProductButton);
        addProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Add new product button clicked!");
                // Open dialog in ADD mode (no product ID passed)
                ProductFormDialog addDialog = new ProductFormDialog(InventoryDashboard.this, null);
                addDialog.setVisible(true);
                loadProductData(); // Refresh data after dialog closes
            }
        });

        // Supplier List Button
        BoneButton supplierListButton = new BoneButton("Supplier List", BONE_SUPPLIER_LIST_IMAGE_PATH);
        buttonsPanel.add(supplierListButton);
        supplierListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Supplier List button clicked!");
                // Hide current dashboard and show Supplier frame
                setVisible(false);
                Supplier supplierFrame = new Supplier(InventoryDashboard.this); // Pass reference to this dashboard
                supplierFrame.setVisible(true);
            }
        });

        // Generate Report Button
        BoneButton generateReportButton = new BoneButton("Generate report", BONE_GENERATE_REPORT_IMAGE_PATH);
        buttonsPanel.add(generateReportButton);
        generateReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Generate report button clicked!");
                setVisible(false); // Hide current dashboard
                ReportGenerator reportFrame = new ReportGenerator(InventoryDashboard.this); // Pass reference to this dashboard
                reportFrame.setVisible(true);
            }
        });


        dashboardPanel.add(buttonsPanel, BorderLayout.SOUTH);
        add(dashboardPanel);

        loadProductData();
    }

    private void loadProductData() {
        tableModel.setRowCount(0);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT id, product_name, product_id, category, price, stock, supplier FROM products ORDER BY id"; 
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            int rowNum = 1;
            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rowNum++);
                row.add(resultSet.getString("product_name"));
                row.add(resultSet.getString("product_id"));
                row.add(resultSet.getString("category"));
                row.add(resultSet.getString("price"));
                row.add(resultSet.getInt("stock"));
                row.add(resultSet.getString("supplier"));
                row.add("BUTTONS_PLACEHOLDER");
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading product data: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteProduct(String productId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM products WHERE product_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, productId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Product '" + productId + "' deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadProductData(); // Refresh table after deletion
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product '" + productId + "'. Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error during deletion: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new InventoryDashboard().setVisible(true);
            }
        });
    }

    class BoneButton extends JButton {
        private ImageIcon buttonImageIcon;
        private String buttonText;
        private boolean imageLoadedSuccessfully = false; // Flag to track successful image loading

        public BoneButton(String text, String imagePath) {
            super(text); // Always set text initially
            this.buttonText = text;

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);

            setFont(new Font("Arial", Font.BOLD, 16));
            setCursor(new Cursor(Cursor.HAND_CURSOR)); // Set hand cursor for all BoneButtons

            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl != null) {
                ImageIcon tempIcon = new ImageIcon(imageUrl);
                setPreferredSize(new Dimension(160, 65)); 
                
                Image tempImage = tempIcon.getImage();
                MediaTracker mt = new MediaTracker(this);
                mt.addImage(tempImage, 1);
                try {
                    mt.waitForAll();
                    if (mt.isErrorAny() || (tempImage.getWidth(null) == -1 && tempImage.getHeight(null) == -1)) {
                         throw new Exception("Image loading error detected or image is invalid for: " + imagePath);
                    }
                    System.out.println("BoneButton: Image loaded successfully for: " + imagePath + ". Size: " + tempImage.getWidth(null) + "x" + tempImage.getHeight(null)); // Debug print
                    buttonImageIcon = new ImageIcon(tempImage.getScaledInstance(
                        getPreferredSize().width, getPreferredSize().height, Image.SCALE_SMOOTH));
                    imageLoadedSuccessfully = true;
                    setText(""); 
                } catch (Exception ex) {
                    System.err.println("Error during image loading (MediaTracker): " + ex.getMessage());
                    // imageLoadedSuccessfully remains false, so text will be drawn by super.paintComponent
                }
            } else {
                System.err.println("Warning: Bone button image '" + imagePath + "' not found. Falling back to default button appearance (with text).");
                // imageLoadedSuccessfully remains false, so text will be drawn by super.paintComponent
            }

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (imageLoadedSuccessfully) {
                        // Implement visual hover effect for image-based button here if needed (e.g., swapping to a hover image)
                    } else { 
                        // Fallback hover effect if image not loaded
                        setBackground(new Color(255, 140, 0));
                        setOpaque(true); // Make opaque for background color to show
                        setContentAreaFilled(true);
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (imageLoadedSuccessfully) {
                        // Revert visual hover effect for image-based button here
                    } else {
                        // Revert fallback hover effect
                        setBackground(new Color(210, 105, 30));
                        setOpaque(false); // Revert to non-opaque after hover
                        setContentAreaFilled(false);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            // If image loaded successfully, draw image first
            if (imageLoadedSuccessfully && buttonImageIcon != null) {
                // Draw the image, scaled to fill the entire button bounds
                buttonImageIcon.paintIcon(this, g, 0, 0); 
            }
            // Then let super paint (text if any, or default background if opaque)
            super.paintComponent(g); 
        }

        @Override
        protected void paintBorder(Graphics g) {
            // Only paint border if not using image background (i.e., image failed to load)
            if (!imageLoadedSuccessfully) {
                super.paintBorder(g);
            }
        }
    }

    class ProductFormDialog extends JDialog {
        private JTextField productNameField;
        private JTextField productIdField;
        private JComboBox<String> categoryComboBox;
        private JTextField priceField;
        private JTextField quantityField;
        private JComboBox<String> supplierComboBox;
        private JLabel dialogTitleLabel; // Reference to the title label
        private String originalProductId = null; // Stores ID if in edit mode, for UPDATE query

        public ProductFormDialog(JFrame parent, String productIdToEdit) {
            super(parent, "", true); // Set title dynamically below, true makes it modal
            
            // Set mode based on whether productIdToEdit is provided
            boolean isEditMode = (productIdToEdit != null && !productIdToEdit.isEmpty());
            this.originalProductId = productIdToEdit; // Store for update operations

            if (isEditMode) {
                setTitle("Edit Product");
            } else {
                setTitle("Add Product");
            }

            // Reduced height to make the bottom shorter, allowing for tight packing
            setSize(400, 430); // Adjusted height further
            setLocationRelativeTo(parent);

            JPanel dialogPanel = new JPanel(new GridBagLayout());
            dialogPanel.setBackground(new Color(134, 100, 79)); // Corrected background color for the main panel
            dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));


            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 5, 8, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // --- Title Panel Section ---
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setBackground(new Color(67, 42, 32));
            titlePanel.setPreferredSize(new Dimension(360, 60));
            titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            titlePanel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Make title bar draggable with hand cursor

            dialogTitleLabel = new JLabel(getTitle(), SwingConstants.CENTER); // Use getTitle() for dynamic text
            dialogTitleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            dialogTitleLabel.setForeground(Color.WHITE); // White text for contrast on dark brown
            titlePanel.add(dialogTitleLabel, BorderLayout.CENTER);

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0; // Title row doesn't grow vertically
            gbc.insets = new Insets(0, 0, 15, 0);
            dialogPanel.add(titlePanel, gbc);
            // --- End Title Panel Section ---


            // Reset gbc for form fields
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0; // Form field rows don't grow vertically
            gbc.insets = new Insets(8, 5, 8, 5);

            // Product Name Label
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel productNameLabel = new JLabel("Product name:");
            productNameLabel.setForeground(Color.BLACK); // Set label text color to black
            dialogPanel.add(productNameLabel, gbc);
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            productNameField = new JTextField(15);
            productNameField.setPreferredSize(new Dimension(200, 28));
            dialogPanel.add(productNameField, gbc);

            // Product ID Label
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel productIdLabel = new JLabel("Product_Id:");
            productIdLabel.setForeground(Color.BLACK); // Set label text color to black
            dialogPanel.add(productIdLabel, gbc);
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            productIdField = new JTextField(15);
            productIdField.setPreferredSize(new Dimension(200, 28));
            // Disable editing Product ID if in edit mode to prevent changing primary key directly
            if (isEditMode) {
                productIdField.setEditable(false); 
                productIdField.setBackground(new Color(200, 200, 200)); // Grey out if not editable
            }
            dialogPanel.add(productIdField, gbc);

            // Category Label
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel categoryLabel = new JLabel("Category:");
            categoryLabel.setForeground(Color.BLACK); // Set label text color to black
            dialogPanel.add(categoryLabel, gbc);
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            String[] categories = {"Accessory", "Food", "Toy", "Health"};
            categoryComboBox = new JComboBox<>(categories);
            categoryComboBox.setPreferredSize(new Dimension(200, 28));
            // --- Set background for JComboBox to white ---
            categoryComboBox.setBackground(Color.WHITE); 
            dialogPanel.add(categoryComboBox, gbc);

            // Price Label
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel priceLabel = new JLabel("Price: ");
            priceLabel.setForeground(Color.BLACK); // Set label text color to black
            dialogPanel.add(priceLabel, gbc);
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            priceField = new JTextField(15);
            priceField.setPreferredSize(new Dimension(200, 28));
            dialogPanel.add(priceField, gbc);

            // Quantity Label
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel quantityLabel = new JLabel("Quantity:");
            quantityLabel.setForeground(Color.BLACK); // Set label text color to black
            dialogPanel.add(quantityLabel, gbc);
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            quantityField = new JTextField(15);
            quantityField.setPreferredSize(new Dimension(200, 28));
            dialogPanel.add(quantityField, gbc);

            // Supplier Label
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel supplierLabel = new JLabel("Supplier:");
            supplierLabel.setForeground(Color.BLACK); // Set label text color to black
            dialogPanel.add(supplierLabel, gbc);
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            // Fetch suppliers dynamically
            List<String> supplierNames = getSupplierNamesFromDatabase();
            supplierComboBox = new JComboBox<>(supplierNames.toArray(new String[0]));
            supplierComboBox.setPreferredSize(new Dimension(200, 28));
            // --- Set background for JComboBox to white ---
            supplierComboBox.setBackground(Color.WHITE); 
            dialogPanel.add(supplierComboBox, gbc);

            // Buttons Panel (Save, Cancel)
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); // FlowLayout for side-by-side buttons
            buttonPanel.setBackground(new Color(134, 100, 79)); // Match dialog background

            // Replaced BoneButton with regular JButton
            JButton saveButton = new JButton("Save");
            saveButton.setFont(new Font("Arial", Font.BOLD, 14));
            saveButton.setBackground(new Color(60, 179, 113)); // Medium Sea Green
            saveButton.setForeground(Color.WHITE);
            saveButton.setFocusPainted(false);
            saveButton.setBorder(BorderFactory.createLineBorder(new Color(46, 139, 87), 2));
            saveButton.setPreferredSize(new Dimension(100, 35)); // Consistent size with LoginScreen buttons
            saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Set pointer cursor
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveProduct(isEditMode); // Pass the mode to saveProduct
                }
            });
            buttonPanel.add(saveButton);

            // Replaced BoneButton with regular JButton
            JButton cancelButton = new JButton("Cancel"); // The Cancel button, re-added with styling
            cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
            cancelButton.setBackground(new Color(220, 20, 60)); // Crimson
            cancelButton.setForeground(Color.WHITE);
            cancelButton.setFocusPainted(false);
            cancelButton.setBorder(BorderFactory.createLineBorder(new Color(180, 0, 40), 2));
            cancelButton.setPreferredSize(new Dimension(100, 35)); // Consistent size
            cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Set pointer cursor
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose(); // Close the dialog
                }
            });
            buttonPanel.add(cancelButton); // Add the cancel button to the panel

            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.HORIZONTAL; // <--- CHANGE: Allow buttonPanel to stretch horizontally
            gbc.weightx = 1.0; // <--- ADD: Allow buttonPanel to absorb horizontal space
            gbc.weighty = 0.0; // Row does not grow vertically
            gbc.insets = new Insets(15, 0, 0, 0); // Keep bottom padding if desired
            dialogPanel.add(buttonPanel, gbc);

            Container contentPane = getContentPane();
            contentPane.setLayout(new BorderLayout());
            // This line ensures the outermost dialog content pane has the correct background
            contentPane.setBackground(new Color(134, 100, 79)); 
            contentPane.add(dialogPanel, BorderLayout.CENTER);

            setUndecorated(true);
            getRootPane().setBorder(BorderFactory.createLineBorder(new Color(67, 42, 32), 5));
            
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

            // If in edit mode, load existing product data
            if (isEditMode) {
                loadProductForEdit(productIdToEdit);
            }
        }

        private void loadProductForEdit(String productId) {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "SELECT product_name, product_id, category, price, stock, supplier FROM products WHERE product_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, productId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    productNameField.setText(resultSet.getString("product_name"));
                    productIdField.setText(resultSet.getString("product_id"));
                    categoryComboBox.setSelectedItem(resultSet.getString("category"));
                    priceField.setText(resultSet.getString("price"));
                    quantityField.setText(String.valueOf(resultSet.getInt("stock")));
                    supplierComboBox.setSelectedItem(resultSet.getString("supplier"));
                } else {
                    JOptionPane.showMessageDialog(this, "Product with ID " + productId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    dispose(); // Close dialog if product not found
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Database error loading product: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                dispose(); // Close dialog on database error
            }
        }

        private void saveProduct(boolean isEditMode) {
            String productName = productNameField.getText().trim();
            String productId = productIdField.getText().trim(); // This will be the new ID if adding, or original if editing
            String category = (String) categoryComboBox.getSelectedItem();
            String price = priceField.getText().trim();
            String quantityStr = quantityField.getText().trim();
            String supplier = (String) supplierComboBox.getSelectedItem();

            if (productName.isEmpty() || productId.isEmpty() || price.isEmpty() || quantityStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Quantity must be a valid number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql;
                if (isEditMode) {
                    // Update existing product
                    sql = "UPDATE products SET product_name = ?, category = ?, price = ?, stock = ?, supplier = ? WHERE product_id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, productName);
                    preparedStatement.setString(2, category);
                    preparedStatement.setString(3, price);
                    preparedStatement.setInt(4, quantity);
                    preparedStatement.setString(5, supplier);
                    preparedStatement.setString(6, originalProductId); // Use originalProductId for WHERE clause
                    
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Product updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dispose(); // Close the dialog after successful save
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update product. Product ID '" + originalProductId + "' not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Insert new product
                    sql = "INSERT INTO products (product_name, product_id, category, price, stock, supplier) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, productName);
                    preparedStatement.setString(2, productId); // New product ID
                    preparedStatement.setString(3, category);
                    preparedStatement.setString(4, price);
                    preparedStatement.setInt(5, quantity);
                    preparedStatement.setString(6, supplier);

                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Product added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dispose(); // Close the dialog after successful save
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to add product.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                // Check for duplicate entry error specifically for product_id
                if (ex.getSQLState().startsWith("23")) { // SQLState for integrity constraint violation (e.g., 23000, 23505)
                    JOptionPane.showMessageDialog(this,
                            "Product ID '" + productId + "' already exists. Please use a unique ID.",
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

        private List<String> getSupplierNamesFromDatabase() {
            List<String> supplierNames = new ArrayList<>();
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "SELECT supplier_name FROM suppliers ORDER BY supplier_name";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    supplierNames.add(resultSet.getString("supplier_name"));
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading supplier names: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
            return supplierNames;
        }
    }

    class ButtonCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton editButton;
        private final JButton deleteButton;

        public ButtonCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0)); // Align buttons horizontally, small gap
            setOpaque(true); // Ensure background is painted

            // Edit Button
            editButton = new JButton("Edit");
            editButton.setFont(new Font("Arial", Font.BOLD, 12));
            editButton.setBackground(new Color(70, 130, 180)); // Steel Blue
            editButton.setForeground(Color.WHITE);
            editButton.setFocusPainted(false);
            editButton.setBorderPainted(false);
            editButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor on hover
            editButton.setPreferredSize(new Dimension(60, 24)); // Smaller size for table cell
            add(editButton);

            // Delete Button
            deleteButton = new JButton("Delete");
            deleteButton.setFont(new Font("Arial", Font.BOLD, 12));
            deleteButton.setBackground(new Color(220, 20, 60)); // Crimson
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);
            deleteButton.setBorderPainted(false);
            deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor on hover
            deleteButton.setPreferredSize(new Dimension(70, 24)); // Slightly wider for "Delete"
            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            // Set panel background to match table row selection
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                editButton.setBackground(new Color(100, 150, 200)); // Slightly different color when selected
                deleteButton.setBackground(new Color(240, 50, 80));
            } else {
                setBackground(table.getBackground());
                editButton.setBackground(new Color(70, 130, 180)); // Reset to default colors
                deleteButton.setBackground(new Color(220, 20, 60));
            }
            return this;
        }
    }

    class ButtonCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private JPanel panel;
        private JButton editButton;
        private JButton deleteButton;
        private Object cellValue; // Stores the value of the cell being edited (e.g., product ID for action)
        private JTable table; // Reference to the table
        private int row; // Current row being edited
        private InventoryDashboard parentDashboard; // Reference to the main dashboard for calling methods

        public ButtonCellEditor(InventoryDashboard parentDashboard) {
            this.parentDashboard = parentDashboard;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setOpaque(true);

            // Edit Button
            editButton = new JButton("Edit");
            editButton.setFont(new Font("Arial", Font.BOLD, 12));
            editButton.setBackground(new Color(70, 130, 180)); // Steel Blue
            editButton.setForeground(Color.WHITE);
            editButton.setFocusPainted(false);
            editButton.setBorderPainted(false);
            editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            editButton.setPreferredSize(new Dimension(60, 24));
            editButton.setActionCommand("edit"); // Set action command
            editButton.addActionListener(this); // Register editor as listener

            // Delete Button
            deleteButton = new JButton("Delete");
            deleteButton.setFont(new Font("Arial", Font.BOLD, 12));
            deleteButton.setBackground(new Color(220, 20, 60)); // Crimson
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);
            deleteButton.setBorderPainted(false);
            deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deleteButton.setPreferredSize(new Dimension(70, 24));
            deleteButton.setActionCommand("delete"); // Set action command
            deleteButton.addActionListener(this); // Register editor as listener

            panel.add(editButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.table = table;
            this.row = row;
            this.cellValue = value; // Although "BUTTONS_PLACEHOLDER", we need the product ID from the table model

            // Set panel background to match table row selection
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
                editButton.setBackground(new Color(100, 150, 200));
                deleteButton.setBackground(new Color(240, 50, 80));
            } else {
                panel.setBackground(table.getBackground());
                editButton.setBackground(new Color(70, 130, 180));
                deleteButton.setBackground(new Color(220, 20, 60));
            }
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return cellValue; // Return the original cell value
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped(); // Stop editing when a button is clicked

            // Get the product ID from the correct column in the table model
            String productId = (String) table.getModel().getValueAt(row, 2); // Assuming Product_Id is at index 2

            if ("edit".equals(e.getActionCommand())) {
                parentDashboard.new ProductFormDialog(parentDashboard, productId).setVisible(true);
                parentDashboard.loadProductData(); // Refresh data after dialog closes
            } else if ("delete".equals(e.getActionCommand())) {
                int confirm = JOptionPane.showConfirmDialog(parentDashboard,
                        "Are you sure you want to delete product with ID: " + productId + "?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    parentDashboard.deleteProduct(productId);
                }
            }
        }
    }
}
