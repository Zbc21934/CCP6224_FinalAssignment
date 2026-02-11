/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ACER
 */
public class Motorcycle extends Vehicle{
    
    public Motorcycle(String licensePlate) {
        super(licensePlate, "Motorcycle");
    }

    
    public boolean canParkIn(ParkingSpot spot) {
        return spot instanceof CompactSpot;
    }
}
