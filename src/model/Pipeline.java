package model;

public class Pipeline {
    private String fromId, toId;
    private double weight;
    private boolean active;

    public Pipeline(String fromId, String toId, double weight) {
        this.fromId = fromId; this.toId = toId;
        this.weight = weight; this.active = true;
    }
    public String getFromId() { return fromId; }
    public String getToId() { return toId; }
    public double getWeight() { return weight; }
    public boolean isActive() { return active; }
    public void markFailed() { this.active = false; }
    public void markActive() { this.active = true; }
}