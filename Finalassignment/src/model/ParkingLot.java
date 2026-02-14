package model;

import java.util.ArrayList;
import java.util.List;

public class ParkingLot {
    private final String name;
    private final List<Floor> floors;

    public ParkingLot(String name) {
        this.name = name;
        this.floors = new ArrayList<>();
    }

    public void initialize(int numFloors, int spotsPerFloor) {
        // 1. Set spots per row to 5 (This creates 4 rows total: 20 / 5 = 4)
        int spotsPerRow = 5; 

        // Calculate total rows dynamically (e.g., 20 spots / 5 = 4 rows)
        int totalRows = (spotsPerFloor + spotsPerRow - 1) / spotsPerRow;

        for (int i = 1; i <= numFloors; i++) {
            String floorID = "Floor " + i;
            Floor floor = new Floor(floorID);

            for (int j = 1; j <= spotsPerFloor; j++) {
                // Calculate Row and Spot Number
                int rowNum = ((j - 1) / spotsPerRow) + 1;
                int spotNum = ((j - 1) % spotsPerRow) + 1;

                // ID Format: F1-R1-S1
                String spotID = "F" + i + "-R" + rowNum + "-S" + spotNum;
                
                ParkingSpot spot;

                // --- NEW LAYOUT LOGIC ---
                if (i == 1) {
                    // Rule 1: Floor 1 is ALL Reserved
                    spot = new ReservedSpot(spotID);
                } else {
                    // Rule 2: Floor 2+
                    if (rowNum == 1) {
                        // Row 1: Compact
                        spot = new CompactSpot(spotID);
                    } else if (rowNum == totalRows) { // Row 4 (The last row)
                        // Last Row: Handicapped
                        spot = new HandicappedSpot(spotID);
                    } else {
                        // Middle Rows (Row 2 & 3): Regular
                        spot = new RegularSpot(spotID);
                    }
                }
                // ------------------------

                floor.addSpot(spot);
            }
            floors.add(floor);
        }
    }
    
    public ParkingSpot getSpotById(String spotId) {
        for (Floor floor : floors) {
            for (ParkingSpot spot : floor.getSpots()) {
                if (spot.getSpotID().equals(spotId)) {
                    return spot;
                }
            }
        }
        return null; // can't find
    }

    // This method fixes the error in ReportService
    public int getTotalCapacity() {
        int total = 0;
        for (Floor floor : floors) {
            total += floor.getSpots().size();
        }
        return total;
    }

    public String getName() { return name; }
    public List<Floor> getFloors() { return floors; }
}