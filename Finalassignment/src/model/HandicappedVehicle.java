/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ACER
 */
public class HandicappedVehicle extends Vehicle{
    
    public HandicappedVehicle(String licensePlate){
        super(licensePlate,"Handicapped");
    }
    
    public boolean canParkIn(ParkingSpot spot) {
        return true; // can park any spot
    }
    
    @Override
    public double getHourlyRate() {
        return 2.0; 
    }
}
