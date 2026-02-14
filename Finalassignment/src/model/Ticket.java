package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    private String ticketId;
    private String plateNumber;
    private String spotId;
    private String vehicleType;
    private LocalDateTime entryTime;
    private boolean handicappedCardHolder; 
    
    // ✅ NEW: Field to track if this ticket is for a violation (e.g., unauthorized parking)
    private boolean isViolation; 

    // Private constructor (updated to accept isViolation)
    private Ticket(String plateNumber, String spotId, String vehicleType, boolean isHandicapped, boolean isViolation) {
        this.ticketId = "T-" + plateNumber + "-" + System.currentTimeMillis();
        this.plateNumber = plateNumber;
        this.spotId = spotId;
        this.vehicleType = vehicleType;
        this.entryTime = LocalDateTime.now();
        this.handicappedCardHolder = isHandicapped;
        this.isViolation = isViolation; // Assign the value
    }

    // ✅ Updated Factory Method to create a ticket with violation status
    public static Ticket createEntryTicket(String plateNumber, String spotId, String vehicleType, boolean isHandicapped, boolean isViolation) {
        return new Ticket(plateNumber, spotId, vehicleType, isHandicapped, isViolation);
    }

    // Getters
    public String getTicketId() { return ticketId; }
    public String getPlateNumber() { return plateNumber; }
    public String getSpotId() { return spotId; }
    public String getVehicleType() { return vehicleType; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public boolean isHandicappedCardHolder() { return handicappedCardHolder; }
    
    // ✅ NEW Getter for violation status (used by Facade to calculate fines)
    public boolean isViolation() { return isViolation; }

    public String getFormattedEntryTime() {
        return entryTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}