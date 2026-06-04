package model;

import utils.Constants.SpotType;

public class ParkingSpot {
    private String spotId;
    private int floorNumber;
    private int rowNumber;
    private int spotNumber;
    private SpotType type;
    private double hourlyRate;
    private boolean isAvailable;
    private Vehicle currentVehicle;

    public ParkingSpot(int floorNumber, int rowNumber, int spotNumber, SpotType type) {
        this.floorNumber = floorNumber;
        this.rowNumber = rowNumber;
        this.spotNumber = spotNumber;
        this.spotId = String.format("F%d-R%d-S%d", floorNumber, rowNumber, spotNumber);
        this.type = type;
        this.hourlyRate = type.getHourlyRate();
        this.isAvailable = true;
    }

    // Getters and setters
    public String getSpotId() { return spotId; }
    public SpotType getType() { return type; }
    public double getHourlyRate() { return hourlyRate; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public Vehicle getCurrentVehicle() { return currentVehicle; }
    public void setCurrentVehicle(Vehicle vehicle) { currentVehicle = vehicle; }
    public int getFloorNumber() { return floorNumber; }
    public int getRowNumber() { return rowNumber; }
    public int getSpotNumber() { return spotNumber; }

    @Override
    public String toString() {
        return String.format("%s - %s - RM%.2f/hour - %s",
                spotId, type.getName(), hourlyRate,
                isAvailable ? "Available" : "Occupied");
    }
}
