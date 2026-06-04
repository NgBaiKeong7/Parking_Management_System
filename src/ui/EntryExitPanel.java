package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import model.*;
import dao.DatabaseParkingDAO;
import utils.Constants;

public class EntryExitPanel extends JPanel {
    private JTabbedPane entryExitTabs;
    
    // Entry Panel Components
    private JTextField licensePlateField;
    private JComboBox<Constants.VehicleType> vehicleTypeCombo;
    private JCheckBox handicappedCheckBox;
    private JButton checkAvailabilityBtn;
    private JList<String> availableSpotsList;
    private DefaultListModel<String> spotsListModel;
    private JButton parkBtn;
    private JTextArea ticketArea;
    
    // Exit Panel Components
    private JTextField exitLicenseField;
    private JButton findVehicleBtn;
    private JTextArea billArea;
    private JComboBox<String> paymentMethodCombo;
    private JButton processPaymentBtn;
    
    private DatabaseParkingDAO dao;
    private Vehicle currentVehicle;
    private ParkingSpot selectedSpot;
    private ReportsPanel reportsPanel; // Reference to reports panel
    
    // Store calculated values for payment
    private double currentParkingFee;
    private double currentFineAmount;
    private double currentTotalAmount;
    
    public EntryExitPanel() {
        dao = new DatabaseParkingDAO();
        initComponents();
        setupLayout();
    }
    
    // Setter for reports panel
    public void setReportsPanel(ReportsPanel reportsPanel) {
        this.reportsPanel = reportsPanel;
    }
    
    // Show exit tab
    public void showExitTab() {
        if (entryExitTabs != null) {
            entryExitTabs.setSelectedIndex(1);
        }
    }
    
    private void initComponents() {
        // Entry Panel Components
        licensePlateField = new JTextField(15);
        vehicleTypeCombo = new JComboBox<>(Constants.VehicleType.values());
        handicappedCheckBox = new JCheckBox("Handicapped Card Holder");
        
        checkAvailabilityBtn = new JButton("Check Available Spots");
        checkAvailabilityBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAvailableSpots();
            }
        });
        
        spotsListModel = new DefaultListModel<>();
        availableSpotsList = new JList<>(spotsListModel);
        availableSpotsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        parkBtn = new JButton("Park Vehicle");
        parkBtn.setEnabled(false);
        parkBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parkVehicle();
            }
        });
        
        ticketArea = new JTextArea(8, 40);
        ticketArea.setEditable(false);
        ticketArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // Exit Panel Components
        exitLicenseField = new JTextField(15);
        
        findVehicleBtn = new JButton("Find Vehicle");
        findVehicleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findVehicleForExit();
            }
        });
        
        billArea = new JTextArea(10, 40);
        billArea.setEditable(false);
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        paymentMethodCombo = new JComboBox<>(new String[]{"Cash", "Credit Card", "Debit Card", "Online Payment"});
        processPaymentBtn = new JButton("Process Payment");
        processPaymentBtn.setEnabled(false);
        processPaymentBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processPayment();
            }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        entryExitTabs = new JTabbedPane();
        
        // Create the panels
        JPanel entryPanel = createEntryPanel();
        JPanel exitPanel = createExitPanel();
        
        entryExitTabs.addTab("Vehicle Entry", entryPanel);
        entryExitTabs.addTab("Vehicle Exit", exitPanel);
        
        add(entryExitTabs, BorderLayout.CENTER);
    }
    
    private JPanel createEntryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("License Plate:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(licensePlateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Vehicle Type:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(vehicleTypeCombo, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        inputPanel.add(handicappedCheckBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(checkAvailabilityBtn, gbc);
        
        // Spots Panel
        JPanel spotsPanel = new JPanel(new BorderLayout());
        spotsPanel.setBorder(BorderFactory.createTitledBorder("Available Parking Spots"));
        spotsPanel.add(new JScrollPane(availableSpotsList), BorderLayout.CENTER);
        
        JPanel spotsButtonPanel = new JPanel();
        spotsButtonPanel.add(parkBtn);
        spotsPanel.add(spotsButtonPanel, BorderLayout.SOUTH);
        
        // Ticket Panel
        JPanel ticketPanel = new JPanel(new BorderLayout());
        ticketPanel.setBorder(BorderFactory.createTitledBorder("Parking Ticket"));
        ticketPanel.add(new JScrollPane(ticketArea), BorderLayout.CENTER);
        
        // Center Panel
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.add(spotsPanel);
        centerPanel.add(ticketPanel);
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createExitPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Find Vehicle"));
        searchPanel.add(new JLabel("License Plate:"));
        searchPanel.add(exitLicenseField);
        searchPanel.add(findVehicleBtn);
        
        // Bill Panel
        JPanel billPanel = new JPanel(new BorderLayout());
        billPanel.setBorder(BorderFactory.createTitledBorder("Bill Details"));
        billPanel.add(new JScrollPane(billArea), BorderLayout.CENTER);
        
        // Payment Panel
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        paymentPanel.setBorder(BorderFactory.createTitledBorder("Payment"));
        paymentPanel.add(new JLabel("Payment Method:"));
        paymentPanel.add(paymentMethodCombo);
        paymentPanel.add(processPaymentBtn);
        
        // South Panel
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(paymentPanel, BorderLayout.CENTER);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(billPanel, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void checkAvailableSpots() {
        String licensePlate = licensePlateField.getText().trim().toUpperCase();
        if (licensePlate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter license plate number", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Constants.VehicleType vehicleType = (Constants.VehicleType) vehicleTypeCombo.getSelectedItem();
        boolean isHandicapped = handicappedCheckBox.isSelected();
        
        // Get or create vehicle - this preserves vehicle history
        currentVehicle = dao.getOrCreateVehicle(licensePlate, vehicleType, isHandicapped);
        
        // Check if vehicle is already parked
        if (ParkingLot.getInstance().getActiveTickets().containsKey(licensePlate)) {
            JOptionPane.showMessageDialog(this, 
                "Vehicle is already parked! Please exit first before re-entering.", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            parkBtn.setEnabled(false);
            spotsListModel.clear();
            return;
        }
        
        java.util.List<ParkingSpot> availableSpots = dao.getAvailableSpots(vehicleType);
        
        spotsListModel.clear();
        for (ParkingSpot spot : availableSpots) {
            spotsListModel.addElement(spot.toString());
        }
        
        if (availableSpots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No available spots for this vehicle type", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            parkBtn.setEnabled(false);
        } else {
            parkBtn.setEnabled(true);
        }
    }
    
    private void parkVehicle() {
        int selectedIndex = availableSpotsList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a parking spot", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        java.util.List<ParkingSpot> availableSpots = dao.getAvailableSpots(currentVehicle.getType());
        selectedSpot = availableSpots.get(selectedIndex);
        
        currentVehicle.setParkingSpot(selectedSpot);
        dao.parkVehicle(currentVehicle, selectedSpot.getSpotId());
        
        // Generate ticket
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String ticket = "================ PARKING TICKET ================\n";
        ticket += String.format("Ticket No:     %s\n", currentVehicle.getTicketNumber());
        ticket += String.format("License Plate: %s\n", currentVehicle.getLicensePlate());
        ticket += String.format("Vehicle Type:  %s\n", currentVehicle.getType().getName());
        ticket += String.format("Parking Spot:  %s\n", selectedSpot.getSpotId());
        ticket += String.format("Spot Type:     %s\n", selectedSpot.getType().getName());
        ticket += String.format("Hourly Rate:   RM%.2f\n", selectedSpot.getHourlyRate());
        ticket += String.format("Entry Time:    %s\n", currentVehicle.getEntryTime().format(formatter));
        ticket += "===============================================\n";
        ticket += "Please keep this ticket for exit.\n";
        
        ticketArea.setText(ticket);
        
        // Reset form
        licensePlateField.setText("");
        spotsListModel.clear();
        parkBtn.setEnabled(false);
        
        // Refresh reports if available
        if (reportsPanel != null) {
            reportsPanel.refreshData();
            reportsPanel.refreshSummary(); 
        }
        
        // REMOVED: dao.createEntryFine(currentVehicle.getLicensePlate()); - No entry fines!
        
        JOptionPane.showMessageDialog(this, "Vehicle parked successfully!\nVehicle is now shown in occupancy report.", 
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void findVehicleForExit() {
        String licensePlate = exitLicenseField.getText().trim().toUpperCase();
        if (licensePlate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter license plate number", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        currentVehicle = dao.findVehicleByLicensePlate(licensePlate);
        if (currentVehicle == null) {
            JOptionPane.showMessageDialog(this, "Vehicle not found in system", 
                "Error", JOptionPane.ERROR_MESSAGE);
            billArea.setText("");
            processPaymentBtn.setEnabled(false);
            return;
        }
        
        // Check if vehicle is actually parked
        boolean isParked = ParkingLot.getInstance().getActiveTickets().containsKey(licensePlate);
        
        if (!isParked) {
            JOptionPane.showMessageDialog(this, "Vehicle is not currently parked", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            billArea.setText("");
            processPaymentBtn.setEnabled(false);
            return;
        }
        
        // Calculate parking duration
        currentVehicle.setExitTime(java.time.LocalDateTime.now());
        long hours = currentVehicle.calculateParkingHours();
        if (hours == 0) hours = 1; // Minimum 1 hour charge
        
        // Calculate parking fee
        double hourlyRate = currentVehicle.getParkingSpot().getHourlyRate();
        double parkingFee = hours * hourlyRate;
        
        // Apply handicapped discount if applicable
        if (currentVehicle.isHandicappedCardHolder() && 
            currentVehicle.getParkingSpot().getType() == Constants.SpotType.HANDICAPPED) {
            parkingFee = 0; // Free for handicapped card holders in handicapped spots
        } else if (currentVehicle.getType() == Constants.VehicleType.HANDICAPPED_VEHICLE) {
            parkingFee = Math.min(parkingFee, hours * 2); // Max RM 2/hour for handicapped vehicles
        }
        
        // Check for overstay fines (only if hours > 24)
        double fineAmount = 0;
        String fineReason = "";
        if (hours > 24) {
            int overstayHours = (int) (hours - 24);
            // Use the admin-selected scheme instead of hardcoding FIXED
            fineAmount = Fine.calculateFine(dao.getCurrentFineScheme(), overstayHours);
            fineReason = "Overstayed by " + overstayHours + " hours";
            
            // Create overstay fine in database
            dao.checkAndCreateOverstayFine(licensePlate, hours);
        }
        
        // Check for any unpaid fines from previous visits
        double unpaidFinesTotal = dao.getTotalUnpaidFines(licensePlate);
        
        double totalAmount = parkingFee + fineAmount + unpaidFinesTotal;
        
        // Store values for payment processing
        currentParkingFee = parkingFee;
        currentFineAmount = fineAmount + unpaidFinesTotal;
        currentTotalAmount = totalAmount;
        
        // Generate bill
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String bill = "================ PARKING BILL ================\n";
        bill += String.format("License Plate: %s\n", currentVehicle.getLicensePlate());
        bill += String.format("Vehicle Type:  %s\n", currentVehicle.getType().getName());
        bill += String.format("Spot Type:     %s\n", currentVehicle.getParkingSpot().getType().getName());
        bill += String.format("Entry Time:    %s\n", currentVehicle.getEntryTime().format(formatter));
        bill += String.format("Exit Time:     %s\n", currentVehicle.getExitTime().format(formatter));
        bill += String.format("Duration:      %d hours\n", hours);
        bill += "------------------------------------------------\n";
        bill += String.format("Parking Fee:   RM%.2f (%d hours @ RM%.2f/hour)\n", 
                parkingFee, hours, hourlyRate);
        
        if (fineAmount > 0) {
            bill += String.format("Overstay Fine: RM%.2f (%s)\n", fineAmount, fineReason);
        }
        
        if (unpaidFinesTotal > 0) {
            bill += String.format("Previous Unpaid Fines: RM%.2f\n", unpaidFinesTotal);
        }
        
        bill += "------------------------------------------------\n";
        bill += String.format("TOTAL DUE:     RM%.2f\n", totalAmount);
        bill += "================================================\n";
        
        billArea.setText(bill);
        processPaymentBtn.setEnabled(true);
    }
    
    private void processPayment() {
        if (currentVehicle == null) {
            JOptionPane.showMessageDialog(this, "No vehicle selected", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
        
        // Mark all fines as paid BEFORE processing exit
        dao.markAllFinesAsPaid(currentVehicle.getLicensePlate(), paymentMethod);
        
        // Process exit
        dao.processExit(currentVehicle, currentParkingFee, currentFineAmount, currentTotalAmount, paymentMethod);
        
        // Generate receipt
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String receipt = "============== PAYMENT RECEIPT ==============\n";
        receipt += String.format("License Plate: %s\n", currentVehicle.getLicensePlate());
        receipt += String.format("Payment Time:  %s\n", java.time.LocalDateTime.now().format(formatter));
        receipt += "------------------------------------------------\n";
        receipt += String.format("Parking Fee:   RM%.2f\n", currentParkingFee);
        receipt += String.format("Fine:          RM%.2f\n", currentFineAmount);
        receipt += "------------------------------------------------\n";
        receipt += String.format("Total Paid:    RM%.2f\n", currentTotalAmount);
        receipt += String.format("Payment Method: %s\n", paymentMethod);
        receipt += "================================================\n";
        receipt += "Thank you for using our parking service!\n";
        
        JOptionPane.showMessageDialog(this, receipt, "Payment Successful", 
            JOptionPane.INFORMATION_MESSAGE);
        
        // Refresh reports if available
        if (reportsPanel != null) {
            reportsPanel.refreshData();
            reportsPanel.refreshSummary(); 
        }
        
        // Reset form
        exitLicenseField.setText("");
        billArea.setText("");
        processPaymentBtn.setEnabled(false);
        currentVehicle = null;
    }
}