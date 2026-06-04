package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import utils.Constants.SpotType;

public class ParkingLot {

    private static ParkingLot instance;

    private List<Floor> floors;
    private Map<String, Ticket> activeTickets; // Currently parked vehicles
    private List<Ticket> allTickets;          // All vehicle visits
    private List<Payment> payments;
    private List<Fine> fines;

    private ParkingLot() {
        floors = new ArrayList<>();
        activeTickets = new HashMap<>();
        allTickets = new ArrayList<>();
        payments = new ArrayList<>();
        fines = new ArrayList<>();
    }

    public static ParkingLot getInstance() {
        if (instance == null) instance = new ParkingLot();
        return instance;
    }

    // =========================
    // FLOOR MANAGEMENT
    // =========================
    public void addFloor(Floor floor) {
        floors.add(floor);
    }

    public List<Floor> getFloors() {
        return floors;
    }

    // =========================
    // FIND AVAILABLE SPOT
    // =========================
    public ParkingSpot findAvailableSpot(SpotType type) {
        for (Floor floor : floors) {
            for (ParkingSpot spot : floor.getParkingSpots()) {
                if (spot.getType() == type && spot.isAvailable()) return spot;
            }
        }
        return null;
    }

    // =========================
    // VEHICLE ENTRY
    // =========================
    public Ticket parkVehicle(Vehicle vehicle, SpotType preferredType) {
        if (vehicle == null || vehicle.getLicensePlate() == null) {
            System.out.println("Invalid vehicle!");
            return null;
        }

        String plate = vehicle.getLicensePlate().toUpperCase().trim();

        // Vehicle already parked - return existing ticket
        if (activeTickets.containsKey(plate)) {
            System.out.println("Vehicle already parked!");
            return activeTickets.get(plate);
        }

        ParkingSpot spot = findAvailableSpot(preferredType);
        if (spot == null) {
            System.out.println("No available spot!");
            return null;
        }

        // Assign spot
        spot.setAvailable(false);
        spot.setCurrentVehicle(vehicle);

        // Generate new ticket number for this parking session
        String ticketNumber = generateTicketNumber(plate);
        vehicle.setTicketNumber(ticketNumber);
        vehicle.setEntryTime(LocalDateTime.now()); // Reset entry time for new session
        vehicle.setExitTime(null); // Clear exit time for new session
        vehicle.setParkingSpot(spot); // Set the parking spot

        // Create ticket
        Ticket ticket = new Ticket(
            ticketNumber,
            plate,
            spot.getSpotId(),
            spot.getHourlyRate()
        );
        ticket.setVehicle(vehicle);

        // Save ticket
        activeTickets.put(plate, ticket);
        allTickets.add(ticket);

        System.out.println("Vehicle parked successfully! Plate: " + plate + ", Spot: " + spot.getSpotId());
        return ticket;
    }

    private String generateTicketNumber(String plate) {
        int count = 1;
        for (Ticket t : allTickets) {
            if (t.getLicensePlate().equals(plate)) count++;
        }
        return plate + "-" + count;
    }

    // =========================
    // VEHICLE EXIT
    // =========================
    public Payment exitVehicle(String licensePlate, String paymentMethod) {
        if (licensePlate == null) return null;

        String plate = licensePlate.toUpperCase().trim();
        Ticket ticket = activeTickets.get(plate);

        if (ticket == null) {
            System.out.println("Vehicle not found or already exited!");
            return null;
        }

        ParkingSpot spot = findSpotById(ticket.getSpotId());

        long hours = Duration.between(ticket.getEntryTime(), LocalDateTime.now()).toHours();
        if (hours == 0) hours = 1;

        double parkingFee = hours * ticket.getHourlyRate();
        double fineAmount = calculateFine(plate, hours);
        double total = parkingFee + fineAmount;

        Payment payment = new Payment(plate, total, paymentMethod, fineAmount, parkingFee);
        payments.add(payment);

        // Free spot
        if (spot != null) {
            spot.setAvailable(true);
            spot.setCurrentVehicle(null);
        }

        // Mark ticket as exited
        ticket.setExitTime(LocalDateTime.now());

        activeTickets.remove(plate);
        System.out.println("Exit successful! Plate: " + plate + ", Fee: " + total);
        return payment;
    }
    

    // =========================
    // HELPER METHODS
    // =========================
    public ParkingSpot findSpotById(String spotId) {
        for (Floor floor : floors) {
            for (ParkingSpot spot : floor.getParkingSpots()) {
                if (spot.getSpotId().equals(spotId)) return spot;
            }
        }
        return null;
    }

    private double calculateFine(String licensePlate, long hours) {
        double totalFine = 0;

        // Overstay fine
        if (hours > 24) {
            Fine fine = new Fine(licensePlate, "Overstay", 50, (int) hours);
            fines.add(fine);
            totalFine += fine.getAmount();
        }

        // Unpaid previous fines
        for (Fine fine : fines) {
            if (fine.getLicensePlate().equals(licensePlate) && !fine.isPaid()) {
                totalFine += fine.getAmount();
                fine.setPaid(true);
            }
        }

        return totalFine;
    }

    // =========================
    // REPORT GETTERS
    // =========================
    public Map<String, Ticket> getActiveTickets() {
        return activeTickets;
    }

    public List<Ticket> getAllTickets() {
        return allTickets;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public List<Fine> getFines() {
        return fines;
    }

    public List<Fine> getUnpaidFines() {
        List<Fine> list = new ArrayList<>();
        for (Fine fine : fines) if (!fine.isPaid()) list.add(fine);
        return list;
    }

    public int getTotalOccupied() {
        int count = 0;
        for (Floor floor : floors) count += floor.getOccupiedCount();
        return count;
    }

    public double getTotalRevenue() {
        double total = 0;
        for (Payment p : payments) total += p.getAmount();
        return total;
    }

    /**
     * Returns occupancy data in format:
     * [floorNumber, spotType, total, occupied, available]
     */
    public List<Object[]> getOccupancyData() {
        List<Object[]> occupancyData = new ArrayList<>();
        
        for (Floor floor : floors) {
            Map<SpotType, Integer> totalByType = new HashMap<>();
            Map<SpotType, Integer> occupiedByType = new HashMap<>();
            
            // Calculate totals and occupied counts
            for (ParkingSpot spot : floor.getParkingSpots()) {
                SpotType type = spot.getType();
                totalByType.put(type, totalByType.getOrDefault(type, 0) + 1);
                
                if (!spot.isAvailable()) {
                    occupiedByType.put(type, occupiedByType.getOrDefault(type, 0) + 1);
                }
            }
            
            // Add data for each spot type
            for (SpotType type : totalByType.keySet()) {
                int total = totalByType.get(type);
                int occupied = occupiedByType.getOrDefault(type, 0);
                int available = total - occupied;
                
                // Double-check occupied count matches active tickets
                int activeTicketsCount = 0;
                for (Ticket ticket : activeTickets.values()) {
                    ParkingSpot ticketSpot = findSpotById(ticket.getSpotId());
                    if (ticketSpot != null && ticketSpot.getFloorNumber() == floor.getFloorNumber() 
                        && ticketSpot.getType() == type) {
                        activeTicketsCount++;
                    }
                }
                
                // Verify consistency - use the active tickets count as it's more reliable
                if (occupied != activeTicketsCount) {
                    System.out.println("Warning: Occupancy mismatch for Floor " + floor.getFloorNumber() 
                        + ", Type " + type + ": Spots occupied=" + occupied 
                        + ", Active tickets=" + activeTicketsCount);
                    occupied = activeTicketsCount;
                    available = total - occupied;
                }
                
                occupancyData.add(new Object[]{
                    floor.getFloorNumber(), 
                    type.getName(), 
                    total, 
                    occupied, 
                    available
                });
            }
        }
        
        return occupancyData;
    }

    // =========================
    // REPORT PRINTING
    // =========================
    public void printTicketReport() {
        System.out.println("\n=== Parking Ticket Report ===");
        for (Ticket t : allTickets) {
            System.out.println("Ticket: " + t.getTicketNumber() +
                    ", Plate: " + t.getLicensePlate() +
                    ", Spot: " + t.getSpotId() +
                    ", Entry: " + t.getEntryTime() +
                    ", Exit: " + (t.getExitTime() != null ? t.getExitTime() : "Still parked") +
                    ", Rate: " + t.getHourlyRate() +
                    ", Fee: " + calculateParkingFee(t));
        }
    }

    public double calculateParkingFee(Ticket ticket) {
        LocalDateTime exitTime = ticket.getExitTime() != null ? ticket.getExitTime() : LocalDateTime.now();
        long hours = Duration.between(ticket.getEntryTime(), exitTime).toHours();
        if (hours == 0) hours = 1;
        return hours * ticket.getHourlyRate();
    }
    // Add this method to ParkingLot.java to clear and reload data
public void clearAllData() {
    activeTickets.clear();
    allTickets.clear();
    payments.clear();
    fines.clear();
    floors.clear();
}

// Add method to check if system has data
public boolean hasActiveTickets() {
    return !activeTickets.isEmpty();
}
}