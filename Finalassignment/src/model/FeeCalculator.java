package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class FeeCalculator {

    public static double calculate(Vehicle vehicle, ParkingSpot spot, LocalDateTime entryTime, boolean isHandicappedCard) {
        long hours = getDurationInHours(entryTime);

        // Requirement: "Handicapped Vehicle... gets a discounted price of RM 2/hour only for a handicapped card holder"
        if (isHandicappedCard && vehicle instanceof model.HandicappedVehicle) {
            
            // Sub-rule: If they park in a Handicapped Spot -> FREE 
            if (spot instanceof model.HandicappedSpot) {
                System.out.println("ℹ️ OKU Privilege: Free Parking (Handicapped Spot)");
                return 0.0;
            } 
            
            // Sub-rule: If they park in ANY other spot (Regular/Reserved) -> Discounted to RM 2.0/hr 
            System.out.println("ℹ️ OKU Privilege: Flat Rate RM 2.0/hr Applied");
            return hours * 2.0;
        }
        
        // If it's a regular Car/SUV, OR a Handicapped Vehicle WITHOUT a card,
        // they pay the standard rate of the spot.
        // Compact: 2.0, Regular: 5.0, Reserved: 10.0
        double rate = (spot != null) ? spot.getHourlyRate() : 0.0;
        return hours * rate;
    }

    public static long getDurationInHours(LocalDateTime entryTime) {
        LocalDateTime exitTime = LocalDateTime.now(); //set current time as exit time

        Duration duration = Duration.between(entryTime, exitTime);

        long hours = duration.toHours(); // rounding up

        //1.05 is 2 hours
        if (duration.toMinutes() % 60 > 0) {
            hours++;
        }

        if (hours <= 0) {
            hours = 1;
        }

        return hours;
    }
}
