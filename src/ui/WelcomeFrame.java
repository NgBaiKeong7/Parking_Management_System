package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import dao.DatabaseManager;  // Add this import

public class WelcomeFrame extends JFrame {
    private JButton adminBtn;
    private JButton entryBtn;
    private JButton exitBtn;
    private JButton reportsBtn;  // Added reports button
    
    public WelcomeFrame() {
        setTitle("Parking Lot Management System - Welcome");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);  // Increased height to accommodate 4 buttons
        setLocationRelativeTo(null);
        
        // Initialize database connection
        DatabaseManager.getInstance();  // This will initialize the database
        
        initComponents();
        setupLayout();
    }
    
    private void initComponents() {
        adminBtn = new JButton("Admin Panel");
        adminBtn.setFont(new Font("Arial", Font.BOLD, 16));
        adminBtn.setPreferredSize(new Dimension(250, 70));
        adminBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openMainFrame("admin");
            }
        });
        
        entryBtn = new JButton("Vehicle Entry");
        entryBtn.setFont(new Font("Arial", Font.BOLD, 16));
        entryBtn.setPreferredSize(new Dimension(250, 70));
        entryBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openMainFrame("entry");
            }
        });
        
        exitBtn = new JButton("Vehicle Exit/Payment");
        exitBtn.setFont(new Font("Arial", Font.BOLD, 16));
        exitBtn.setPreferredSize(new Dimension(250, 70));
        exitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openMainFrame("exit");
            }
        });
        
        reportsBtn = new JButton("Reports Viewer");
        reportsBtn.setFont(new Font("Arial", Font.BOLD, 16));
        reportsBtn.setPreferredSize(new Dimension(250, 70));
        reportsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openMainFrame("reports");
            }
        });
    }
    
    private void setupLayout() {
        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(0, 102, 204);
                Color color2 = new Color(0, 153, 255);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("UNIVERSITY PARKING LOT MANAGEMENT SYSTEM");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Center panel with buttons
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        JLabel welcomeLabel = new JLabel("Welcome to the Parking Management System");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        centerPanel.add(welcomeLabel, gbc);
        
        gbc.gridy = 1;
        JLabel selectLabel = new JLabel("Please select your role:");
        selectLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        selectLabel.setForeground(Color.WHITE);
        centerPanel.add(selectLabel, gbc);
        
        gbc.gridy = 2;
        centerPanel.add(createStyledButton(adminBtn), gbc);
        
        gbc.gridy = 3;
        centerPanel.add(createStyledButton(entryBtn), gbc);
        
        gbc.gridy = 4;
        centerPanel.add(createStyledButton(exitBtn), gbc);
        
        gbc.gridy = 5;
        centerPanel.add(createStyledButton(reportsBtn), gbc);
        
        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        
        JLabel footerLabel = new JLabel("© 2026 University Parking Management System");
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        
        // Add window listener to close database connection
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                DatabaseManager.closeConnection();
                System.out.println("Database connection closed.");
            }
        });
    }
    
    private JButton createStyledButton(JButton button) {
        button.setBackground(new Color(255, 255, 255));
        button.setForeground(new Color(0, 102, 204));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 3),
            BorderFactory.createEmptyBorder(15, 30, 15, 30)
        ));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(255, 255, 150));
                button.setForeground(new Color(0, 51, 102));
                button.setFont(new Font("Arial", Font.BOLD, 18));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(new Color(0, 102, 204));
                button.setFont(new Font("Arial", Font.BOLD, 16));
            }
        });
        
        return button;
    }
    
    private void openMainFrame(String role) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(role);
            mainFrame.setVisible(true);
            this.dispose();
        });
    }
    
    // Keep old methods for backward compatibility
    private void openAdminPanel() {
        openMainFrame("admin");
    }
    
    private void openEntryPanel() {
        openMainFrame("entry");
    }
    
    private void openExitPanel() {
        openMainFrame("exit");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            WelcomeFrame welcomeFrame = new WelcomeFrame();
            welcomeFrame.setVisible(true);
        });
    } 
}