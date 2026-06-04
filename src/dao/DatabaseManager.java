package dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {
    private static DatabaseManager instance;
    private static Connection connection;
    private static final String DB_URL = "jdbc:sqlite:parking_system.db";
    
    private DatabaseManager() {
        initializeDatabase();
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void initializeDatabase() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Create connection
            connection = DriverManager.getConnection(DB_URL);
            
            // Enable foreign keys
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            
            // Create tables
            createTables();
            
            System.out.println("Database initialized successfully!");
            
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }
    
    private void createTables() throws SQLException {
        // Create floors table
        String createFloorsTable = "CREATE TABLE IF NOT EXISTS floors (" +
                "floor_number INTEGER PRIMARY KEY" +
                ")";
        
        // Create parking_spots table
        String createSpotsTable = "CREATE TABLE IF NOT EXISTS parking_spots (" +
                "spot_id TEXT PRIMARY KEY," +
                "floor_number INTEGER," +
                "row_number INTEGER," +
                "spot_number INTEGER," +
                "spot_type TEXT," +
                "hourly_rate REAL," +
                "is_available INTEGER DEFAULT 1," +
                "FOREIGN KEY (floor_number) REFERENCES floors(floor_number)" +
                ")";
        
        // Create vehicles table (for vehicle history)
        String createVehiclesTable = "CREATE TABLE IF NOT EXISTS vehicles (" +
                "license_plate TEXT PRIMARY KEY," +
                "vehicle_type TEXT," +
                "is_handicapped INTEGER DEFAULT 0," +
                "total_visits INTEGER DEFAULT 0," +
                "last_visit_time TEXT" +
                ")";
        
        // Create tickets table
        String createTicketsTable = "CREATE TABLE IF NOT EXISTS tickets (" +
                "ticket_number TEXT PRIMARY KEY," +
                "license_plate TEXT," +
                "spot_id TEXT," +
                "entry_time TEXT," +
                "exit_time TEXT," +
                "hourly_rate REAL," +
                "FOREIGN KEY (license_plate) REFERENCES vehicles(license_plate)," +
                "FOREIGN KEY (spot_id) REFERENCES parking_spots(spot_id)" +
                ")";
        
        // Create payments table
        String createPaymentsTable = "CREATE TABLE IF NOT EXISTS payments (" +
                "payment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "license_plate TEXT," +
                "payment_time TEXT," +
                "amount REAL," +
                "payment_method TEXT," +
                "parking_fee REAL," +
                "fine_amount REAL," +
                "FOREIGN KEY (license_plate) REFERENCES vehicles(license_plate)" +
                ")";
        
        // Create fines table
        String createFinesTable = "CREATE TABLE IF NOT EXISTS fines (" +
                "fine_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "license_plate TEXT," +
                "fine_type TEXT," +
                "amount REAL," +
                "issue_time TEXT," +
                "is_paid INTEGER DEFAULT 0," +
                "overstay_hours INTEGER," +
                "FOREIGN KEY (license_plate) REFERENCES vehicles(license_plate)" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createFloorsTable);
            stmt.execute(createSpotsTable);
            stmt.execute(createVehiclesTable);
            stmt.execute(createTicketsTable);
            stmt.execute(createPaymentsTable);
            stmt.execute(createFinesTable);
        }
    }
    
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Static utility methods
    public static String dateTimeToString(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    public static LocalDateTime stringToDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    
}