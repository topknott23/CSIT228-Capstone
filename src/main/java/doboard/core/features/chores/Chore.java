package doboard.core.features.chores;

import doboard.core.common.enums.Frequency;

import java.time.LocalDate;

public class Chore {
    final int chore_id;
    String title;
    String description;
    Frequency frequency;
    LocalDate due_date;
    boolean status;

    public Chore(int chore_id, String description, Frequency frequency, LocalDate due_date, boolean status) {
        this.chore_id = chore_id;
        this.description = description;
        this.frequency = frequency;
        this.due_date = due_date;
        this.status = status;
    }


    //GETTERS
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

    public boolean isStatus() {
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

    public void setStatus(boolean status) {
        this.status = status;
    }
}
