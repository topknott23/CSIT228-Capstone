package doboard.dashboard;

public class MaintenanceRequest {
    private final int id;
    private final String unit; // Displays: Dorm Name (Username)
    private final String issue;
    private final String status;

    public MaintenanceRequest(int id, String unit, String issue, String status) {
        this.id = id;
        this.unit = unit;
        this.issue = issue;
        this.status = status;
    }

    public int getId() { return id; }
    public String getUnit() { return unit; }
    public String getIssue() { return issue; }
    public String getStatus() { return status; }
}