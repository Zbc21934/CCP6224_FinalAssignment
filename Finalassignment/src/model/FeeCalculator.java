package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class FeeCalculator {

    public static double calculate(Vehicle vehicle, ParkingSpot spot, LocalDateTime entryTime, boolean isHandicappedCard) {
        long hours = getDurationInHours(entryTime);

        //handicapped check
        if (isHandicappedCard) {
            // Case A, HAVE THE CARD M PARK AT HANDCIPPED PARKING =FREE
            if (spot instanceof HandicappedSpot) {
                System.out.println("ℹ️ OKU Free Parking Applied");
                return 0.0;
            }//CASE B, HAVE CARD BUT NO PARKING AT THE HANDCIPPED PARK
            System.out.println("ℹ️ OKU Flat Rate Applied (RM 2.0)");
            return hours * 2.0;

        }
        //CASE C: NORMA USER
        double rate = (spot != null) ? spot.getHourlyRate() : 2.0;

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
