package doboard.billing;

import java.time.LocalDate;
import java.time.Instant;

public class Bill {
    private final int bill_id;
    private int dorm_id;
    private String title;
    private double total_amount;
    private LocalDate bill_due_date;
    private Instant created_at;

    public Bill(int bill_id, int dorm_id, String title, double total_amount, LocalDate bill_due_date) {
        this.bill_id = bill_id;
        this.dorm_id = dorm_id;
        this.title = title;
        this.total_amount = total_amount;
        this.bill_due_date = bill_due_date;
        this.created_at = Instant.now();
    }


    //GETTERS
    public int getBill_id() {
        return bill_id;
    }

    public int getBill_dorm_id() {
        return dorm_id;
    }

    public String getTitle() {
        return title;
    }

    public double getTotal_amount() {
        return total_amount;
    }

    public LocalDate getBill_due_date() {
        return bill_due_date;
    }

    public Instant getCreated_at() {
        return created_at;
    }


    //SETTERS
    public void setBill_dorm_id(int dorm_id) {
        this.dorm_id = dorm_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTotal_amount(double total_amount) {
        this.total_amount = total_amount;
    }

    public void setBill_due_date(LocalDate bill_due_date) {
        this.bill_due_date = bill_due_date;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }
}
