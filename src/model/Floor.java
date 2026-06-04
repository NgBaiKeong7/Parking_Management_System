package model;

import java.util.*;
import utils.Constants.SpotType;

public class Floor {

    private int floorNumber;
    private List<ParkingSpot> parkingSpots;

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.parkingSpots = new ArrayList<>();
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public List<ParkingSpot> getParkingSpots() {
        return parkingSpots;
    }

    public void addParkingSpot(ParkingSpot spot) {
        parkingSpots.add(spot);
    }

    // ========================
    // OCCUPANCY UTILS
    // ========================

    // Return total number of spots grouped by SpotType
    public Map<SpotType, Integer> getTotalSpotsByType() {
        Map<SpotType, Integer> totals = new HashMap<>();
        for (ParkingSpot spot : parkingSpots) {
            SpotType type = spot.getType();
            totals.put(type, totals.getOrDefault(type, 0) + 1);
        }
        return totals;
    }

    // Return occupied spots grouped by SpotType
    public Map<SpotType, Integer> getOccupiedSpotsByType() {
        Map<SpotType, Integer> occupied = new HashMap<>();
        for (ParkingSpot spot : parkingSpots) {
            if (!spot.isAvailable()) {
                SpotType type = spot.getType();
                occupied.put(type, occupied.getOrDefault(type, 0) + 1);
            }
        }
        return occupied;
    }

    // Return total occupied spots for this floor
    public int getOccupiedCount() {
        int count = 0;
        for (ParkingSpot spot : parkingSpots) {
            if (!spot.isAvailable()) count++;
        }
        return count;
    }
}
