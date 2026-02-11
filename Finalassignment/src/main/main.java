package main;

import database.DbConnection; 

public class main {

    public static void main(String[] args) {
        System.out.println("System Starting...");
        
        DbConnection.getInstance();
        
        System.out.println("System Initialized. You can check the database file now.");
    }
}