package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.DatabaseParkingDAO;
import model.*;
import utils.Constants;
import utils.Constants.FineScheme;

public class AdminPanel extends JPanel {
    
    private JTabbedPane adminTabs;
    
    // Configuration Panel Components
    private JTextField floorField;
    private JTextField rowField;
    private JTextField spotField;
    private JComboBox<String> spotTypeCombo;
    private JButton addSpotBtn;
    private JTable spotsTable;
    private DefaultTableModel spotsTableModel;
    private JButton refreshSpotsBtn;
    
    // Rates Panel Components
    private JTable ratesTable;
    private DefaultTableModel ratesTableModel;
    private JButton updateRatesBtn;
    
    // Fines Panel Components
    private JTable finesTable;
    private DefaultTableModel finesTableModel;
    private JButton markPaidBtn;
    private JButton markAllForVehicleBtn;
    private JButton refreshFinesBtn;
    private JButton viewUnpaidOnlyBtn;
    private JButton viewAllFinesBtn;
    private JComboBox<FineScheme> fineSchemeCombo;
    private JButton applySchemeBtn;
    private JLabel totalUnpaidFinesLabel;
    private JLabel totalPaidFinesLabel;
    private JLabel totalFinesCountLabel;
    private JTextField searchPlateField;
    private JButton searchBtn;
    private JButton clearSearchBtn;
    
    private DatabaseParkingDAO dao;
    
    public AdminPanel() {
        dao = new DatabaseParkingDAO();
        initComponents();
        loadData();
        updateFinesSummary();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        adminTabs = new JTabbedPane();
        
        // Configuration Tab
        JPanel configPanel = createConfigurationPanel();
        adminTabs.addTab("Parking Configuration", configPanel);
        
        // Rates Tab
        JPanel ratesPanel = createRatesPanel();
        adminTabs.addTab("Parking Rates", ratesPanel);
        
        // Fines Management Tab
        JPanel finesPanel = createFinesPanel();
        adminTabs.addTab("Fines Management", finesPanel);
        
        add(adminTabs, BorderLayout.CENTER);
    }
    
    private JPanel createConfigurationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Parking Spot"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Floor Number
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Floor Number:"), gbc);
        gbc.gridx = 1;
        floorField = new JTextField(10);
        inputPanel.add(floorField, gbc);
        
        // Row Number
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Row Number:"), gbc);
        gbc.gridx = 1;
        rowField = new JTextField(10);
        inputPanel.add(rowField, gbc);
        
        // Spot Number
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Spot Number:"), gbc);
        gbc.gridx = 1;
        spotField = new JTextField(10);
        inputPanel.add(spotField, gbc);
        
        // Spot Type
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Spot Type:"), gbc);
        gbc.gridx = 1;
        spotTypeCombo = new JComboBox<>(new String[]{
            "COMPACT", "REGULAR", "HANDICAPPED", "RESERVED"
        });
        inputPanel.add(spotTypeCombo, gbc);
        
        // Add Button
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        addSpotBtn = new JButton("Add Parking Spot");
        addSpotBtn.setBackground(new Color(0, 102, 204));
        addSpotBtn.setForeground(Color.BLACK);
        addSpotBtn.setFocusPainted(false);
        addSpotBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addParkingSpot();
            }
        });
        inputPanel.add(addSpotBtn, gbc);
        
        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Current Parking Spots"));
        
        spotsTableModel = new DefaultTableModel(
            new Object[]{"Floor", "Row", "Spot", "Spot ID", "Type", "Hourly Rate", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        spotsTable = new JTable(spotsTableModel);
        spotsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        spotsTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        spotsTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        spotsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        spotsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        spotsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        spotsTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(spotsTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Refresh Button
        refreshSpotsBtn = new JButton("Refresh Spots");
        refreshSpotsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadSpotsData();
                JOptionPane.showMessageDialog(AdminPanel.this, 
                    "Parking spots refreshed!", 
                    "Refresh", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JPanel btnPanel = new JPanel();
        btnPanel.add(refreshSpotsBtn);
        tablePanel.add(btnPanel, BorderLayout.SOUTH);
        
        // Split Panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, tablePanel);
        splitPane.setDividerLocation(300);
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRatesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Rates Table
        ratesTableModel = new DefaultTableModel(
            new Object[]{"Spot Type", "Current Rate (RM/hour)", "New Rate (RM/hour)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Only New Rate column is editable
            }
        };
        ratesTable = new JTable(ratesTableModel);
        ratesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        ratesTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        ratesTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        
        // Load current rates
        loadRatesData();
        
        JScrollPane scrollPane = new JScrollPane(ratesTable);
        
        // Update Button
        JPanel buttonPanel = new JPanel();
        updateRatesBtn = new JButton("Update Rates");
        updateRatesBtn.setBackground(new Color(0, 102, 204));
        updateRatesBtn.setForeground(Color.BLACK);
        updateRatesBtn.setFocusPainted(false);
        updateRatesBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateRates();
            }
        });
        buttonPanel.add(updateRatesBtn);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createFinesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top Panel for Fine Scheme Selection and Search
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Fine Management Controls"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 0: Fine Scheme Selection
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Fine Scheme:"), gbc);
        
        gbc.gridx = 1;
        fineSchemeCombo = new JComboBox<>(FineScheme.values());
        fineSchemeCombo.setSelectedItem(dao.getCurrentFineScheme());
        topPanel.add(fineSchemeCombo, gbc);
        
        gbc.gridx = 2;
        applySchemeBtn = new JButton("Apply Scheme");
        applySchemeBtn.setBackground(new Color(0, 102, 204));
        applySchemeBtn.setForeground(Color.BLACK);
        applySchemeBtn.setFocusPainted(false);
        applySchemeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFineScheme();
            }
        });
        topPanel.add(applySchemeBtn, gbc);
        
        gbc.gridx = 3;
        JLabel schemeInfoLabel = new JLabel("(Affects future entries only)");
        schemeInfoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        schemeInfoLabel.setForeground(Color.GRAY);
        topPanel.add(schemeInfoLabel, gbc);
        
        // Row 1: Search by License Plate
        gbc.gridx = 0; gbc.gridy = 1;
        topPanel.add(new JLabel("Search Plate:"), gbc);
        
        gbc.gridx = 1;
        searchPlateField = new JTextField(15);
        topPanel.add(searchPlateField, gbc);
        
        gbc.gridx = 2;
        searchBtn = new JButton("Search");
        searchBtn.setBackground(new Color(0, 153, 0));
        searchBtn.setForeground(Color.BLACK);
        searchBtn.setFocusPainted(false);
        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchFinesByPlate();
            }
        });
        topPanel.add(searchBtn, gbc);
        
        gbc.gridx = 3;
        clearSearchBtn = new JButton("Clear Search");
        clearSearchBtn.setBackground(new Color(153, 0, 0));
        clearSearchBtn.setForeground(Color.BLACK);
        clearSearchBtn.setFocusPainted(false);
        clearSearchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFinesData();
                searchPlateField.setText("");
                JOptionPane.showMessageDialog(AdminPanel.this, 
                    "Search cleared. Showing all fines.", 
                    "Clear Search", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        topPanel.add(clearSearchBtn, gbc);
        
        // Summary Panel
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Fines Summary"));
        
        totalUnpaidFinesLabel = new JLabel("Unpaid: RM0.00");
        totalUnpaidFinesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalUnpaidFinesLabel.setForeground(new Color(200, 0, 0));
        
        totalPaidFinesLabel = new JLabel("Paid: RM0.00");
        totalPaidFinesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalPaidFinesLabel.setForeground(new Color(0, 150, 0));
        
        totalFinesCountLabel = new JLabel("Total Fines: 0");
        totalFinesCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        summaryPanel.add(totalUnpaidFinesLabel);
        summaryPanel.add(totalPaidFinesLabel);
        summaryPanel.add(totalFinesCountLabel);
        
        // Fines Table
        finesTableModel = new DefaultTableModel(
            new Object[]{"Fine ID", "License Plate", "Fine Type", "Amount", "Issue Date", "Status", "Payment Method", "Payment Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        finesTable = new JTable(finesTableModel);
        finesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        finesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        finesTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        finesTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        finesTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        finesTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        finesTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        finesTable.getColumnModel().getColumn(7).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(finesTable);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        markPaidBtn = new JButton("Mark Selected as Paid");
        markPaidBtn.setBackground(new Color(0, 150, 0));
        markPaidBtn.setForeground(Color.BLACK);
        markPaidBtn.setFocusPainted(false);
        markPaidBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markFinesAsPaid();
            }
        });
        
        markAllForVehicleBtn = new JButton("Mark All for Vehicle");
        markAllForVehicleBtn.setBackground(new Color(0, 102, 204));
        markAllForVehicleBtn.setForeground(Color.BLACK);
        markAllForVehicleBtn.setFocusPainted(false);
        markAllForVehicleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markAllFinesForVehicle();
            }
        });
        
        viewUnpaidOnlyBtn = new JButton("View Unpaid Only");
        viewUnpaidOnlyBtn.setBackground(new Color(255, 140, 0));
        viewUnpaidOnlyBtn.setForeground(Color.BLACK);
        viewUnpaidOnlyBtn.setFocusPainted(false);
        viewUnpaidOnlyBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadUnpaidFinesOnly();
            }
        });
        
        viewAllFinesBtn = new JButton("View All Fines");
        viewAllFinesBtn.setBackground(new Color(128, 0, 128));
        viewAllFinesBtn.setForeground(Color.BLACK);
        viewAllFinesBtn.setFocusPainted(false);
        viewAllFinesBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFinesData();
            }
        });
        
        refreshFinesBtn = new JButton("Refresh");
        refreshFinesBtn.setBackground(new Color(0, 102, 204));
        refreshFinesBtn.setForeground(Color.BLACK);
        refreshFinesBtn.setFocusPainted(false);
        refreshFinesBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFinesData();
                updateFinesSummary();
                JOptionPane.showMessageDialog(AdminPanel.this, 
                    "Fines data refreshed from database!", 
                    "Refresh", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        buttonPanel.add(markPaidBtn);
        buttonPanel.add(markAllForVehicleBtn);
        buttonPanel.add(viewUnpaidOnlyBtn);
        buttonPanel.add(viewAllFinesBtn);
        buttonPanel.add(refreshFinesBtn);
        
        // Combine panels
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(summaryPanel, BorderLayout.CENTER);
        
        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void addParkingSpot() {
        try {
            int floor = Integer.parseInt(floorField.getText().trim());
            int row = Integer.parseInt(rowField.getText().trim());
            int spot = Integer.parseInt(spotField.getText().trim());
            String spotType = (String) spotTypeCombo.getSelectedItem();
            
            // Validate inputs
            if (floor <= 0 || row <= 0 || spot <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "Floor, row, and spot numbers must be positive!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if spot already exists
            ParkingLot parkingLot = ParkingLot.getInstance();
            boolean spotExists = false;
            
            for (Floor f : parkingLot.getFloors()) {
                if (f.getFloorNumber() == floor) {
                    for (ParkingSpot s : f.getParkingSpots()) {
                        if (s.getRowNumber() == row && s.getSpotNumber() == spot) {
                            spotExists = true;
                            break;
                        }
                    }
                }
                if (spotExists) break;
            }
            
            if (spotExists) {
                JOptionPane.showMessageDialog(this, 
                    "Spot already exists on Floor " + floor + ", Row " + row + ", Spot " + spot, 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Add the spot
            dao.addParkingSpot(floor, row, spot, spotType);
            
            // Clear fields
            floorField.setText("");
            rowField.setText("");
            spotField.setText("");
            spotTypeCombo.setSelectedIndex(0);
            
            // Refresh table
            loadSpotsData();
            
            JOptionPane.showMessageDialog(this, 
                "Parking spot added successfully!\nSpot ID: F" + floor + "-R" + row + "-S" + spot, 
                "Success", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid numbers for floor, row, and spot", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSpotsData() {
        spotsTableModel.setRowCount(0);
        ParkingLot parkingLot = ParkingLot.getInstance();
        
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getParkingSpots()) {
                spotsTableModel.addRow(new Object[]{
                    floor.getFloorNumber(),
                    spot.getRowNumber(),
                    spot.getSpotNumber(),
                    spot.getSpotId(),
                    spot.getType().getName(),
                    String.format("RM%.2f", spot.getHourlyRate()),
                    spot.isAvailable() ? "Available" : "Occupied"
                });
            }
        }
    }
    
    private void loadRatesData() {
        ratesTableModel.setRowCount(0);
        
        // Add rates for all spot types
        for (Constants.SpotType spotType : Constants.SpotType.values()) {
            ratesTableModel.addRow(new Object[]{
                spotType.name(),
                String.format("%.2f", spotType.getHourlyRate()),
                ""
            });
        }
    }
    
    private void updateRates() {
        boolean updated = false;
        StringBuilder message = new StringBuilder("Updated Rates:\n");
        
        for (int i = 0; i < ratesTableModel.getRowCount(); i++) {
            String newRateStr = (String) ratesTableModel.getValueAt(i, 2);
            if (newRateStr != null && !newRateStr.trim().isEmpty()) {
                try {
                    double newRate = Double.parseDouble(newRateStr.trim());
                    String spotTypeName = (String) ratesTableModel.getValueAt(i, 0);
                    
                    // Update in database
                    dao.updateSpotRate(spotTypeName, newRate);
                    
                    message.append(String.format("  %s: RM%.2f\n", spotTypeName, newRate));
                    updated = true;
                    
                    // Clear the new rate field
                    ratesTableModel.setValueAt("", i, 2);
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Invalid rate format for " + ratesTableModel.getValueAt(i, 0), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        
        if (updated) {
            // Reload rates to show updated values
            loadRatesData();
            JOptionPane.showMessageDialog(this, 
                message.toString(), 
                "Rates Updated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, 
                "No rates were updated. Please enter new rates in the 'New Rate' column.", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void loadFinesData() {
        finesTableModel.setRowCount(0);
        ParkingLot parkingLot = ParkingLot.getInstance();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (Fine fine : parkingLot.getFines()) {
            String issueDate = fine.getIssueTime() != null ? 
                fine.getIssueTime().format(formatter) : "N/A";
            String paymentDate = fine.getPaymentTime() != null ? 
                fine.getPaymentTime().format(formatter) : "Not Paid";
            String paymentMethod = fine.getPaymentMethod() != null ? 
                fine.getPaymentMethod() : "N/A";
            
            finesTableModel.addRow(new Object[]{
                fine.getFineId(),
                fine.getLicensePlate(),
                fine.getFineType(),
                String.format("RM%.2f", fine.getAmount()),
                issueDate,
                fine.isPaid() ? "Paid" : "Unpaid",
                fine.isPaid() ? paymentMethod : "N/A",
                fine.isPaid() ? paymentDate : "N/A"
            });
        }
        
        updateFinesSummary();
    }
    
    private void loadUnpaidFinesOnly() {
        finesTableModel.setRowCount(0);
        ParkingLot parkingLot = ParkingLot.getInstance();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int unpaidCount = 0;
        
        for (Fine fine : parkingLot.getFines()) {
            if (!fine.isPaid()) {
                String issueDate = fine.getIssueTime() != null ? 
                    fine.getIssueTime().format(formatter) : "N/A";
                
                finesTableModel.addRow(new Object[]{
                    fine.getFineId(),
                    fine.getLicensePlate(),
                    fine.getFineType(),
                    String.format("RM%.2f", fine.getAmount()),
                    issueDate,
                    "Unpaid",
                    "N/A",
                    "N/A"
                });
                unpaidCount++;
            }
        }
        
        JOptionPane.showMessageDialog(this, 
            "Showing " + unpaidCount + " unpaid fines", 
            "Unpaid Fines Only", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void searchFinesByPlate() {
        String searchPlate = searchPlateField.getText().trim().toUpperCase();
        if (searchPlate.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a license plate number to search", 
                "Search", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        finesTableModel.setRowCount(0);
        ParkingLot parkingLot = ParkingLot.getInstance();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int count = 0;
        
        for (Fine fine : parkingLot.getFines()) {
            if (fine.getLicensePlate().contains(searchPlate)) {
                String issueDate = fine.getIssueTime() != null ? 
                    fine.getIssueTime().format(formatter) : "N/A";
                String paymentDate = fine.getPaymentTime() != null ? 
                    fine.getPaymentTime().format(formatter) : "Not Paid";
                String paymentMethod = fine.getPaymentMethod() != null ? 
                    fine.getPaymentMethod() : "N/A";
                
                finesTableModel.addRow(new Object[]{
                    fine.getFineId(),
                    fine.getLicensePlate(),
                    fine.getFineType(),
                    String.format("RM%.2f", fine.getAmount()),
                    issueDate,
                    fine.isPaid() ? "Paid" : "Unpaid",
                    fine.isPaid() ? paymentMethod : "N/A",
                    fine.isPaid() ? paymentDate : "N/A"
                });
                count++;
            }
        }
        
        JOptionPane.showMessageDialog(this, 
            "Found " + count + " fines for plate containing: " + searchPlate, 
            "Search Results", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void markFinesAsPaid() {
        int[] selectedRows = finesTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select at least one fine to mark as paid", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirmed = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to mark " + selectedRows.length + " fine(s) as paid?", 
            "Confirm Payment", JOptionPane.YES_NO_OPTION);
        
        if (confirmed == JOptionPane.YES_OPTION) {
            String paymentMethod = JOptionPane.showInputDialog(this, 
                "Enter payment method (Cash/Credit Card/Debit Card):", 
                "Payment Method", JOptionPane.QUESTION_MESSAGE);
            
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                paymentMethod = "Cash";
            }
            
            int successCount = 0;
            StringBuilder paidFines = new StringBuilder();
            
            for (int row : selectedRows) {
                int fineId = (int) finesTableModel.getValueAt(row, 0);
                String licensePlate = (String) finesTableModel.getValueAt(row, 1);
                double amount = Double.parseDouble(((String) finesTableModel.getValueAt(row, 3)).replace("RM", ""));
                
                // Update in database
                dao.updateFineStatus(fineId, true, paymentMethod);
                
                // Update in memory
                ParkingLot parkingLot = ParkingLot.getInstance();
                for (Fine fine : parkingLot.getFines()) {
                    if (fine.getFineId() == fineId) {
                        fine.setPaid(true);
                        fine.setPaymentMethod(paymentMethod);
                        break;
                    }
                }
                
                paidFines.append(String.format("Fine ID %d: RM%.2f for %s\n", fineId, amount, licensePlate));
                successCount++;
            }
            
            JOptionPane.showMessageDialog(this, 
                successCount + " fine(s) marked as paid successfully!\n\n" + paidFines.toString(), 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh data
            loadFinesData();
            updateFinesSummary();
        }
    }
    
    private void markAllFinesForVehicle() {
        String licensePlate = JOptionPane.showInputDialog(this, 
            "Enter License Plate to mark all fines as paid:", 
            "Mark All Fines", JOptionPane.QUESTION_MESSAGE);
        
        if (licensePlate != null && !licensePlate.trim().isEmpty()) {
            licensePlate = licensePlate.trim().toUpperCase();
            
            String paymentMethod = JOptionPane.showInputDialog(this, 
                "Enter payment method (Cash/Credit Card/Debit Card):", 
                "Payment Method", JOptionPane.QUESTION_MESSAGE);
            
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                paymentMethod = "Cash";
            }
            
            int count = 0;
            double totalAmount = 0;
            ParkingLot parkingLot = ParkingLot.getInstance();
            
            for (Fine fine : parkingLot.getFines()) {
                if (fine.getLicensePlate().equals(licensePlate) && !fine.isPaid()) {
                    dao.updateFineStatus(fine.getFineId(), true, paymentMethod);
                    fine.setPaid(true);
                    fine.setPaymentMethod(paymentMethod);
                    count++;
                    totalAmount += fine.getAmount();
                }
            }
            
            if (count > 0) {
                JOptionPane.showMessageDialog(this, 
                    count + " fines marked as paid for vehicle " + licensePlate + 
                    "\nTotal Amount: RM" + String.format("%.2f", totalAmount), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No unpaid fines found for vehicle " + licensePlate, 
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            }
            
            loadFinesData();
            updateFinesSummary();
        }
    }
    
    private void applyFineScheme() {
        FineScheme selected = (FineScheme) fineSchemeCombo.getSelectedItem();
        dao.setCurrentFineScheme(selected);
        
        String schemeDescription = "";
        switch (selected) {
            case FIXED:
                schemeDescription = "Flat RM 50 fine for overstaying";
                break;
            case PROGRESSIVE:
                schemeDescription = "Progressive: RM50 (24h), RM150 (48h), RM300 (72h), RM500 (72h+)";
                break;
            case HOURLY:
                schemeDescription = "Hourly: RM20 per hour overstay";
                break;
        }
        
        JOptionPane.showMessageDialog(this, 
            "Fine scheme changed to: " + selected + "\n" + schemeDescription + 
            "\n\nThis will affect future entries only.\nExisting fines remain unchanged.", 
            "Scheme Updated", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateFinesSummary() {
        ParkingLot parkingLot = ParkingLot.getInstance();
        double totalUnpaid = 0;
        double totalPaid = 0;
        int unpaidCount = 0;
        int paidCount = 0;
        
        for (Fine fine : parkingLot.getFines()) {
            if (fine.isPaid()) {
                totalPaid += fine.getAmount();
                paidCount++;
            } else {
                totalUnpaid += fine.getAmount();
                unpaidCount++;
            }
        }
        
        totalUnpaidFinesLabel.setText(String.format("Unpaid: RM%.2f (%d)", totalUnpaid, unpaidCount));
        totalPaidFinesLabel.setText(String.format("Paid: RM%.2f (%d)", totalPaid, paidCount));
        totalFinesCountLabel.setText("Total Fines: " + (paidCount + unpaidCount));
    }
    
    private void loadData() {
        loadSpotsData();
        loadRatesData();
        loadFinesData();
    }
    
    public void refreshData() {
        loadData();
        updateFinesSummary();
    }
}