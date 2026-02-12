/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author PatrickToh
 */
import java.time.Duration;
import java.time.LocalDateTime;

public class FeeCalculator {

    public static double calculate(Vehicle vehicle, ParkingSpot spot, LocalDateTime entryTime) {
        long hours = getDurationInHours(entryTime);

        double rate = vehicle.getHourlyRate();

        // oku
        if (spot != null && vehicle instanceof HandicappedVehicle && spot instanceof HandicappedSpot) {
             rate = 0.0; 
        }

        return hours * rate;
    }

    public static long getDurationInHours(LocalDateTime entryTime) {
        long hours = Duration.between(entryTime, LocalDateTime.now()).toHours();
        return (hours == 0) ? 1 : hours;
    }
}