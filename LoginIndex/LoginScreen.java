import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.net.URL;

public class LoginScreen extends JFrame {

    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/memistore";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    // GUI components for Login Panel
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;

    // Panels for switching views
    private JPanel bootUpPanel;
    private JPanel loginPanel;

    private static final String BOOTUP_IMAGE_PATH = "/images/CollarWithNameColor.png";
    private static final String LOGIN_HEADER_IMAGE_PATH = "/images/CollarWithNameColor.png"; // Still needed for the login form image

    /**
     * Constructor for the LoginScreen class.
     * Initializes the GUI components and sets up the layout.
     */
    public LoginScreen() {
        // Set up the JFrame (window) properties
        setTitle("Memi's Treats - Inventory Management System"); // Window title
        setSize(900, 650); // Set window size to 900x600
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close operation
        setLocationRelativeTo(null); // Center the window on the screen
        setResizable(false); // Prevent resizing for a fixed layout

        // Use CardLayout to switch between the boot-up screen and login screen
        setLayout(new CardLayout());

        // --- Create the Boot Up Panel with an image background ---
        Image bootUpBgImage = loadImage(BOOTUP_IMAGE_PATH);
        bootUpPanel = new ImagePanel(bootUpBgImage); // Use the custom ImagePanel
        bootUpPanel.setLayout(new BorderLayout()); // Set layout for components on top of image
        bootUpPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20)); // Padding

        // Add a message for user interaction
        JLabel instructionLabel = new JLabel("Click anywhere or press any key to continue...", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        instructionLabel.setForeground(new Color(255, 255, 255));
        bootUpPanel.add(instructionLabel, BorderLayout.SOUTH);

        // Add mouse listener to transition to login screen on click
        bootUpPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showLoginPanel();
            }
        });

        // Add key listener to transition to login screen on key press
        bootUpPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                showLoginPanel();
            }
        });
        bootUpPanel.setFocusable(true); // Make the panel focusable for key events


        // --- Create the Login Panel ---
        loginPanel = new JPanel(new BorderLayout());
        loginPanel.setBackground(new Color(255, 224, 160)); // Set background for the overall login panel

        // Login Form Panel (for image, username, password, and button)
        JPanel loginFormPanel = new JPanel(new GridBagLayout());
        loginFormPanel.setBackground(new Color(255, 224, 160)); 
        loginFormPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40)); // Padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5); // Padding between components
        // gbc.fill is intentionally NOT set here, so default behavior is NONE,
        // preventing stretching unless explicitly specified per component.


        // --- Add the image directly to the loginFormPanel ---
        Image loginHeaderImage = loadImage(LOGIN_HEADER_IMAGE_PATH);
        if (loginHeaderImage != null) {
            ImageIcon loginImageRawIcon = new ImageIcon(loginHeaderImage);
            // Manually adjust the size of the image
            Image scaledImage = loginImageRawIcon.getImage().getScaledInstance(500, 350, Image.SCALE_SMOOTH); // Adjust size manually here
            ImageIcon scaledLoginIcon = new ImageIcon(scaledImage);

            JLabel loginImageLabel = new JLabel(scaledLoginIcon);
            gbc.gridx = 0; // Column 0
            gbc.gridy = 0; // Row 0
            gbc.gridwidth = 2; // Span across 2 columns
            gbc.anchor = GridBagConstraints.CENTER; // Center the image
            loginFormPanel.add(loginImageLabel, gbc);
        } else {
            System.err.println("Login header image not found, defaulting to text label.");
            // If image not found, you could add a text label here as a fallback if desired
            // JLabel fallbackLabel = new JLabel("Login Panel", SwingConstants.CENTER);
            // fallbackLabel.setFont(new Font("Arial", Font.BOLD, 24));
            // gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
            // loginFormPanel.add(fallbackLabel, gbc);
        }


        // Username Label
        gbc.gridx = 0; // Column 0
        gbc.gridy = 1; // Row 1 (shifted down by 1 for the image)
        gbc.gridwidth = 1; // Keep 1 for label
        gbc.fill = GridBagConstraints.NONE; // Explicitly set to NONE for labels
        gbc.anchor = GridBagConstraints.EAST; // Align to left
        gbc.weightx = 0.0;
        loginFormPanel.add(new JLabel("Username:"), gbc);

        // Username Text Field
        gbc.gridx = 1; // Column 1
        gbc.gridy = 1; // Row 1
        gbc.gridwidth = 1; // Keep 1 for text field
        gbc.weightx = 0.0; // Give horizontal space to this column
        gbc.fill = GridBagConstraints.HORIZONTAL; // Allow text field to fill its column
        loginFormPanel.add(usernameField = new JTextField(20), gbc);

        // Password Label
        gbc.gridx = 0; // Column 0
        gbc.gridy = 2; // Row 2 (shifted down by 1)
        gbc.gridwidth = 1; // Keep 1 for label
        gbc.fill = GridBagConstraints.NONE; // Explicitly set to NONE for labels
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.0;
        loginFormPanel.add(new JLabel("Password:"), gbc);

        // Password Text Field
        gbc.gridx = 1; // Column 1
        gbc.gridy = 2; // Row 2
        gbc.gridwidth = 1; // Keep 1 for text field
        gbc.weightx = 0.0; // Give horizontal space to this column
        gbc.fill = GridBagConstraints.HORIZONTAL; // Allow text field to fill its column
        loginFormPanel.add(passwordField = new JPasswordField(20), gbc);

        // Login Button
        gbc.gridx = 0; // Column 0
        gbc.gridy = 3; // Row 3 (shifted down by 1)
        gbc.gridwidth = 2; // Span across 2 columns (for centering)
        gbc.anchor = GridBagConstraints.CENTER; // Center the button
        gbc.fill = GridBagConstraints.NONE; // Explicitly set to NONE for the button (to respect preferred size)
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14)); // Bold font for button
        loginButton.setBackground(new Color(60, 179, 113)); // Medium Sea Green
        loginButton.setForeground(Color.WHITE); // White text
        loginButton.setFocusPainted(false); // Remove focus border
        loginButton.setBorder(BorderFactory.createLineBorder(new Color(46, 139, 87), 2)); // Darker border
        loginButton.setPreferredSize(new Dimension(100, 35)); // Fixed size
        loginFormPanel.add(loginButton, gbc);

        // Message Label (for displaying success/error messages)
        gbc.gridx = 0; // Column 0
        gbc.gridy = 4; // Row 4 (shifted down by 1)
        gbc.gridwidth = 2; // Span across 2 columns
        gbc.fill = GridBagConstraints.NONE; // Explicitly set to NONE for the message label
        messageLabel = new JLabel("", SwingConstants.CENTER); // Center text
        messageLabel.setForeground(Color.RED); // Default to red for errors
        loginFormPanel.add(messageLabel, gbc);

        // Add the loginFormPanel to the center of the loginPanel
        loginPanel.add(loginFormPanel, BorderLayout.CENTER);

        // Add both panels to the frame using CardLayout
        add(bootUpPanel, "BootUp");
        add(loginPanel, "Login");

        // Initially show the boot up panel
        CardLayout cl = (CardLayout)(this.getContentPane().getLayout());
        cl.show(this.getContentPane(), "BootUp");

        // Request focus for the bootUpPanel so key events are captured immediately
        bootUpPanel.requestFocusInWindow();
    }

    /**
     * Helper method to load an image from a given path.
     * @param path The path to the image resource.
     * @return The loaded Image object, or null if not found.
     */
    private Image loadImage(String path) {
        URL imageUrl = getClass().getResource(path);
        if (imageUrl != null) {
            return new ImageIcon(imageUrl).getImage();
        } else {
            System.err.println("Warning: Image '" + path + "' not found. Check its path.");
            return null;
        }
    }

    /**
     * Shows the login panel and hides the boot up panel.
     */
    private void showLoginPanel() {
        CardLayout cl = (CardLayout)(this.getContentPane().getLayout());
        cl.show(this.getContentPane(), "Login");
        usernameField.requestFocusInWindow(); // Set focus to username field
    }

    /**
     * Authenticates the user against the database.
     */
    private void authenticateUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            messageLabel.setForeground(Color.RED);
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                messageLabel.setText("Login Successful! Redirecting to dashboard...");
                messageLabel.setForeground(new Color(34, 139, 34));
                JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // In a real application, you would dispose of this window and open the main dashboard
                // For example:
                // dispose();
                // new InventoryDashboard().setVisible(true);
            } else {
                messageLabel.setText("Invalid username or password.");
                messageLabel.setForeground(Color.RED);
            }
        } catch (SQLException ex) {
            messageLabel.setText("Database error: " + ex.getMessage());
            messageLabel.setForeground(Color.RED);
            ex.printStackTrace();
        }
    }

    /**
     * Main method to run the application.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginScreen().setVisible(true);
            }
        });
    }

    /**
     * Inner class to create a JPanel that paints an image as its background.
     */
    class ImagePanel extends JPanel {
        private Image image;

        public ImagePanel(Image image) {
            this.image = image;
            // Ensure the panel is opaque so the background image is drawn
            setOpaque(false);
            // Set preferred size for consistency, if the image has a preferred size
            // If the image is meant to fill, this might not be needed or will be overridden by layout
            if (image != null) {
                // Adjust for actual image dimensions, or set a fixed preferred size
                // Example: setPreferredSize(new Dimension(image.getWidth(this), image.getHeight(this)));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                // Draw the image, scaled to fill the entire panel
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
