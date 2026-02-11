package model; // Inside the ParkingLot package

import model.Floor;
import model.ParkingSpot;
import model.RegularSpot;
import model.CompactSpot;
import model.ReservedSpot;
import model.HandicappedSpot;
import java.util.ArrayList;
import java.util.List;

public class ParkingLot {
    private String name;
    private List<Floor> floors;

    public ParkingLot(String name) {
        this.name = name;
        this.floors = new ArrayList<>();
    }

    public void initialize(int numFloors, int spotsPerFloor) {
        int spotsPerRow = 10; // Let's assume 10 spots make 1 row

        for (int i = 1; i <= numFloors; i++) {
            String floorID = "Floor " + i;
            Floor floor = new Floor(floorID);

            for (int j = 1; j <= spotsPerFloor; j++) {
                // Logic to calculate Row and Spot number within that row
                int rowNum = ((j - 1) / spotsPerRow) + 1;
                int spotNum = ((j - 1) % spotsPerRow) + 1;

                // New ID Format: F1-R1-S1
                String spotID = "F" + i + "-R" + rowNum + "-S" + spotNum;
                
                ParkingSpot spot;
                // Distribute types (Same percentages as before)
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