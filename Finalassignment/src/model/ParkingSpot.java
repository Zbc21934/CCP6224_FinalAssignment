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

