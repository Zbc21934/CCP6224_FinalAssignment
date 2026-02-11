package Main; 

import ParkingLot.*; 

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Now Main can find "ParkingSystemFacade" because of the import above
        ParkingSystemFacade parkingSystem = new ParkingSystemFacade();
        
        ParkingLot lot = parkingSystem.getParkingLot();

        System.out.println("=== Parking Lot System Initialized ===");
        
        for (Floor floor : lot.getFloors()) {
            System.out.println("\n--- [ " + floor.getFloorID() + " ] ---");
            for (ParkingSpot spot : floor.getSpots()) {
                System.out.println("   " + spot.toString());
            }
        }
    }
}