package model;

public abstract class Vehicle {
    protected String licensePlate;
    protected String type;
    
    public Vehicle(String licensePlate, String type){
        this.licensePlate = licensePlate;
        this.type = type;
    }
    
    public abstract boolean canParkIn(ParkingSpot spot);
    
    public String getLicensePlate() { return licensePlate; }
    public String getType() { return type; }
    public abstract double getHourlyRate();
    
    public static Vehicle create(String plate, String typeStr) {
        switch (typeStr.toUpperCase()) {
            case "MOTORCYCLE": 
                return new Motorcycle(plate);
            case "CAR":
                return new Car(plate);
            case "SUV":        
                return new SUV(plate);
            case "HANDICAPPED":
                return new HandicappedVehicle(plate);
            default: 
                throw new IllegalArgumentException("Unknown type: " + typeStr);
        }
    }
}
