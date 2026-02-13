/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ACER
 */
public class FixedFine implements FineScheme{
    
    //Override
    public double FineCalculation(double hours, boolean isReservedMisuse){
        
        if(isReservedMisuse){
            return 50.0; 
        }
        
        if(hours>24)
            return 50.0;
        else
            return 0;
    }
}
