package ParkingLot;

import java.util.ArrayList;
import java.util.List;

public class Floor {
    private String floorID;
    private List<ParkingSpot> spots;

    public Floor(String floorID) {
        this.floorID = floorID;
        this.spots = new ArrayList<>();
    }

    public void addSpot(ParkingSpot spot) {
        spots.add(spot);
    }

    public String getFloorID() { return floorID; }
    public List<ParkingSpot> getSpots() { return spots; }
}