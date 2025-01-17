import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.geom.Path2D;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main extends JFrame {
    private static final long serialVersionUID = 4648172894076113183L;
    private JTextField originalUrlField;
    private JTextField shortenedUrlField;
    private JLabel statusLabel;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String API_URL = "https://tinyurl.com/api-create.php?url=";
    public static final int HEIGHT = 225;
    public static final int WIDTH = 360;
    private double rotation = 0; // Rotation angle for the pentagram
    private Timer timer; // Timer for spinning effect

    public Main() {
        initUI();
        applyTheme();
        setTitle("Link Shortener");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        // Initialize and start the timer for spinning effect
        timer = new Timer(20, e -> {
            rotation += 0.05; // Increment rotation angle
            repaint(); // Repaint the frame to update the pentagram
        });
        timer.start(); // Start the timer
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create main panel with tight layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 20));

        // Create pentagram panel
        JPanel pentagramPanel = new JPanel() {
            private static final long serialVersionUID = 77091376395953152L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawPentagram(g2d, getWidth() / 2, getHeight() / 2, Math.min(getWidth(), getHeight()) / 2 - 10, rotation);
            }
        };

        pentagramPanel.setPreferredSize(new Dimension(120, 120));

        // Create panel for URL components
        JPanel urlPanel = new JPanel();
        urlPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Original URL input
        JLabel urlLabel = new JLabel("Enter URL:");
        originalUrlField = new JTextField(15); // Reduced width

        // Shorten button
        JButton shortenButton = new JButton("Shorten");
        shortenButton.addActionListener(e -> shortenUrl());

        // Shortened URL field with copy button
        shortenedUrlField = new JTextField(15); // Reduced width
        shortenedUrlField.setEditable(false);
        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> copyToClipboard());

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.GRAY);

        // Layout components with minimal spacing
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 3, 0);
        urlPanel.add(urlLabel, gbc);

        gbc.gridy = 1;
        urlPanel.add(originalUrlField, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(5, 0, 5, 0);
        urlPanel.add(shortenButton, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 3, 0);
        urlPanel.add(shortenedUrlField, gbc);

        gbc.gridy = 4;
        urlPanel.add(copyButton, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(5, 0, 0,  0);
        urlPanel.add(statusLabel, gbc);

        // Create a panel to hold both pentagram and URL panel
        JPanel combinedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        combinedPanel.add(pentagramPanel);
        combinedPanel.add(urlPanel);

        // Add combined panel to main panel
        mainPanel.add(combinedPanel, BorderLayout.WEST);

        add(mainPanel);
    }

    private void shortenUrl() {
        String originalUrl = originalUrlField.getText().trim();

        if (originalUrl.isEmpty()) {
            statusLabel.setText("Please enter a URL");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // Add http:// if no protocol is specified
        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            originalUrl = "http://" + originalUrl;
        }

        try {
            // Create HTTP request to TinyURL API
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + originalUrl))
                .GET()
                .build();

            // Send request asynchronously
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        SwingUtilities.invokeLater(() -> {
                            shortenedUrlField.setText(response.body());
                            statusLabel.setText("URL shortened successfully!");
                            statusLabel.setForeground(new Color(0, 150, 0));
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Error: Could not shorten URL");
                            statusLabel.setForeground(Color.RED);
                        });
                    }
                })
                .exceptionally(e -> {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Error: Network or service problem");
                        statusLabel.setForeground(Color.RED);
                    });
                    return null;
                });
        } catch (Exception e) {
            statusLabel.setText("Error shortening URL");
            statusLabel.setForeground(Color.RED);
        }
    }

    private void copyToClipboard() {
        String shortenedUrl = shortenedUrlField.getText();

        if (!shortenedUrl.isEmpty()) {
            StringSelection selection = new StringSelection(shortenedUrl);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            statusLabel.setText("Copied to clipboard!");
            statusLabel.setForeground(new Color(0, 150, 0));
        }
    }

    private void applyTheme() {
        try {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceMagmaLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException e) {
            try {
                System.out.println("Substance theme not detected, reverting to OS Default.");
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SwingUtilities.updateComponentTreeUI(this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set an icon for the JFrame
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("images/icon.png")));
        } catch (Exception e) {
            System.out.println("Icon not found");
        }
    }

    private void drawPentagram(Graphics2D g2d, int centerX, int centerY, int size, double rotationAngle) {
        // Calculate the five points of the pentagram
        double[][] points = new double[5][2];

        // Angle calculations for an inverted pentagram
        double goldenAngle = Math.PI * 2 / 5;
        double baseRotation = Math.PI / 2; // Rotated to point downwards

        // Calculate vertex points with additional rotation
        for (int i = 0; i < 5; i++) {
            double angle = i * goldenAngle + baseRotation + rotationAngle;
            points[i][0] = centerX + size * Math.cos(angle);
            points[i][1] = centerY + size * Math.sin(angle);
        }

        // Create pentagram path
        Path2D pentagram = new Path2D.Double();

        // Connect points to form the inverted star
        pentagram.moveTo(points[0][0], points[0][1]);
        pentagram.lineTo(points[2][0], points[2][1]);
        pentagram.lineTo(points[4][0], points[4][1]);
        pentagram.lineTo(points[1][0], points[1][ 1]);
        pentagram.lineTo(points[3][0], points[3][1]);
        pentagram.closePath();

        // Create circle path manually
        Path2D circle = new Path2D.Double();
        int numPoints = 100; // Smooth circle approximation
        for (int i = 0; i <= numPoints; i++) {
            double angle = 2 * Math.PI * i / numPoints;
            double x = centerX + size * Math.cos(angle);
            double y = centerY + size * Math.sin(angle);

            if (i == 0) {
                circle.moveTo(x, y);
            } else {
                circle.lineTo(x, y);
            }
        }

        // Styling
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3f));

        // Draw circle
        g2d.draw(circle);

        // Draw pentagram
        g2d.draw(pentagram);

        // Fill pentagram with semi-transparent dark red
        g2d.setColor(new Color(139, 0, 0, 100));
        g2d.fill(pentagram);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main gui = new Main();
            gui.setVisible(true);
        });
    }
}