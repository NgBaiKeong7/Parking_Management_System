import ui.EntryExitPanel;
import ui.ReportsPanel;
import model.*;
import utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ParkingManagementApp extends JFrame {
    private JTabbedPane mainTabbedPane;
    private EntryExitPanel entryExitPanel;
    private ReportsPanel reportsPanel;
    
    public ParkingManagementApp() {
        initUI();
        setupSampleData(); // For testing
    }
    
    private void initUI() {
        setTitle("Parking Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        
        mainTabbedPane = new JTabbedPane();
        
        // Create panels
        entryExitPanel = new EntryExitPanel();
        reportsPanel = new ReportsPanel();
        
        // Link panels
        entryExitPanel.setReportsPanel(reportsPanel);
        
        // Add to tabbed pane
        mainTabbedPane.addTab("Entry/Exit", entryExitPanel);
        mainTabbedPane.addTab("Reports", reportsPanel);
        
        // Add menu bar
        setJMenuBar(createMenuBar());
        
        add(mainTabbedPane);
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // View Menu
        JMenu viewMenu = new JMenu("View");
        JMenuItem entryExitItem = new JMenuItem("Entry/Exit");
        entryExitItem.addActionListener(e -> mainTabbedPane.setSelectedIndex(0));
        JMenuItem reportsItem = new JMenuItem("Reports");
        reportsItem.addActionListener(e -> mainTabbedPane.setSelectedIndex(1));
        viewMenu.add(entryExitItem);
        viewMenu.add(reportsItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this, 
            "Parking Management System v1.0\nDeveloped for vehicle re-entry support", 
            "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    private void setupSampleData() {
        ParkingLot parkingLot = ParkingLot.getInstance();
        
        // Create floors
        Floor floor1 = new Floor(1);
        Floor floor2 = new Floor(2);
        
        // Add spots to floor 1
        floor1.addParkingSpot(new ParkingSpot(1, 1, 1, Constants.SpotType.COMPACT));
        floor1.addParkingSpot(new ParkingSpot(1, 1, 2, Constants.SpotType.COMPACT));
        floor1.addParkingSpot(new ParkingSpot(1, 1, 3, Constants.SpotType.REGULAR));
        floor1.addParkingSpot(new ParkingSpot(1, 1, 4, Constants.SpotType.REGULAR));
        floor1.addParkingSpot(new ParkingSpot(1, 1, 5, Constants.SpotType.HANDICAPPED));
        
        // Add spots to floor 2
        floor2.addParkingSpot(new ParkingSpot(2, 1, 1, Constants.SpotType.COMPACT));
        floor2.addParkingSpot(new ParkingSpot(2, 1, 2, Constants.SpotType.REGULAR));
        floor2.addParkingSpot(new ParkingSpot(2, 1, 3, Constants.SpotType.REGULAR));
        floor2.addParkingSpot(new ParkingSpot(2, 1, 4, Constants.SpotType.REGULAR));
        floor2.addParkingSpot(new ParkingSpot(2, 1, 5, Constants.SpotType.HANDICAPPED));
        
        parkingLot.addFloor(floor1);
        parkingLot.addFloor(floor2);
        
        System.out.println("Sample parking lot data initialized!");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                ParkingManagementApp app = new ParkingManagementApp();
                app.setVisible(true);
            }
        });
    }
}