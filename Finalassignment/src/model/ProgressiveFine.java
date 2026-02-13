/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ACER
 */
public class ProgressiveFine implements FineScheme{
    
    public double FineCalculation(double hours, boolean isReservedMisuse){
        double fineableHours;
        double fine = 0.0;
        
        
        //calculate the fine Hours
        if(isReservedMisuse){
            fineableHours = hours;
        }else{
            if(hours<=24)
                return 0.0;
            
            fineableHours = hours-24; 
        }
        
        //calculate fine
        if(fineableHours > 0){
            fine += 50.0;
        }
        if(fineableHours > 24){
            fine += 100.0;
        }
        if(fineableHours > 48){
            fine += 150.0;
        }
        if(fineableHours > 72){
            fine += 200;
        }

        return fine;
    }
}
