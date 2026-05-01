package doboard.core.models;

import java.time.LocalDate;

public class BillDisplay {
    private int splitId;
    private String title;
    private double totalAmount;
    private double amountOwed;
    private LocalDate dueDate;
    private boolean isPaid;

    public BillDisplay(int splitId, String title, double totalAmount, double amountOwed, LocalDate dueDate, boolean isPaid) {
        this.splitId = splitId;
        this.title = title;
        this.totalAmount = totalAmount;
        this.amountOwed = amountOwed;
        this.dueDate = dueDate;
        this.isPaid = isPaid;
    }

    public int getSplitId() { return splitId; }
    public String getTitle() { return title; }
    public double getTotalAmount() { return totalAmount; }
    public double getAmountOwed() { return amountOwed; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isPaid() { return isPaid; }
}
