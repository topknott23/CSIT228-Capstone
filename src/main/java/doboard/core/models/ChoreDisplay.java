package doboard.core.models;

import java.time.LocalDate;

public class ChoreDisplay {
    private int choreId;
    private String title;
    private String assigneeName;
    private LocalDate dueDate;
    private String status;

    public ChoreDisplay(int choreId, String title, String assigneeName, LocalDate dueDate, String status) {
        this.choreId = choreId;
        this.title = title;
        this.assigneeName = assigneeName;
        this.dueDate = dueDate;
        this.status = status;
    }

    public int getChoreId() { return choreId; }
    public String getTitle() { return title; }
    public String getAssigneeName() { return assigneeName; }
    public LocalDate getDueDate() { return dueDate; }
    public String getStatus() { return status; }
    
    public void setStatus(String status) { this.status = status; }
}
