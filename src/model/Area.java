package model;

public class Area {
    private String id, name;
    private double waterDemand, waterAvailable;

    public Area(String id, String name, double waterDemand, double waterAvailable) {
        this.id = id; this.name = name;
        this.waterDemand = waterDemand; this.waterAvailable = waterAvailable;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public double getWaterDemand() { return waterDemand; }
    public double getWaterAvailable() { return waterAvailable; }
    public boolean isShortage() { return waterAvailable < waterDemand; }
    public double shortageAmount() { return waterDemand - waterAvailable; }
}