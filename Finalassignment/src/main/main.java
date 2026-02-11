package main;

import controller.ParkingSystemFacade;

public class Main {
    public static void main(String[] args) {
        System.out.println("?System Starting...");
        
        ParkingSystemFacade facade = new ParkingSystemFacade();
        
        // TEST DB
        boolean success = facade.parkVehicle("JQA1234", "Car", "F1-S01");
        
        if (success) {
            System.out.println("Database Connected & Vehicle Parked Successfully!");
        } else {
            System.out.println("Insert failed. Check console for SQL errors.");
        }
    }
}