package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.DatabaseParkingDAO;
import model.ParkingLot;

public class ReportsPanel extends JPanel {

    private JTabbedPane tabbedPane;
    private JButton refreshBtn;
    
    // Tables
    private JTable vehiclesTable;
    private JTable revenueTable;
    private JTable occupancyTable;
    private JTable finesTable;

    private DefaultTableModel vehiclesModel;
    private DefaultTableModel revenueModel;
    private DefaultTableModel occupancyModel;
    private DefaultTableModel finesModel;

    // Summary Labels
    private JLabel activeVehiclesLabel;
    private JLabel occupiedSpotsLabel;
    private JLabel totalFinesLabel;
    private JLabel totalRevenueLabel;
    private JPanel summaryPanel;

    private DatabaseParkingDAO dao;

    public ReportsPanel() {
        dao = new DatabaseParkingDAO();
        initComponents();
        loadAllData();
        updateSummary(); // Initial summary update
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Create top panel with refresh button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshBtn = new JButton("Refresh Reports");
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
        topPanel.add(refreshBtn);

        tabbedPane = new JTabbedPane();

        // Vehicles Table
        vehiclesModel = new DefaultTableModel(new Object[]{"License Plate", "Vehicle Type", "Spot ID", "Entry Time", "Duration (hrs)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        vehiclesTable = new JTable(vehiclesModel);
        vehiclesTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        tabbedPane.addTab("Active Vehicles", new JScrollPane(vehiclesTable));

        // Revenue Table
        revenueModel = new DefaultTableModel(new Object[]{"Payment Time", "License Plate", "Parking Fee", "Fine Amount", "Total Amount", "Payment Method"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        revenueTable = new JTable(revenueModel);
        revenueTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        tabbedPane.addTab("Revenue", new JScrollPane(revenueTable));

        // Occupancy Table
        occupancyModel = new DefaultTableModel(new Object[]{"Floor", "Spot Type", "Total Spots", "Occupied", "Available"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        occupancyTable = new JTable(occupancyModel);
        tabbedPane.addTab("Occupancy", new JScrollPane(occupancyTable));

        // Fines Table
        finesModel = new DefaultTableModel(new Object[]{"Issue Time", "License Plate", "Fine Type", "Amount (RM)", "Paid", "Overstay Hours"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        finesTable = new JTable(finesModel);
        finesTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        tabbedPane.addTab("Fines", new JScrollPane(finesTable));

        // Create Summary Panel with labels that can be updated
        summaryPanel = createSummaryPanel();
        
        // Add all to main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Summary"));
        
        // Create labels that will be updated
        activeVehiclesLabel = createSummaryLabel("Active Vehicles: 0");
        occupiedSpotsLabel = createSummaryLabel("Occupied Spots: 0");
        totalFinesLabel = createSummaryLabel("Total Fines: 0");
        totalRevenueLabel = createSummaryLabel("Total Revenue: RM0.00");
        
        panel.add(activeVehiclesLabel);
        panel.add(occupiedSpotsLabel);
        panel.add(totalFinesLabel);
        panel.add(totalRevenueLabel);
        
        return panel;
    }
    
    private JLabel createSummaryLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    public void refreshData() {
        loadAllData();
        updateSummary(); // Update summary when refreshing
        JOptionPane.showMessageDialog(this, "Reports refreshed successfully!", 
            "Refresh", JOptionPane.INFORMATION_MESSAGE);
    }

    public void loadAllData() {
        loadVehiclesData();
        loadRevenueData();
        loadOccupancyData();
        loadFinesData();
    }

    private void loadVehiclesData() {
        vehiclesModel.setRowCount(0);
        List<Object[]> vehicles = dao.getCurrentVehicles();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Object[] v : vehicles) {
            vehiclesModel.addRow(new Object[]{
                    v[0],
                    v[1].toString(),
                    v[2],
                    v[3] != null ? formatter.format((java.time.LocalDateTime) v[3]) : "",
                    v[4] + " hrs"
            });
        }
    }

    private void loadRevenueData() {
        revenueModel.setRowCount(0);
        List<Object[]> payments = dao.getRevenueData();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Object[] p : payments) {
            revenueModel.addRow(new Object[]{
                    p[0] != null ? formatter.format((java.time.LocalDateTime) p[0]) : "",
                    p[1],
                    "RM" + String.format("%.2f", p[2]),
                    "RM" + String.format("%.2f", p[3]),
                    "RM" + String.format("%.2f", p[4]),
                    p[5]
            });
        }
    }

    private void loadOccupancyData() {
        occupancyModel.setRowCount(0);
        List<Object[]> occupancy = dao.getOccupancyData();
        for (Object[] o : occupancy) {
            occupancyModel.addRow(new Object[]{
                    "Floor " + o[0],
                    o[1],
                    o[2],
                    o[3],
                    o[4]
            });
        }
    }

    private void loadFinesData() {
        finesModel.setRowCount(0);
        List<Object[]> fines = dao.getFinesData();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Object[] f : fines) {
            finesModel.addRow(new Object[]{
                    f[0] != null ? formatter.format((java.time.LocalDateTime) f[0]) : "",
                    f[1],
                    f[2],
                    "RM" + String.format("%.2f", f[3]),
                    f[4].toString(),
                    f[5] + " hrs"
            });
        }
    }
    
    // New method to update summary data
    public void updateSummary() {
        ParkingLot parkingLot = ParkingLot.getInstance();
        
        int activeVehicles = parkingLot.getActiveTickets().size();
        int totalOccupied = parkingLot.getTotalOccupied();
        int totalFines = parkingLot.getFines().size();
        double totalRevenue = parkingLot.getTotalRevenue();
        
        activeVehiclesLabel.setText("Active Vehicles: " + activeVehicles);
        occupiedSpotsLabel.setText("Occupied Spots: " + totalOccupied);
        totalFinesLabel.setText("Total Fines: " + totalFines);
        totalRevenueLabel.setText("Total Revenue: RM" + String.format("%.2f", totalRevenue));
        
        // Repaint to ensure updates are visible
        summaryPanel.revalidate();
        summaryPanel.repaint();
    }
    
    // Getter for summary update (to be called from EntryExitPanel)
    public void refreshSummary() {
        updateSummary();
    }
}