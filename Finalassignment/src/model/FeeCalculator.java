package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class FeeCalculator {
    public static double calculate(Vehicle vehicle, ParkingSpot spot, LocalDateTime entryTime) {
        long hours = getDurationInHours(entryTime);
        
        // 1. Rate
        // use the spot rate first, if got bug can use the vehicle rate for the backup plan
        double rate = (spot != null) ? spot.getHourlyRate() : vehicle.getHourlyRate();

        // oku free
        if (spot != null && vehicle instanceof HandicappedVehicle && spot instanceof HandicappedSpot) {
            System.out.println("ℹ️ Handicapped Discount Applied: Free Parking");
            return 0.0;
        }

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
        
        if (hours <= 0) hours = 1;
        
        return hours;
    }
}