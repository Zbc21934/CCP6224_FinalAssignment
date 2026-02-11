package ParkingLot;

public class ParkingSystemFacade {
    private ParkingLot parkingLot;

    public ParkingSystemFacade() {
        // Create the Parking Lot
        this.parkingLot = new ParkingLot("University Parking");
        
        // Requirement 1: Initialize 5 Floors with 20 spots each
        this.parkingLot.initialize(5, 20);
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }
}