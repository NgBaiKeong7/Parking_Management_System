package dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.*;
import utils.Constants;
import utils.Constants.FineScheme;

public class DatabaseParkingDAO {
    private ParkingLot parkingLot;
    private Map<String, Vehicle> vehicleMap;
    private FineScheme currentFineScheme;
    
    public DatabaseParkingDAO() {
        this.parkingLot = ParkingLot.getInstance();
        this.vehicleMap = new HashMap<>();
        this.currentFineScheme = FineScheme.PROGRESSIVE; // Default scheme
        
        // Clear existing data in parking lot before loading
        parkingLot.clearAllData();
        
        // Load data from database
        loadDataFromDatabase();
    }
    
    // ==================== LOAD DATA FROM DATABASE ====================
    
    private void loadDataFromDatabase() {
        loadFloorsAndSpots();
        loadVehicles();
        loadTickets();
        loadPayments();
        loadFines();
        synchronizeActiveTickets();
        
        // Print status for debugging
        printDatabaseStatus();
    }
    
    private void loadFloorsAndSpots() {
        String sql = "SELECT ps.*, f.floor_number " +
                     "FROM parking_spots ps " +
                     "JOIN floors f ON ps.floor_number = f.floor_number " +
                     "ORDER BY ps.floor_number, ps.row_number, ps.spot_number";
        
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            Map<Integer, Floor> floorMap = new HashMap<>();
            
            while (rs.next()) {
                int floorNum = rs.getInt("floor_number");
                Floor floor = floorMap.get(floorNum);
                if (floor == null) {
                    floor = new Floor(floorNum);
                    floorMap.put(floorNum, floor);
                }
                
                ParkingSpot spot = new ParkingSpot(
                    floorNum,
                    rs.getInt("row_number"),
                    rs.getInt("spot_number"),
                    Constants.SpotType.valueOf(rs.getString("spot_type"))
                );
                
                // Set availability from database (will be updated by active tickets later)
                spot.setAvailable(rs.getInt("is_available") == 1);
                
                floor.addParkingSpot(spot);
            }
            
            // Add floors to parking lot
            for (Floor floor : floorMap.values()) {
                parkingLot.addFloor(floor);
            }
            
            System.out.println("Loaded " + floorMap.size() + " floors with parking spots");
            
        } catch (SQLException e) {
            System.err.println("Error loading floors and spots: " + e.getMessage());
        }
    }
    
    private void loadVehicles() {
        String sql = "SELECT * FROM vehicles";
        
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String licensePlate = rs.getString("license_plate");
                String vehicleType = rs.getString("vehicle_type");
                boolean isHandicapped = rs.getInt("is_handicapped") == 1;
                
                Vehicle vehicle = new Vehicle(
                    licensePlate,
                    Constants.VehicleType.valueOf(vehicleType),
                    isHandicapped
                );
                
                vehicleMap.put(licensePlate, vehicle);
            }
            
            System.out.println("Loaded " + vehicleMap.size() + " vehicles");
            
        } catch (SQLException e) {
            System.err.println("Error loading vehicles: " + e.getMessage());
        }
    }
    
    private void loadTickets() {
        String sql = "SELECT * FROM tickets ORDER BY entry_time";
        
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int activeCount = 0;
            
            while (rs.next()) {
                String ticketNumber = rs.getString("ticket_number");
                String licensePlate = rs.getString("license_plate");
                String spotId = rs.getString("spot_id");
                LocalDateTime entryTime = DatabaseManager.stringToDateTime(rs.getString("entry_time"));
                LocalDateTime exitTime = DatabaseManager.stringToDateTime(rs.getString("exit_time"));
                double hourlyRate = rs.getDouble("hourly_rate");
                
                Ticket ticket = new Ticket(ticketNumber, licensePlate, spotId, hourlyRate);
                
                // Set entry and exit times using reflection
                try {
                    java.lang.reflect.Field entryField = Ticket.class.getDeclaredField("entryTime");
                    entryField.setAccessible(true);
                    entryField.set(ticket, entryTime);
                    
                    java.lang.reflect.Field exitField = Ticket.class.getDeclaredField("exitTime");
                    exitField.setAccessible(true);
                    exitField.set(ticket, exitTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                // Find and set the vehicle for this ticket
                Vehicle vehicle = vehicleMap.get(licensePlate);
                if (vehicle != null) {
                    ticket.setVehicle(vehicle);
                    
                    // If this is an active ticket, set up the vehicle and spot
                    if (exitTime == null) {
                        ParkingSpot spot = parkingLot.findSpotById(spotId);
                        if (spot != null) {
                            vehicle.setParkingSpot(spot);
                            vehicle.setEntryTime(entryTime);
                            vehicle.setTicketNumber(ticketNumber);
                            spot.setCurrentVehicle(vehicle);
                            spot.setAvailable(false);
                            
                            // Add to active tickets
                            parkingLot.getActiveTickets().put(licensePlate, ticket);
                            activeCount++;
                        }
                    }
                }
                
                parkingLot.getAllTickets().add(ticket);
            }
            
            System.out.println("Loaded " + parkingLot.getAllTickets().size() + " tickets");
            System.out.println("Active tickets: " + activeCount);
            
        } catch (SQLException e) {
            System.err.println("Error loading tickets: " + e.getMessage());
        }
    }
    
    private void loadPayments() {
        String sql = "SELECT * FROM payments ORDER BY payment_time";
        
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String licensePlate = rs.getString("license_plate");
                LocalDateTime paymentTime = DatabaseManager.stringToDateTime(rs.getString("payment_time"));
                double amount = rs.getDouble("amount");
                String paymentMethod = rs.getString("payment_method");
                double parkingFee = rs.getDouble("parking_fee");
                double fineAmount = rs.getDouble("fine_amount");
                
                Payment payment = new Payment(licensePlate, amount, paymentMethod, fineAmount, parkingFee);
                
                // Set payment time and ID using reflection
                try {
                    java.lang.reflect.Field timeField = Payment.class.getDeclaredField("paymentTime");
                    timeField.setAccessible(true);
                    timeField.set(payment, paymentTime);
                    
                    java.lang.reflect.Field idField = Payment.class.getDeclaredField("paymentId");
                    idField.setAccessible(true);
                    idField.set(payment, rs.getInt("payment_id"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                parkingLot.getPayments().add(payment);
            }
            
            System.out.println("Loaded " + parkingLot.getPayments().size() + " payments");
            
        } catch (SQLException e) {
            System.err.println("Error loading payments: " + e.getMessage());
        }
    }
    
    private void loadFines() {
        String sql = "SELECT * FROM fines ORDER BY issue_time DESC";
        
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String licensePlate = rs.getString("license_plate");
                String fineType = rs.getString("fine_type");
                double amount = rs.getDouble("amount");
                LocalDateTime issueTime = DatabaseManager.stringToDateTime(rs.getString("issue_time"));
                boolean isPaid = rs.getInt("is_paid") == 1;
                int overstayHours = rs.getInt("overstay_hours");
                String paymentMethod = rs.getString("payment_method");
                LocalDateTime paymentTime = DatabaseManager.stringToDateTime(rs.getString("payment_time"));
                
                Fine fine = new Fine(licensePlate, fineType, amount, overstayHours);
                
                // Set additional fields using reflection
                try {
                    java.lang.reflect.Field idField = Fine.class.getDeclaredField("fineId");
                    idField.setAccessible(true);
                    idField.set(fine, rs.getInt("fine_id"));
                    
                    java.lang.reflect.Field timeField = Fine.class.getDeclaredField("issueTime");
                    timeField.setAccessible(true);
                    timeField.set(fine, issueTime);
                    
                    java.lang.reflect.Field paidField = Fine.class.getDeclaredField("paid");
                    paidField.setAccessible(true);
                    paidField.set(fine, isPaid);
                    
                    if (paymentMethod != null) {
                        java.lang.reflect.Field pmField = Fine.class.getDeclaredField("paymentMethod");
                        pmField.setAccessible(true);
                        pmField.set(fine, paymentMethod);
                    }
                    
                    if (paymentTime != null) {
                        java.lang.reflect.Field ptField = Fine.class.getDeclaredField("paymentTime");
                        ptField.setAccessible(true);
                        ptField.set(fine, paymentTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                parkingLot.getFines().add(fine);
            }
            
            System.out.println("Loaded " + parkingLot.getFines().size() + " fines");
            
        } catch (SQLException e) {
            System.err.println("Error loading fines: " + e.getMessage());
        }
    }
    
    private void synchronizeActiveTickets() {
        // Update spot availability based on active tickets
        for (Ticket ticket : parkingLot.getActiveTickets().values()) {
            ParkingSpot spot = parkingLot.findSpotById(ticket.getSpotId());
            if (spot != null) {
                spot.setAvailable(false);
                System.out.println("Spot " + spot.getSpotId() + " is occupied by " + ticket.getLicensePlate());
            }
        }
    }
    
    private void printDatabaseStatus() {
        System.out.println("=== DATABASE LOAD STATUS ===");
        System.out.println("Active tickets: " + parkingLot.getActiveTickets().size());
        System.out.println("All tickets: " + parkingLot.getAllTickets().size());
        System.out.println("Vehicles in map: " + vehicleMap.size());
        System.out.println("Payments: " + parkingLot.getPayments().size());
        System.out.println("Fines: " + parkingLot.getFines().size());
        System.out.println("==============================");
    }
    
    public boolean hasData() {
        return !parkingLot.getAllTickets().isEmpty() || !vehicleMap.isEmpty();
    }
    
    // ==================== SAVE DATA TO DATABASE ====================
    
    public void saveFloor(Floor floor) {
        String sql = "INSERT OR IGNORE INTO floors (floor_number) VALUES (?)";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, floor.getFloorNumber());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving floor: " + e.getMessage());
        }
    }
    
    public void saveParkingSpot(ParkingSpot spot) {
        String sql = "INSERT OR REPLACE INTO parking_spots " +
                     "(spot_id, floor_number, row_number, spot_number, spot_type, hourly_rate, is_available) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, spot.getSpotId());
            pstmt.setInt(2, spot.getFloorNumber());
            pstmt.setInt(3, spot.getRowNumber());
            pstmt.setInt(4, spot.getSpotNumber());
            pstmt.setString(5, spot.getType().name());
            pstmt.setDouble(6, spot.getHourlyRate());
            pstmt.setInt(7, spot.isAvailable() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving parking spot: " + e.getMessage());
        }
    }
    
    public void saveVehicle(Vehicle vehicle) {
        String sql = "INSERT OR REPLACE INTO vehicles " +
                     "(license_plate, vehicle_type, is_handicapped, total_visits, last_visit_time) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, vehicle.getLicensePlate());
            pstmt.setString(2, vehicle.getType().name());
            pstmt.setInt(3, vehicle.isHandicappedCardHolder() ? 1 : 0);
            
            // Count visits
            int visits = 0;
            for (Ticket t : parkingLot.getAllTickets()) {
                if (t.getLicensePlate().equals(vehicle.getLicensePlate())) {
                    visits++;
                }
            }
            pstmt.setInt(4, visits);
            
            pstmt.setString(5, DatabaseManager.dateTimeToString(LocalDateTime.now()));
            pstmt.executeUpdate();
            
            // Update vehicle map
            vehicleMap.put(vehicle.getLicensePlate(), vehicle);
            
        } catch (SQLException e) {
            System.err.println("Error saving vehicle: " + e.getMessage());
        }
    }
    
    public void saveTicket(Ticket ticket) {
        String sql = "INSERT OR REPLACE INTO tickets " +
                     "(ticket_number, license_plate, spot_id, entry_time, exit_time, hourly_rate) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, ticket.getTicketNumber());
            pstmt.setString(2, ticket.getLicensePlate());
            pstmt.setString(3, ticket.getSpotId());
            pstmt.setString(4, DatabaseManager.dateTimeToString(ticket.getEntryTime()));
            pstmt.setString(5, DatabaseManager.dateTimeToString(ticket.getExitTime()));
            pstmt.setDouble(6, ticket.getHourlyRate());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving ticket: " + e.getMessage());
        }
    }
    
    public void savePayment(Payment payment) {
        String sql = "INSERT INTO payments " +
                     "(license_plate, payment_time, amount, payment_method, parking_fee, fine_amount) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, payment.getLicensePlate());
            pstmt.setString(2, DatabaseManager.dateTimeToString(payment.getPaymentTime()));
            pstmt.setDouble(3, payment.getAmount());
            pstmt.setString(4, payment.getPaymentMethod());
            pstmt.setDouble(5, payment.getParkingFee());
            pstmt.setDouble(6, payment.getFineAmount());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving payment: " + e.getMessage());
        }
    }
    
    public void saveFine(Fine fine) {
        String sql = "INSERT INTO fines " +
                     "(license_plate, fine_type, amount, issue_time, is_paid, overstay_hours, payment_method, payment_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, fine.getLicensePlate());
            pstmt.setString(2, fine.getFineType());
            pstmt.setDouble(3, fine.getAmount());
            pstmt.setString(4, DatabaseManager.dateTimeToString(fine.getIssueTime()));
            pstmt.setInt(5, fine.isPaid() ? 1 : 0);
            pstmt.setInt(6, fine.getOverstayHours());
            pstmt.setString(7, fine.getPaymentMethod());
            pstmt.setString(8, DatabaseManager.dateTimeToString(fine.getPaymentTime()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving fine: " + e.getMessage());
        }
    }
    
    // ==================== FINE MANAGEMENT METHODS ====================
    
    /**
     * Update spot rate in database
     */
    public void updateSpotRate(String spotType, double newRate) {
        String sql = "UPDATE parking_spots SET hourly_rate = ? WHERE spot_type = ?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setDouble(1, newRate);
            pstmt.setString(2, spotType);
            int updated = pstmt.executeUpdate();
            
            System.out.println("Updated " + updated + " spots of type " + spotType + " to rate RM" + newRate);
            
        } catch (SQLException e) {
            System.err.println("Error updating spot rates: " + e.getMessage());
        }
    }
    
    /**
     * Update fine status with payment method
     */
    public void updateFineStatus(int fineId, boolean isPaid, String paymentMethod) {
        String sql = "UPDATE fines SET is_paid = ?, payment_method = ?, payment_time = ? WHERE fine_id = ?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, isPaid ? 1 : 0);
            pstmt.setString(2, paymentMethod);
            pstmt.setString(3, DatabaseManager.dateTimeToString(LocalDateTime.now()));
            pstmt.setInt(4, fineId);
            pstmt.executeUpdate();
            
            System.out.println("Updated fine " + fineId + " status to paid with method: " + paymentMethod);
            
        } catch (SQLException e) {
            System.err.println("Error updating fine status: " + e.getMessage());
        }
    }
    
    /**
     * Overloaded method for backward compatibility
     */
    public void updateFineStatus(int fineId, boolean isPaid) {
        updateFineStatus(fineId, isPaid, "Cash");
    }
    
    /**
     * Check and create overstay fines on exit (only if parked > 24 hours)
     */
    public void checkAndCreateOverstayFine(String licensePlate, long parkedHours) {
        Fine fine = Fine.createOverstayFine(licensePlate, currentFineScheme, parkedHours);
        if (fine != null) {
            saveFine(fine);
            parkingLot.getFines().add(fine);
            System.out.println("Overstay fine created for " + licensePlate + 
                              ": RM" + fine.getAmount());
        }
    }
    
    /**
     * Get all unpaid fines for a vehicle
     */
    public List<Fine> getUnpaidFinesForVehicle(String licensePlate) {
        List<Fine> unpaidFines = new ArrayList<>();
        for (Fine fine : parkingLot.getFines()) {
            if (fine.getLicensePlate().equals(licensePlate) && !fine.isPaid()) {
                unpaidFines.add(fine);
            }
        }
        return unpaidFines;
    }
    
    /**
     * Calculate total unpaid fines for a vehicle
     */
    public double getTotalUnpaidFines(String licensePlate) {
        double total = 0;
        for (Fine fine : parkingLot.getFines()) {
            if (fine.getLicensePlate().equals(licensePlate) && !fine.isPaid()) {
                total += fine.getAmount();
            }
        }
        return total;
    }
    
    /**
     * Mark all fines as paid for a vehicle
     */
    public void markAllFinesAsPaid(String licensePlate, String paymentMethod) {
        for (Fine fine : parkingLot.getFines()) {
            if (fine.getLicensePlate().equals(licensePlate) && !fine.isPaid()) {
                fine.markAsPaid(paymentMethod);
                updateFineStatus(fine.getFineId(), true, paymentMethod);
            }
        }
        System.out.println("All fines marked as paid for vehicle: " + licensePlate);
    }
    
    /**
     * Set current fine scheme
     */
    public void setCurrentFineScheme(FineScheme scheme) {
        this.currentFineScheme = scheme;
        System.out.println("Fine scheme changed to: " + scheme);
    }
    
    /**
     * Get current fine scheme
     */
    public FineScheme getCurrentFineScheme() {
        return currentFineScheme;
    }
    
    public void updateParkingSpotAvailability(String spotId, boolean isAvailable) {
        String sql = "UPDATE parking_spots SET is_available = ? WHERE spot_id = ?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, isAvailable ? 1 : 0);
            pstmt.setString(2, spotId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating spot availability: " + e.getMessage());
        }
    }
    
    // ==================== BUSINESS METHODS ====================
    
    public Vehicle findVehicleByLicensePlate(String licensePlate) {
        String plate = licensePlate.toUpperCase().trim();
        
        // First check active tickets
        for (Ticket ticket : parkingLot.getActiveTickets().values()) {
            if (ticket.getLicensePlate().equals(plate)) {
                Vehicle v = ticket.getVehicle();
                if (v != null) {
                    return v;
                }
            }
        }
        
        // Then check vehicle map
        if (vehicleMap.containsKey(plate)) {
            return vehicleMap.get(plate);
        }
        
        // If not found, try to load from database
        String sql = "SELECT * FROM vehicles WHERE license_plate = ?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, plate);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String vehicleType = rs.getString("vehicle_type");
                boolean isHandicapped = rs.getInt("is_handicapped") == 1;
                
                Vehicle vehicle = new Vehicle(plate, 
                    Constants.VehicleType.valueOf(vehicleType), 
                    isHandicapped);
                
                vehicleMap.put(plate, vehicle);
                return vehicle;
            }
        } catch (SQLException e) {
            System.err.println("Error finding vehicle: " + e.getMessage());
        }
        
        return null;
    }
    
    public Vehicle getOrCreateVehicle(String licensePlate, Constants.VehicleType vehicleType, boolean isHandicapped) {
        String plate = licensePlate.toUpperCase().trim();
        
        Vehicle vehicle = findVehicleByLicensePlate(plate);
        
        if (vehicle == null) {
            vehicle = new Vehicle(plate, vehicleType, isHandicapped);
            vehicleMap.put(plate, vehicle);
        } else {
            vehicle.setType(vehicleType);
            vehicle.setHandicappedCardHolder(isHandicapped);
        }
        
        vehicle.setEntryTime(LocalDateTime.now());
        vehicle.setExitTime(null);
        vehicle.setTicketNumber(generateTicketNumber(plate));
        
        saveVehicle(vehicle);
        
        return vehicle;
    }
    
    private String generateTicketNumber(String plate) {
        return "T-" + plate + "-" + System.currentTimeMillis();
    }
    
    public List<ParkingSpot> getAvailableSpots(Constants.VehicleType vehicleType) {
        List<ParkingSpot> availableSpots = new ArrayList<>();
        
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getParkingSpots()) {
                if (spot.isAvailable() && canPark(vehicleType, spot.getType())) {
                    availableSpots.add(spot);
                }
            }
        }
        return availableSpots;
    }
    
    private boolean canPark(Constants.VehicleType vehicleType, Constants.SpotType spotType) {
        switch (vehicleType) {
            case MOTORCYCLE:
                return spotType == Constants.SpotType.COMPACT;
            case CAR:
                return spotType == Constants.SpotType.COMPACT || 
                       spotType == Constants.SpotType.REGULAR;
            case SUV_TRUCK:
                return spotType == Constants.SpotType.REGULAR;
            case HANDICAPPED_VEHICLE:
                return true;
            //case BUS:
                //return spotType == Constants.SpotType.REGULAR;
            default:
                return false;
        }
    }
    
    public void parkVehicle(Vehicle vehicle, String spotId) {
        ParkingSpot spot = parkingLot.findSpotById(spotId);
        if (spot != null) {
            String plate = vehicle.getLicensePlate();
            
            // Update spot
            spot.setAvailable(false);
            spot.setCurrentVehicle(vehicle);
            vehicle.setParkingSpot(spot);
            vehicle.setEntryTime(LocalDateTime.now());
            
            // Create new ticket
            String ticketNumber = "T-" + plate + "-" + System.currentTimeMillis();
            vehicle.setTicketNumber(ticketNumber);
            
            Ticket ticket = new Ticket(
                ticketNumber,
                plate,
                spotId,
                spot.getHourlyRate()
            );
            ticket.setVehicle(vehicle);
            
            // Save to memory
            parkingLot.getActiveTickets().put(plate, ticket);
            parkingLot.getAllTickets().add(ticket);
            
            // Save to database
            saveFloor(parkingLot.getFloors().stream()
                .filter(f -> f.getFloorNumber() == spot.getFloorNumber())
                .findFirst().orElse(null));
            saveParkingSpot(spot);
            saveVehicle(vehicle);
            saveTicket(ticket);
            
            System.out.println("Vehicle parked: " + plate + " at spot: " + spotId);
        }
    }
    
    public void processExit(Vehicle vehicle, double parkingFee, double fineAmount, 
                           double totalAmount, String paymentMethod) {
        if (vehicle == null) return;
        
        String plate = vehicle.getLicensePlate();
        ParkingSpot spot = vehicle.getParkingSpot();
        
        if (spot != null) {
            spot.setAvailable(true);
            spot.setCurrentVehicle(null);
            updateParkingSpotAvailability(spot.getSpotId(), true);
        }
        
        // Remove from active tickets
        parkingLot.getActiveTickets().remove(plate);
        
        // Create payment record
        Payment payment = new Payment(plate, totalAmount, paymentMethod, fineAmount, parkingFee);
        parkingLot.getPayments().add(payment);
        savePayment(payment);
        
        // Mark all fines as paid
        markAllFinesAsPaid(plate, paymentMethod);
        
        // Update ticket with exit time
        for (Ticket ticket : parkingLot.getAllTickets()) {
            if (ticket.getLicensePlate().equals(plate) && ticket.getExitTime() == null) {
                ticket.setExitTime(LocalDateTime.now());
                saveTicket(ticket);
                break;
            }
        }
        
        vehicle.setExitTime(LocalDateTime.now());
        saveVehicle(vehicle);
        
        System.out.println("Vehicle exited: " + plate);
    }
    
    public void addParkingSpot(int floorNumber, int rowNumber, int spotNumber, String spotType) {
        Floor targetFloor = null;
        for (Floor floor : parkingLot.getFloors()) {
            if (floor.getFloorNumber() == floorNumber) {
                targetFloor = floor;
                break;
            }
        }
        
        if (targetFloor == null) {
            targetFloor = new Floor(floorNumber);
            parkingLot.addFloor(targetFloor);
            saveFloor(targetFloor);
        }
        
        Constants.SpotType type = Constants.SpotType.valueOf(spotType.toUpperCase());
        
        ParkingSpot spot = new ParkingSpot(floorNumber, rowNumber, spotNumber, type);
        targetFloor.addParkingSpot(spot);
        
        saveParkingSpot(spot);
        
        System.out.println("Parking spot added: " + spot.getSpotId());
    }
    
    // ==================== REPORT METHODS ====================
    
    public List<Object[]> getCurrentVehicles() {
        List<Object[]> vehicles = new ArrayList<>();
        for (Ticket ticket : parkingLot.getActiveTickets().values()) {
            Vehicle v = ticket.getVehicle();
            if (v != null) {
                long hours = v.calculateParkingHours();
                vehicles.add(new Object[]{
                    v.getLicensePlate(),
                    v.getType().getName(),
                    ticket.getSpotId(),
                    v.getEntryTime(),
                    hours
                });
            }
        }
        return vehicles;
    }
    
    public List<Object[]> getRevenueData() {
        List<Object[]> revenue = new ArrayList<>();
        for (Payment p : parkingLot.getPayments()) {
            revenue.add(new Object[]{
                p.getPaymentTime(),
                p.getLicensePlate(),
                p.getParkingFee(),
                p.getFineAmount(),
                p.getAmount(),
                p.getPaymentMethod()
            });
        }
        return revenue;
    }
    
    public List<Object[]> getOccupancyData() {
        return parkingLot.getOccupancyData();
    }
    
    public List<Object[]> getFinesData() {
        List<Object[]> fines = new ArrayList<>();
        for (Fine f : parkingLot.getFines()) {
            fines.add(new Object[]{
                f.getIssueTime(),
                f.getLicensePlate(),
                f.getFineType(),
                f.getAmount(),
                f.isPaid(),
                f.getOverstayHours()
            });
        }
        return fines;
    }
    
    public ParkingLot getParkingLot() {
        return parkingLot;
    }
}