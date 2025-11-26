package com.budgetwise.ad;

public class RecurringExpense {
    public String recurringId, userId, categoryId, title, note, interval;
    public double amount;
    public long startDate, nextRunDate;

    public RecurringExpense(String recurringId, String userId, String categoryId,
                            String title, double amount, String note,
                            String interval, long startDate, long nextRunDate) {
        this.recurringId = recurringId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.amount = amount;
        this.note = note;
        this.interval = interval;
        this.startDate = startDate;
        this.nextRunDate = nextRunDate;
    }
}