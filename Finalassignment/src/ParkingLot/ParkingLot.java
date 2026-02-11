package ParkingLot;

import java.util.ArrayList;
import java.util.List;

public class ParkingLot {
    private String name;
    private List<Floor> floors;

    public ParkingLot(String name) {
        this.name = name;
        this.floors = new ArrayList<>();
    }

    // Logic to build the floors and spots
    public void initialize(int numFloors, int spotsPerFloor) {
        for (int i = 1; i <= numFloors; i++) {
            String floorID = "Floor " + i;
            Floor floor = new Floor(floorID);

            // Distribute spot types:
            // 20% Compact, 50% Regular, 10% Handicapped, 20% Reserved
            for (int j = 1; j <= spotsPerFloor; j++) {
                String spotID = "F" + i + "-S" + j; 
                
                ParkingSpot spot;
                if (j <= spotsPerFloor * 0.2) {
                    spot = new CompactSpot(spotID);
                } else if (j <= spotsPerFloor * 0.7) {
                    spot = new RegularSpot(spotID);
                } else if (j <= spotsPerFloor * 0.8) {
                    spot = new HandicappedSpot(spotID);
                } else {
                    spot = new ReservedSpot(spotID);
                }
                floor.addSpot(spot);
            }
            floors.add(floor);
        }
    }

    public String getName() { return name; }
    public List<Floor> getFloors() { return floors; }
}