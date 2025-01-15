import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
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

    public Main() {
        initUI();
        applyTheme();
        setTitle("Link Shortener");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 220);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create main panel with increased top padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(35, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();

        // Original URL input
        JLabel urlLabel = new JLabel("Enter URL:");
        originalUrlField = new JTextField(25);
        
        // Shorten button
        JButton shortenButton = new JButton("Shorten");
        shortenButton.addActionListener(e -> shortenUrl());

        // Shortened URL field with copy button
        shortenedUrlField = new JTextField(25);
        shortenedUrlField.setEditable(false);
        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> copyToClipboard());

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.GRAY);

        // Layout components
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(urlLabel, gbc);

        gbc.gridy = 1;
        mainPanel.add(originalUrlField, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 10, 0);
        mainPanel.add(shortenButton, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 5, 0);
        mainPanel.add(shortenedUrlField, gbc);

        gbc.gridy = 4;
        mainPanel.add(copyButton, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(10, 0, 0, 0);
        mainPanel.add(statusLabel, gbc);

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
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main gui = new Main();
            gui.setVisible(true);
        });
    }
}