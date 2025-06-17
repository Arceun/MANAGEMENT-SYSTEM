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

    private static final String DB_URL = "jdbc:mysql://localhost:3306/memistore"; 
    private static final String DB_USER = "root"; 
    private static final String DB_PASSWORD = ""; 

    // GUI components for Login Panel
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private JButton loginButton;
    private final JLabel messageLabel;

    // Panels for switching views
    private final JPanel bootUpPanel;
    private final JPanel loginPanel;

    private static final String BOOTUP_IMAGE_PATH = "/images/CollarWithNameColor.png";
    private static final String LOGIN_HEADER_IMAGE_PATH = "/images/CollarWithNameColor.png";

    
    public LoginScreen() {
        System.out.println("--- LoginScreen constructor started ---"); 

        // Set up the JFrame (window) properties
        setTitle("Memi's Treats - Inventory Management System"); 
        setSize(900, 650); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close operation
        setLocationRelativeTo(null); // Center the window on the screen
        setResizable(false); // Prevent resizing for a fixed layout

        // Use CardLayout to switch between the boot-up screen and login screen
        setLayout(new CardLayout());

        // --- Create the Boot Up Panel with an image background ---
        Image bootUpBgImage = loadImage(BOOTUP_IMAGE_PATH);
        bootUpPanel = new ImagePanel(bootUpBgImage); 
        bootUpPanel.setLayout(new BorderLayout()); 
        bootUpPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20)); 

        // Add a message for user interaction
        JLabel instructionLabel = new JLabel("Click anywhere or press any key to continue...", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        instructionLabel.setForeground(new Color(0, 0, 0));
        bootUpPanel.add(instructionLabel, BorderLayout.SOUTH);

        // Add mouse listener to transition to login screen on click
        bootUpPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("BootUpPanel clicked!"); // Debug print
                showLoginPanel();
            }
        });

        // Add key listener to transition to login screen on key press
        bootUpPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("BootUpPanel key pressed!"); // Debug print
                showLoginPanel();
            }
        });
        bootUpPanel.setFocusable(true); // Make the panel focusable for key events


        // --- Create the Login Panel ---
        loginPanel = new JPanel(new BorderLayout());
        loginPanel.setBackground(new Color(255, 224, 160));

        // Login Form Panel (for image, username, password, and button)
        JPanel loginFormPanel = new JPanel(new GridBagLayout());
        loginFormPanel.setBackground(new Color(255, 224, 160));
        loginFormPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40)); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5); 


        // --- Add the image directly to the loginFormPanel ---
        Image loginHeaderImage = loadImage(LOGIN_HEADER_IMAGE_PATH);
        if (loginHeaderImage != null) {
            ImageIcon loginImageRawIcon = new ImageIcon(loginHeaderImage);
            Image scaledImage = loginImageRawIcon.getImage().getScaledInstance(500, 350, Image.SCALE_SMOOTH); 
            ImageIcon scaledLoginIcon = new ImageIcon(scaledImage);

            JLabel loginImageLabel = new JLabel(scaledLoginIcon);
            gbc.gridx = 0; // Column 0
            gbc.gridy = 0; // Row 0
            gbc.gridwidth = 2; // Span across 2 columns
            gbc.anchor = GridBagConstraints.CENTER; // Center the image
            gbc.weightx = 0.0; 
            gbc.fill = GridBagConstraints.NONE; // Keep image from stretching
            loginFormPanel.add(loginImageLabel, gbc);
        } else {
            System.err.println("Login header image not found, defaulting to text label.");
            // Fallback if image not found, added for robustness
            JLabel fallbackLabel = new JLabel("Login Panel", SwingConstants.CENTER);
            fallbackLabel.setFont(new Font("Arial", Font.BOLD, 24));
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
            loginFormPanel.add(fallbackLabel, gbc);
        }


        // Username Label
        gbc.gridx = 0; // Column 0
        gbc.gridy = 1; // Row 1 
        gbc.gridwidth = 1; // Label takes 1 column
        gbc.fill = GridBagConstraints.NONE; 
        gbc.anchor = GridBagConstraints.EAST; 
        gbc.weightx = 0.0; 
        loginFormPanel.add(new JLabel("Username:"), gbc);

        // Username Text Field
        gbc.gridx = 1; // Column 1
        gbc.gridy = 1; // Row 1
        gbc.gridwidth = 1; // Text field takes 1 column
        gbc.weightx = 0.0; 
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        usernameField = new JTextField(20); 
        loginFormPanel.add(usernameField, gbc);

        // Password Label
        gbc.gridx = 0; // Column 0
        gbc.gridy = 2; // Row 2 (shifted down by 1)
        gbc.gridwidth = 1; // Label takes 1 column
        gbc.fill = GridBagConstraints.NONE; // Label itself doesn't stretch
        gbc.anchor = GridBagConstraints.EAST; 
        gbc.weightx = 0.0; 
        loginFormPanel.add(new JLabel("Password:"), gbc);

        // Password Text Field
        gbc.gridx = 1; // Column 1
        gbc.gridy = 2; // Row 2
        gbc.gridwidth = 1; // Text field takes 1 column
        gbc.weightx = 0.0; 
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        passwordField = new JPasswordField(20);
        loginFormPanel.add(passwordField, gbc);

        // Login Button
        gbc.gridx = 0; // Column 0
        gbc.gridy = 3; // Row 3 (shifted down by 1)
        gbc.gridwidth = 2; // Span across 2 columns (for centering)
        gbc.anchor = GridBagConstraints.CENTER; // Center the button
        gbc.fill = GridBagConstraints.NONE; // Button itself doesn't stretch
        gbc.weightx = 0.0; 
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14)); 
        loginButton.setBackground(new Color(255, 255, 255));
        loginButton.setForeground(Color.BLACK); 
        loginButton.setFocusPainted(false); // Remove focus border
        loginButton.setBorder(BorderFactory.createLineBorder(new Color(46, 139, 87), 2)); 
        loginButton.setPreferredSize(new Dimension(100, 35)); 
        loginFormPanel.add(loginButton, gbc);


        // --- Place the action listener right after button initialization ---
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Login button clicked - Action Listener Fired!"); 
                authenticateUser();
            }
        });

        // --- Add KeyListener to the password field ---
        passwordField.addKeyListener(new KeyAdapter() { 
            @Override
            public void keyPressed(KeyEvent e) {
                // When the Enter key is pressed
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginButton.doClick(); 
                }
            }
        });

        usernameField.addKeyListener(new KeyAdapter() { 
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocusInWindow(); 
                }
            }
        });

        // Message Label 
        gbc.gridx = 0; // Column 0
        gbc.gridy = 4; // Row 4 (shifted down by 1)
        gbc.gridwidth = 2; // Span across 2 columns
        gbc.fill = GridBagConstraints.NONE; 
        gbc.weightx = 0.0;
        messageLabel = new JLabel("", SwingConstants.CENTER); // Center text
        messageLabel.setForeground(Color.RED); 
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
        System.out.println("--- LoginScreen constructor finished ---");
    }

    private Image loadImage(String path) {
        URL imageUrl = getClass().getResource(path);
        if (imageUrl != null) {
            return new ImageIcon(imageUrl).getImage();
        } else {
            System.err.println("Warning: Image '" + path + "' not found. Check its path.");
            return null;
        }
    }

    /* Shows the login panel and hides the boot up panel. */
    private void showLoginPanel() {
        CardLayout cl = (CardLayout)(this.getContentPane().getLayout());
        cl.show(this.getContentPane(), "Login");
        usernameField.requestFocusInWindow(); 
        System.out.println("--- Switched to Login Panel ---"); 
    }

    /**
     * Authenticates the user against the database.
     */
    private void authenticateUser() {
        System.out.println("--- authenticateUser() called ---"); // Debug print

        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username or password fields are empty."); // Debug print
            messageLabel.setText("Please enter both username and password.");
            messageLabel.setForeground(Color.RED);
            return;
        }

        System.out.println("Attempting database connection..."); // Debug print
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Database connection successful. Preparing statement."); // Debug print
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            System.out.println("Executing query: " + sql + " with username: " + username + " and password: " + password); // Debug print
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Login successful in database. Redirecting to dashboard."); // Debug print
                messageLabel.setText("Login Successful! Redirecting to dashboard...");
                messageLabel.setForeground(new Color(34, 139, 34));
                JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // --- REDIRECTION TO DASHBOARD ---
                dispose(); // Close the current LoginScreen window
                new InventoryDashboard().setVisible(true); // Open the new InventoryDashboard
                // --- END REDIRECTION ---

            } else {
                System.out.println("Login failed: Invalid credentials."); // Debug print
                messageLabel.setText("Invalid username or password.");
                messageLabel.setForeground(Color.RED);
            }
        } catch (SQLException ex) {
            System.err.println("SQLException caught: " + ex.getMessage()); // Debug print
            messageLabel.setText("Database error: " + ex.getMessage());
            messageLabel.setForeground(Color.RED);
            ex.printStackTrace();
        }
        System.out.println("--- End of authenticateUser() ---"); // Debug print
    }

    public static void main(String[] args) {
        System.out.println("--- Main method started ---"); // Debug print
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginScreen().setVisible(true);
            }
        });
    }

    class ImagePanel extends JPanel {
        private Image image;

        public ImagePanel(Image image) {
            this.image = image;
            setOpaque(false); 
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
