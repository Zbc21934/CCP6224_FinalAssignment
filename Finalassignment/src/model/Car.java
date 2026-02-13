/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ACER
 */
public class Car extends Vehicle{
    
    public Car(String licensePlate) {
        super(licensePlate, "Car");
    }
    
    public boolean canParkIn(ParkingSpot spot) {
        return spot instanceof CompactSpot || spot instanceof RegularSpot || spot instanceof ReservedSpot;
    }
    
    @Override
    public double getHourlyRate() {
        return 5.0; // price/hours
    }
}
