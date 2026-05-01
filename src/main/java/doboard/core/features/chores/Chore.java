package doboard.core.features.chores;

import doboard.core.common.enums.Frequency;

import java.time.LocalDate;

public class Chore {
    final int chore_id;
    private int dorm_id;
    String title;
    String description;
    Frequency frequency;
    LocalDate due_date;
    Status status;

    public enum Status {
        PENDING,
        COMPLETE
    }

    public Chore(int chore_id, int dorm_id, String title, String description, Frequency frequency, LocalDate due_date, Status status) {
        this.chore_id = chore_id;
        this.dorm_id = dorm_id;
        this.title = title;
        this.description = description;
        this.frequency = frequency;
        this.due_date = due_date;
        this.status = status;
    }

    //GETTERS
    public int getChore_id() {
        return chore_id;
    }

    public int getDorm_id() {
        return dorm_id;
    }
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public LocalDate getDue_date() {
        return due_date;
    }

    public Status getStatus() {
        return status;
    }


    //SETTERS
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public void setDue_date(LocalDate due_date) {
        this.due_date = due_date;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
