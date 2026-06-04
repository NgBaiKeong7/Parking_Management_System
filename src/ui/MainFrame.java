package ui;

import javax.swing.*;
import java.awt.*;
import dao.DatabaseManager;  // Add this import

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private AdminPanel adminPanel;
    private EntryExitPanel entryExitPanel;
    private ReportsPanel reportsPanel;
    
    public MainFrame(String selectedTab) {
        setTitle("University Parking Lot Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Initialize database connection
        DatabaseManager.getInstance();  // This will initialize the database
        
        initComponents();
        setupLayout();
        
        // Link panels for communication
        entryExitPanel.setReportsPanel(reportsPanel);
        
        // Select the appropriate tab based on parameter
        if (selectedTab != null) {
            switch (selectedTab.toLowerCase()) {
                case "admin":
                    tabbedPane.setSelectedIndex(0);
                    break;
                case "entry":
                    tabbedPane.setSelectedIndex(1);
                    break;
                case "exit":
                    tabbedPane.setSelectedIndex(1); // Entry/Exit panel, but show exit tab
                    if (entryExitPanel != null) {
                        entryExitPanel.showExitTab();
                    }
                    break;
                case "reports":
                    tabbedPane.setSelectedIndex(2);
                    break;
            }
        }
        
        // Add back button
        addBackButton();
    }
    
    // Keep original constructor for backward compatibility
    public MainFrame() {
        this("admin"); // Default to admin tab
    }
    
    private void initComponents() {
        tabbedPane = new JTabbedPane();
        adminPanel = new AdminPanel();
        entryExitPanel = new EntryExitPanel();
        reportsPanel = new ReportsPanel();
        
        tabbedPane.addTab("Admin", adminPanel);
        tabbedPane.addTab("Entry/Exit", entryExitPanel);
        tabbedPane.addTab("Reports", reportsPanel);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));
        headerPanel.setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("UNIVERSITY PARKING LOT MANAGEMENT SYSTEM");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        
        JLabel statusLabel = new JLabel("Ready - Database connected");
        statusPanel.add(statusLabel);
        
        add(statusPanel, BorderLayout.SOUTH);
        
        // Add window listener to close database connection
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                DatabaseManager.closeConnection();
                System.out.println("Database connection closed.");
            }
        });
    }
    
    private void addBackButton() {
        JPanel headerPanel = (JPanel) ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.NORTH);
        
        JButton backButton = new JButton("← Back to Menu");
        backButton.setFont(new Font("Arial", Font.PLAIN, 12));
        backButton.setBackground(Color.WHITE);
        backButton.setForeground(new Color(0, 102, 204));
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        backButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to return to the main menu?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                SwingUtilities.invokeLater(() -> {
                    WelcomeFrame welcomeFrame = new WelcomeFrame();
                    welcomeFrame.setVisible(true);
                    this.dispose();
                });
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(backButton);
        
        headerPanel.add(buttonPanel, BorderLayout.WEST);
    }
    
    // Add main method for backward compatibility
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}