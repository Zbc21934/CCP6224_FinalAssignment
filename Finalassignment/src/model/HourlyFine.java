/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ACER
 */
public class HourlyFine implements FineScheme{
    
    public double FineCalculation(double hours, boolean isReservedMisuse){
        double fineableHours = 0;

        if (isReservedMisuse) {
            fineableHours = hours;
        } else {
            // normal situation
            if (hours > 24) {
                fineableHours = hours - 24;
            }
        }
        
        // Ceiling and calculate fines
        return Math.ceil(fineableHours) * 20.0;
    
    }
}
