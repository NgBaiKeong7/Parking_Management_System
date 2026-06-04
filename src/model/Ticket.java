package model;

import java.time.LocalDateTime;

public class Ticket {
    private String ticketNumber;
    private String licensePlate;
    private String spotId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double hourlyRate;
    private Vehicle vehicle;

    public Ticket(String ticketNumber, String licensePlate, String spotId, double hourlyRate) {
        this.ticketNumber = ticketNumber;
        this.licensePlate = licensePlate;
        this.spotId = spotId;
        this.hourlyRate = hourlyRate;
        this.entryTime = LocalDateTime.now();
        this.exitTime = null;
    }

    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }
    public boolean isExited() { return exitTime != null; }

    // Vehicle link
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public Vehicle getVehicle() { return vehicle; }

    // Getters
    public String getTicketNumber() { return ticketNumber; }
    public String getLicensePlate() { return licensePlate; }
    public String getSpotId() { return spotId; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public double getHourlyRate() { return hourlyRate; }
}
