
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

    // Database credentials - MAKE SURE TO UPDATE THESE WITH YOUR OWN!
    private static final String DB_URL = "jdbc:mysql://localhost:3306/memistore";
    private static final String DB_USER = "root"; // Your MySQL username
    private static final String DB_PASSWORD = ""; // Your MySQL password

    // GUI components for Login Panel
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;

    // Panels for switching views
    private JPanel bootUpPanel;
    private JPanel loginPanel;

    /**
     * Constructor for the LoginScreen class.
     * Initializes the GUI components and sets up the layout.
     */
    public LoginScreen() {
        // Set up the JFrame (window) properties
        setTitle("Memi's Treats - Inventory Management System"); // Window title
        setSize(400, 300); // Window size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close operation
        setLocationRelativeTo(null); // Center the window on the screen
        setResizable(false); // Prevent resizing for a fixed layout

        // Use CardLayout to switch between the boot-up screen and login screen
        setLayout(new CardLayout());

        // --- Create the Boot Up Panel ---
        bootUpPanel = new JPanel(new BorderLayout());
        bootUpPanel.setBackground(new Color(255, 224, 160));
        bootUpPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20)); // Padding

        JLabel bootUpLabel = new JLabel("Memi's Treats", SwingConstants.CENTER);
        bootUpLabel.setFont(new Font("Arial", Font.BOLD, 40));
        bootUpLabel.setForeground(new Color(139, 69, 19)); // Saddle Brown
        bootUpPanel.add(bootUpLabel, BorderLayout.CENTER);

        // Add a message for user interaction
        JLabel instructionLabel = new JLabel("Click anywhere or press any key to continue...", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        instructionLabel.setForeground(new Color(105, 105, 105)); // Dim Gray
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

        // Header Panel (for the logo/title and image on Login Screen)
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(255, 224, 160)); // Light blue background
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Padding, adjusted for image


        ImageIcon logoIcon = null;
        URL imageUrl = getClass().getResource("/images/collar.png"); // Path relative to current class
        if (imageUrl != null) {
            logoIcon = new ImageIcon(imageUrl);
            // Optionally, resize the image if it's too big/small
            Image image = logoIcon.getImage(); // transform it
            Image newimg = image.getScaledInstance(100, 100,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
            logoIcon = new ImageIcon(newimg);  // transform it back
        } else {
            System.err.println("Warning: Image 'Image not found' not found. Check its path.");
        }

        JLabel logoLabel;
        if (logoIcon != null) {
            // Create a label with both image and text
            logoLabel = new JLabel("Memi's Treats", logoIcon, SwingConstants.CENTER);
            logoLabel.setHorizontalTextPosition(SwingConstants.CENTER); // Text to the right of the image
            logoLabel.setVerticalTextPosition(SwingConstants.CENTER); // Vertically centered
        } else {
            // Fallback to text-only if image not found
            logoLabel = new JLabel("Memi's Treats", SwingConstants.CENTER);
        }
        logoLabel.setFont(new Font("Arial", Font.BOLD, 30)); // Larger, bold font
        logoLabel.setForeground(new Color(0, 100, 0)); // Dark green text
        headerPanel.add(logoLabel); // Add the logo label (with or without image) to the header
        loginPanel.add(headerPanel, BorderLayout.NORTH);

        // Login Form Panel (for username, password, and button)
        JPanel loginFormPanel = new JPanel(new GridBagLayout());
        loginFormPanel.setBackground(new Color(255, 224, 160)); // Alice Blue background
        loginFormPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40)); // Padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5); // Padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally

        // Username Label
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        loginFormPanel.add(new JLabel("Username:"), gbc);

        // Username Text Field
        gbc.gridx = 1; // Column 1
        gbc.gridy = 0; // Row 0
        usernameField = new JTextField(20); // 20 columns wide
        loginFormPanel.add(usernameField, gbc);

        // Password Label
        gbc.gridx = 0; // Column 0
        gbc.gridy = 1; // Row 1
        loginFormPanel.add(new JLabel("Password:"), gbc);

        // Password Text Field
        gbc.gridx = 1; // Column 1
        gbc.gridy = 1; // Row 1
        passwordField = new JPasswordField(20); // 20 columns wide
        loginFormPanel.add(passwordField, gbc);

        // Login Button
        gbc.gridx = 0; // Column 0
        gbc.gridy = 2; // Row 2
        gbc.gridwidth = 2; // Span across 2 columns
        gbc.anchor = GridBagConstraints.CENTER; // Center the button
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
        gbc.gridy = 3; // Row 3
        gbc.gridwidth = 2; // Span across 2 columns
        messageLabel = new JLabel("", SwingConstants.CENTER); // Center text
        messageLabel.setForeground(Color.RED); // Default to red for errors
        loginFormPanel.add(messageLabel, gbc);

        loginPanel.add(loginFormPanel, BorderLayout.CENTER);

        // Add action listener to the login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authenticateUser();
            }
        });

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
}
