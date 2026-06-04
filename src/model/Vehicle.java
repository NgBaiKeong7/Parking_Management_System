package model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import utils.Constants.VehicleType;
import utils.Constants.SpotType;

public class Vehicle {
    private String licensePlate;
    private VehicleType type;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private ParkingSpot parkingSpot;
    private String ticketNumber;
    private boolean isHandicappedCardHolder;
    
    public Vehicle(String licensePlate, VehicleType type, boolean isHandicappedCardHolder) {
        this.licensePlate = licensePlate;
        this.type = type;
        this.entryTime = LocalDateTime.now();
        this.isHandicappedCardHolder = isHandicappedCardHolder;
        this.ticketNumber = generateTicketNumber();
    }
    
    private String generateTicketNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "T-" + licensePlate + "-" + timestamp.substring(timestamp.length() - 6);
    }
    
    public boolean canParkIn(ParkingSpot spot) {
        SpotType spotType = spot.getType();
        
        switch (type) {
            case MOTORCYCLE:
                return spotType == SpotType.COMPACT;
            case CAR:
                return spotType == SpotType.COMPACT || spotType == SpotType.REGULAR;
            case SUV_TRUCK:
                return spotType == SpotType.REGULAR;
            case HANDICAPPED_VEHICLE:
                return true; // Can park in any spot
            default:
                return false;
        }
    }
    
    public long calculateParkingHours() {
        LocalDateTime endTime = (exitTime == null) ? LocalDateTime.now() : exitTime;
        long minutes = ChronoUnit.MINUTES.between(entryTime, endTime);
        return (minutes + 59) / 60; // Ceiling rounding
    }
    
    // Getters and setters
    public String getLicensePlate() { return licensePlate; }
    
    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }
    
    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }
    
    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }
    
    public ParkingSpot getParkingSpot() { return parkingSpot; }
    public void setParkingSpot(ParkingSpot parkingSpot) { this.parkingSpot = parkingSpot; }
    
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    
    public boolean isHandicappedCardHolder() { return isHandicappedCardHolder; }
    public void setHandicappedCardHolder(boolean handicappedCardHolder) { 
        this.isHandicappedCardHolder = handicappedCardHolder; 
    }
}