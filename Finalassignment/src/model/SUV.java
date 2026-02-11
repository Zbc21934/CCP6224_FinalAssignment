/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ACER
 */
public class SUV extends Vehicle{
    public SUV(String licensePlate) {
        super(licensePlate, "SUV");
    }
    
    public boolean canParkIn(ParkingSpot spot) {
        return spot instanceof RegularSpot;
    }
}
