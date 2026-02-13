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

    private Ticket(String plateNumber, String spotId, String vehicleType, boolean isHandicapped) {
        this.ticketId = "T-" + plateNumber + "-" + System.currentTimeMillis();
        this.plateNumber = plateNumber;
        this.spotId = spotId;
        this.vehicleType = vehicleType;
        this.entryTime = LocalDateTime.now();
        this.handicappedCardHolder = isHandicapped;
    }

    public static Ticket createEntryTicket(String plateNumber, String spotId, String vehicleType, boolean isHandicapped) {
        return new Ticket(plateNumber, spotId, vehicleType, isHandicapped);
    }

    //getter
    public String getTicketId() { return ticketId; }
    public String getPlateNumber() { return plateNumber; }
    public String getSpotId() { return spotId; }
    public String getVehicleType() { return vehicleType; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public boolean isHandicappedCardHolder() { return handicappedCardHolder; }

    public String getFormattedEntryTime() {
        return entryTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}