package model;

public class Tank {
    private String id, name;
    private double capacity, currentLevel;

    public Tank(String id, String name, double capacity, double currentLevel) {
        this.id = id; this.name = name;
        this.capacity = capacity; this.currentLevel = currentLevel;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public double getCapacity() { return capacity; }
    public double getCurrentLevel() { return currentLevel; }
}