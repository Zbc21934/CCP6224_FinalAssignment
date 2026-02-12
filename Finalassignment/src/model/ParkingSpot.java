package model;

// 1. The Abstract Parent Class

import model.Vehicle;

public abstract class ParkingSpot {
    protected String spotID;
    protected boolean isOccupied;
    protected double hourlyRate;
    protected Vehicle currentVehicle;

    public ParkingSpot(String spotID, double hourlyRate) {
        this.spotID = spotID;
        this.hourlyRate = hourlyRate;
        this.isOccupied = false;
        this.currentVehicle = null;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public String getSpotID() {
        return spotID;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void assignVehicle(Vehicle v) {
        this.currentVehicle = v;
        this.isOccupied = true;
    }

    public void removeVehicle() {
        this.currentVehicle = null;
        this.isOccupied = false;
    }

    public Vehicle getVehicle() {
        return this.currentVehicle;
    }
    
    @Override
    public String toString() {
        return spotID + " [" + this.getClass().getSimpleName() + "] - Rate: RM" + hourlyRate;
    }
}

// 2. The Subclasses (Non-public, so they can live in the same file)

class CompactSpot extends ParkingSpot {
    public CompactSpot(String spotID) {
        super(spotID, 2.0); // RM 2/hour
    }
}

class RegularSpot extends ParkingSpot {
    public RegularSpot(String spotID) {
        super(spotID, 5.0); // RM 5/hour
    }
}

class HandicappedSpot extends ParkingSpot {
    public HandicappedSpot(String spotID) {
        super(spotID, 2.0); // RM 2/hour
    }
}

class ReservedSpot extends ParkingSpot {
    public ReservedSpot(String spotID) {
        super(spotID, 10.0); // RM 10/hour
    }
}