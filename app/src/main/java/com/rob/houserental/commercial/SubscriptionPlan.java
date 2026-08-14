package com.rob.houserental.commercial;

public class SubscriptionPlan {

    private final String planCode;
    private final String displayName;
    private final int durationDays;
    private final long priceMinor; // e.g. 149900 for 1499 BDT
    private final String currency; // "BDT"
    private final boolean active;
    private final int sortOrder;

    public SubscriptionPlan(String planCode, String displayName, int durationDays, long priceMinor, String currency, boolean active, int sortOrder) {
        this.planCode = planCode;
        this.displayName = displayName;
        this.durationDays = durationDays;
        this.priceMinor = priceMinor;
        this.currency = currency;
        this.active = active;
        this.sortOrder = sortOrder;
    }

    public String getPlanCode() {
        return planCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public long getPriceMinor() {
        return priceMinor;
    }

    public double getPriceMajor() {
        return priceMinor / 100.0;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isActive() {
        return active;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
