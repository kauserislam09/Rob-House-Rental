package com.rob.houserental.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RentDateUtils {

    private static final SimpleDateFormat FORMAT_MONTH_KEY = new SimpleDateFormat("yyyy-MM", Locale.US);
    private static final SimpleDateFormat FORMAT_MONTH_DISPLAY = new SimpleDateFormat("MMMM yyyy", Locale.US);
    private static final SimpleDateFormat FORMAT_DUE_DATE = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    private static final SimpleDateFormat[] PARSE_FORMATS = new SimpleDateFormat[]{
            new SimpleDateFormat("dd MMM yyyy", Locale.US),
            new SimpleDateFormat("d MMM yyyy", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd", Locale.US),
            new SimpleDateFormat("dd-MM-yyyy", Locale.US),
            new SimpleDateFormat("dd/MM/yyyy", Locale.US),
            new SimpleDateFormat("MM/dd/yyyy", Locale.US),
            new SimpleDateFormat("yyyy-MM", Locale.US),
            new SimpleDateFormat("MMMM yyyy", Locale.US)
    };

    /**
     * Computes the exact due date string for a given billing month and rent due day,
     * safely adjusting for months with fewer days (e.g. Feb 28/29, Apr 30).
     */
    public static String computeDueDate(String billingMonth, int rentDueDay) {
        Calendar cal = Calendar.getInstance();

        Date monthDate = parseDate(billingMonth);
        if (monthDate != null) {
            cal.setTime(monthDate);
        }

        int targetDay = rentDueDay > 0 ? rentDueDay : 10;
        int maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int actualDay = Math.min(targetDay, maxDaysInMonth);

        cal.set(Calendar.DAY_OF_MONTH, actualDay);
        return FORMAT_DUE_DATE.format(cal.getTime());
    }

    /**
     * Checks whether the due date has already passed.
     * Evaluates against the end of the due day (23:59:59.999).
     */
    public static boolean isDueDatePassed(String dueDateStr) {
        if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
            return false;
        }

        Date parsed = parseDate(dueDateStr.trim());
        if (parsed == null) {
            return false;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(parsed);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        return System.currentTimeMillis() > cal.getTimeInMillis();
    }

    /**
     * Centralized status calculation rule:
     * - PAID: remainingAmount <= 0.001
     * - WAIVED: explicitly marked as WAIVED
     * - OVERDUE: remainingAmount > 0 AND dueDate has passed (for both UNPAID and PARTIAL balances)
     * - PARTIAL: remainingAmount > 0 AND amountPaid > 0 AND dueDate has NOT passed
     * - UNPAID: remainingAmount > 0 AND amountPaid <= 0 AND dueDate has NOT passed
     */
    public static String calculateStatus(
            double amountDue,
            double amountPaid,
            double remainingAmount,
            String dueDate,
            String currentStatus
    ) {
        if ("WAIVED".equalsIgnoreCase(currentStatus)) {
            return "WAIVED";
        }

        if (remainingAmount <= 0.001) {
            return "PAID";
        }

        boolean duePassed = isDueDatePassed(dueDate);

        if (duePassed) {
            return "OVERDUE";
        }

        if (amountPaid > 0.001) {
            return "PARTIAL";
        }

        return "UNPAID";
    }

    /**
     * Extracts the day of month from a start date string if valid (1-31).
     */
    public static int extractDayOfMonth(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return 10;
        }
        Date date = parseDate(dateStr.trim());
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal.get(Calendar.DAY_OF_MONTH);
        }
        return 10;
    }

    public static Date parseDate(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        String cleaned = input.trim();
        for (SimpleDateFormat format : PARSE_FORMATS) {
            try {
                format.setLenient(false);
                return format.parse(cleaned);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    /**
     * Returns true if the given billing month (e.g. "2026-07") is before the tenancy start month (e.g. "2026-08").
     */
    public static boolean isBillingMonthBeforeTenancyStart(String billingMonth, String startDateStr) {
        if (startDateStr == null || startDateStr.trim().isEmpty() || billingMonth == null || billingMonth.trim().isEmpty()) {
            return false;
        }
        Date monthDate = parseDate(billingMonth.trim());
        Date startDate = parseDate(startDateStr.trim());
        if (monthDate != null && startDate != null) {
            Calendar calMonth = Calendar.getInstance();
            calMonth.setTime(monthDate);
            Calendar calStart = Calendar.getInstance();
            calStart.setTime(startDate);

            int monthIndex = calMonth.get(Calendar.YEAR) * 12 + calMonth.get(Calendar.MONTH);
            int startIndex = calStart.get(Calendar.YEAR) * 12 + calStart.get(Calendar.MONTH);
            return monthIndex < startIndex;
        }
        return false;
    }
}
