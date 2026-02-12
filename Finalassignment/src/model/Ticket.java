package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    private final String ticketId;    // 格式：T-PLATE-TIMESTAMP
    private final String plateNumber;
    private final String vehicleType; 
    private final String spotId;
    private final LocalDateTime entryTime;

    private Ticket(Builder builder) {
        this.plateNumber = builder.plateNumber;
        this.vehicleType = builder.vehicleType;
        this.spotId = builder.spotId;
        this.entryTime = LocalDateTime.now();
        
        // autogenerate Ticket number
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        this.ticketId = "T-" + plateNumber + "-" + this.entryTime.format(fmt);
    }

    // Getters
    public String getTicketId() { return ticketId; }
    public String getPlateNumber() { return plateNumber; }
    public String getVehicleType() { return vehicleType; }
    public String getSpotId() { return spotId; }
    public LocalDateTime getEntryTime() { return entryTime; }

    public String getFormattedEntryTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return this.entryTime.format(formatter);
}
    // Builder Pattern 
    public static class Builder {
        private String plateNumber;
        private String vehicleType;
        private String spotId;

        public Builder(String plateNumber, String spotId) {
            this.plateNumber = plateNumber;
            this.spotId = spotId;
        }

        public Builder setVehicleType(String vehicleType) {
            this.vehicleType = vehicleType;
            return this;
        }

        public Ticket build() {
            return new Ticket(this);
        }
        
    }
}