package model;

public class LeakReport implements Comparable<LeakReport> {
    private int id, severity;
    private String areaId, description;
    private long reportedTimestamp;

    public LeakReport(int id, String areaId, int severity, String description, long reportedTimestamp) {
        this.id = id; this.areaId = areaId; this.severity = severity;
        this.description = description; this.reportedTimestamp = reportedTimestamp;
    }
    public int getId() { return id; }
    public String getAreaId() { return areaId; }
    public int getSeverity() { return severity; }
    public String getDescription() { return description; }
    public long getReportedTimestamp() { return reportedTimestamp; }

    @Override
    public int compareTo(LeakReport o) {
        if (this.severity != o.severity) return o.severity - this.severity;
        return Long.compare(this.reportedTimestamp, o.reportedTimestamp);
    }
}