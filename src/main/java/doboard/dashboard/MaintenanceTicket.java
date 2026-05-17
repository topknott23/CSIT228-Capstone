package doboard.dashboard;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class MaintenanceTicket {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty unit;
    private final SimpleStringProperty issue;

    public MaintenanceTicket(int id, String unit, String issue) {
        this.id = new SimpleIntegerProperty(id);
        this.unit = new SimpleStringProperty(unit);
        this.issue = new SimpleStringProperty(issue);
    }

    public int getId() { return id.get(); }
    public SimpleIntegerProperty idProperty() { return id; }

    public String getUnit() { return unit.get(); }
    public SimpleStringProperty unitProperty() { return unit; }

    public String getIssue() { return issue.get(); }
    public SimpleStringProperty issueProperty() { return issue; }
}
