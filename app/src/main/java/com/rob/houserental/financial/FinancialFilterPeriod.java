package com.rob.houserental.financial;

public class FinancialFilterPeriod {

    public enum Type {
        THIS_MONTH,
        LAST_MONTH,
        THIS_YEAR,
        LAST_YEAR,
        CUSTOM
    }

    private Type type;
    private String startDate; // yyyy-MM-dd
    private String endDate;   // yyyy-MM-dd
    private String startMonth; // yyyy-MM
    private String endMonth;   // yyyy-MM

    public FinancialFilterPeriod(Type type) {
        this.type = type;
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat mFormat = new java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault());
        java.text.SimpleDateFormat dFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        int year = cal.get(java.util.Calendar.YEAR);

        switch (type) {
            case THIS_MONTH:
                this.startMonth = mFormat.format(cal.getTime());
                this.endMonth = this.startMonth;
                this.startDate = this.startMonth + "-01";
                this.endDate = this.startMonth + "-31";
                break;
            case LAST_MONTH:
                cal.add(java.util.Calendar.MONTH, -1);
                this.startMonth = mFormat.format(cal.getTime());
                this.endMonth = this.startMonth;
                this.startDate = this.startMonth + "-01";
                this.endDate = this.startMonth + "-31";
                break;
            case THIS_YEAR:
                this.startMonth = year + "-01";
                this.endMonth = year + "-12";
                this.startDate = year + "-01-01";
                this.endDate = year + "-12-31";
                break;
            case LAST_YEAR:
                int lastYear = year - 1;
                this.startMonth = lastYear + "-01";
                this.endMonth = lastYear + "-12";
                this.startDate = lastYear + "-01-01";
                this.endDate = lastYear + "-12-31";
                break;
            case CUSTOM:
                break;
        }
    }

    public FinancialFilterPeriod(Type type, String startMonth, String endMonth) {
        this.type = type;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        if (startMonth != null) this.startDate = startMonth + "-01";
        if (endMonth != null) this.endDate = endMonth + "-31";
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(String startMonth) {
        this.startMonth = startMonth;
    }

    public String getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(String endMonth) {
        this.endMonth = endMonth;
    }
}
