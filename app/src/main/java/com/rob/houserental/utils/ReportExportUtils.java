package com.rob.houserental.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import com.rob.houserental.R;
import com.rob.houserental.financial.FinancialSummary;
import com.rob.houserental.financial.PropertyFinancialSummary;
import com.rob.houserental.model.ExpenseDisplayItem;
import com.rob.houserental.model.PaymentDisplayItem;
import com.rob.houserental.model.RentRecordDisplayItem;
import com.rob.houserental.model.UtilityBillDisplayItem;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportExportUtils {

    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public static void exportFinancialSummaryPdf(Context context, FinancialSummary summary, List<PropertyFinancialSummary> properties, String periodName, OutputStream out) throws Exception {
        PdfDocument document = new PdfDocument();
        int pageWidth = 595;
        int pageHeight = 842;
        int pageNumber = 1;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setTextSize(18f);
        titlePaint.setFakeBoldText(true);
        titlePaint.setColor(Color.BLACK);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(11f);
        textPaint.setColor(Color.DKGRAY);

        Paint boldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boldPaint.setTextSize(11f);
        boldPaint.setFakeBoldText(true);
        boldPaint.setColor(Color.BLACK);

        int y = 40;

        String appTitle = context.getString(R.string.app_name) + " — " + context.getString(R.string.financial_dashboard_title);
        canvas.drawText(appTitle, 40, y, titlePaint);
        y += 22;

        String meta = (periodName != null ? periodName : "") + " | " + dateFormat.format(new Date());
        canvas.drawText(meta, 40, y, textPaint);
        y += 24;

        canvas.drawLine(40, y, pageWidth - 40, y, textPaint);
        y += 20;

        String curr = context.getString(R.string.currency_symbol);
        canvas.drawText(context.getString(R.string.tab_report_dashboard), 40, y, boldPaint);
        y += 18;

        if (summary != null) {
            canvas.drawText(context.getString(R.string.reports_expected_rent) + ": " + curr + currencyFormatter.format(summary.getExpectedRent()), 40, y, textPaint);
            canvas.drawText(context.getString(R.string.reports_collected_rent) + ": " + curr + currencyFormatter.format(summary.getCollectedRent()), 300, y, textPaint);
            y += 16;

            canvas.drawText(context.getString(R.string.reports_outstanding_rent) + ": " + curr + currencyFormatter.format(summary.getOutstandingRent()), 40, y, textPaint);
            canvas.drawText(context.getString(R.string.reports_overdue_rent) + ": " + curr + currencyFormatter.format(summary.getOverdueRent()), 300, y, textPaint);
            y += 16;

            canvas.drawText(context.getString(R.string.reports_total_expenses) + ": " + curr + currencyFormatter.format(summary.getActiveExpenses()), 40, y, textPaint);
            canvas.drawText(context.getString(R.string.reports_utility_bills) + ": " + curr + currencyFormatter.format(summary.getUtilityBillsPaid()), 300, y, textPaint);
            y += 16;

            canvas.drawText(context.getString(R.string.reports_net_income) + ": " + curr + currencyFormatter.format(summary.getNetIncome()), 40, y, boldPaint);
            canvas.drawText(context.getString(R.string.reports_collection_rate) + ": " + String.format(Locale.getDefault(), "%.1f%%", summary.getCollectionRate()), 300, y, textPaint);
            y += 22;

            canvas.drawText(context.getString(R.string.reports_current_occupancy), 40, y, boldPaint);
            y += 18;
            canvas.drawText(context.getString(R.string.reports_total_units) + ": " + summary.getTotalUnits() + " | " +
                    context.getString(R.string.reports_occupied_units) + ": " + summary.getOccupiedUnits() + " | " +
                    context.getString(R.string.reports_vacant_units) + ": " + summary.getVacantUnits(), 40, y, textPaint);
            canvas.drawText(context.getString(R.string.reports_occupancy_rate) + ": " + String.format(Locale.getDefault(), "%.1f%%", summary.getOccupancyRate()), 300, y, textPaint);
            y += 24;
        }

        canvas.drawLine(40, y, pageWidth - 40, y, textPaint);
        y += 20;

        canvas.drawText(context.getString(R.string.reports_property_performance), 40, y, boldPaint);
        y += 20;

        if (properties != null && !properties.isEmpty()) {
            for (PropertyFinancialSummary prop : properties) {
                // Multi-page support: check vertical space
                if (y > pageHeight - 60) {
                    document.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 40;
                    canvas.drawText(appTitle + " (" + pageNumber + ")", 40, y, titlePaint);
                    y += 30;
                }

                canvas.drawText(prop.getPropertyName() + " (" + context.getString(R.string.reports_total_units) + ": " + prop.getTotalUnits() + ", " + context.getString(R.string.reports_occupied_units) + ": " + prop.getOccupiedUnits() + ")", 40, y, boldPaint);
                y += 16;
                canvas.drawText("  " + context.getString(R.string.reports_expected_rent) + ": " + curr + currencyFormatter.format(prop.getExpectedRent()) +
                        " | " + context.getString(R.string.reports_collected_rent) + ": " + curr + currencyFormatter.format(prop.getCollectedRent()) +
                        " | " + context.getString(R.string.reports_net_income) + ": " + curr + currencyFormatter.format(prop.getNetIncome()), 40, y, textPaint);
                y += 20;
            }
        }

        document.finishPage(page);
        document.writeTo(out);
        document.close();
    }

    public static void exportRentReportCsv(Context context, List<RentRecordDisplayItem> items, OutputStream out) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(R.string.csv_rent_header)).append("\n");
        if (items != null) {
            for (RentRecordDisplayItem item : items) {
                sb.append(item.id).append(",")
                        .append(csvEscape(item.tenantName)).append(",")
                        .append(csvEscape(item.propertyName)).append(",")
                        .append(csvEscape(item.unitNumber)).append(",")
                        .append(csvEscape(item.billingMonth)).append(",")
                        .append(csvEscape(item.dueDate)).append(",")
                        .append(item.amountDue).append(",")
                        .append(item.amountPaid).append(",")
                        .append(item.remainingAmount).append(",")
                        .append(csvEscape(item.status)).append(",")
                        .append(csvEscape(PaymentMethodUtils.getDisplayName(context, item.paymentMethod))).append("\n");
            }
        }
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void exportPaymentReportCsv(Context context, List<PaymentDisplayItem> items, OutputStream out) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(R.string.csv_payment_header)).append("\n");
        if (items != null) {
            for (PaymentDisplayItem item : items) {
                sb.append(item.id).append(",")
                        .append(csvEscape(item.paymentDate)).append(",")
                        .append(item.amount).append(",")
                        .append(csvEscape(PaymentMethodUtils.getDisplayName(context, item.paymentMethod))).append(",")
                        .append(csvEscape(item.reference)).append(",")
                        .append(csvEscape(item.tenantName)).append(",")
                        .append(csvEscape(item.propertyName)).append(",")
                        .append(csvEscape(item.unitNumber)).append(",")
                        .append(csvEscape(item.billingMonth)).append(",")
                        .append(csvEscape(item.notes)).append("\n");
            }
        }
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void exportExpenseReportCsv(Context context, List<ExpenseDisplayItem> items, OutputStream out) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(R.string.csv_expense_header)).append("\n");
        if (items != null) {
            for (ExpenseDisplayItem item : items) {
                sb.append(item.id).append(",")
                        .append(csvEscape(item.expenseDate)).append(",")
                        .append(csvEscape(item.category)).append(",")
                        .append(csvEscape(item.propertyName)).append(",")
                        .append(csvEscape(item.unitNumber)).append(",")
                        .append(item.amount).append(",")
                        .append(csvEscape(item.description)).append(",")
                        .append(item.isArchived ? "Yes" : "No").append("\n");
            }
        }
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void exportBillReportCsv(Context context, List<UtilityBillDisplayItem> items, OutputStream out) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(R.string.csv_bill_header)).append("\n");
        if (items != null) {
            for (UtilityBillDisplayItem item : items) {
                sb.append(item.id).append(",")
                        .append(csvEscape(item.billType)).append(",")
                        .append(csvEscape(item.propertyName)).append(",")
                        .append(csvEscape(item.unitNumber)).append(",")
                        .append(csvEscape(item.billingMonth)).append(",")
                        .append(item.amountDue).append(",")
                        .append(item.amountPaid).append(",")
                        .append(item.remainingAmount).append(",")
                        .append(csvEscape(item.status)).append(",")
                        .append(csvEscape(item.dueDate)).append("\n");
            }
        }
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void exportPropertyPerformanceCsv(Context context, List<PropertyFinancialSummary> items, OutputStream out) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(R.string.csv_property_header)).append("\n");
        if (items != null) {
            for (PropertyFinancialSummary item : items) {
                sb.append(item.getPropertyId()).append(",")
                        .append(csvEscape(item.getPropertyName())).append(",")
                        .append(item.getTotalUnits()).append(",")
                        .append(item.getOccupiedUnits()).append(",")
                        .append(item.getVacantUnits()).append(",")
                        .append(item.getExpectedRent()).append(",")
                        .append(item.getCollectedRent()).append(",")
                        .append(item.getOutstandingRent()).append(",")
                        .append(item.getExpenses()).append(",")
                        .append(item.getUtilityBillsPaid()).append(",")
                        .append(item.getNetIncome()).append(",")
                        .append(String.format(Locale.getDefault(), "%.1f", item.getCollectionRate())).append(",")
                        .append(String.format(Locale.getDefault(), "%.1f", item.getOccupancyRate())).append("\n");
            }
        }
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void exportMaintenanceReportPdf(Context context, List<com.rob.houserental.model.MaintenanceRecord> records, String filterSummary, OutputStream out) throws Exception {
        PdfDocument document = new PdfDocument();
        int pageWidth = 595;
        int pageHeight = 842;
        int pageNumber = 1;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setTextSize(16f);
        titlePaint.setFakeBoldText(true);
        titlePaint.setColor(Color.BLACK);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(10f);
        textPaint.setColor(Color.BLACK);

        Paint boldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boldPaint.setTextSize(10f);
        boldPaint.setFakeBoldText(true);
        boldPaint.setColor(Color.BLACK);

        int y = 40;
        canvas.drawText(context.getString(R.string.maintenance_title) + " Report", 40, y, titlePaint);
        y += 20;

        String meta = (filterSummary != null ? filterSummary : "") + " | " + dateFormat.format(new Date());
        canvas.drawText(meta, 40, y, textPaint);
        y += 25;

        canvas.drawText("Title", 40, y, boldPaint);
        canvas.drawText("Category", 180, y, boldPaint);
        canvas.drawText("Priority", 270, y, boldPaint);
        canvas.drawText("Status", 340, y, boldPaint);
        canvas.drawText("Sched/Comp Date", 420, y, boldPaint);
        canvas.drawText("Cost", 510, y, boldPaint);
        y += 10;

        Paint linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        canvas.drawLine(40, y, 555, y, linePaint);
        y += 15;

        if (records != null) {
            for (com.rob.houserental.model.MaintenanceRecord r : records) {
                if (y > pageHeight - 50) {
                    document.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 40;
                }

                String title = r.getTitle() != null ? (r.getTitle().length() > 22 ? r.getTitle().substring(0, 20) + ".." : r.getTitle()) : "";
                String cat = com.rob.houserental.adapter.MaintenanceAdapter.getCategoryDisplay(context, r.getCategory());
                String prio = com.rob.houserental.adapter.MaintenanceAdapter.getPriorityDisplay(context, r.getPriority());
                String stat = com.rob.houserental.adapter.MaintenanceAdapter.getStatusDisplay(context, r.getStatus());
                String dateStr = r.getCompletedDate() != null && !r.getCompletedDate().isEmpty() ? r.getCompletedDate() : (r.getScheduledDate() != null ? r.getScheduledDate() : "-");
                double cost = r.getActualCost() > 0 ? r.getActualCost() : r.getEstimatedCost();

                canvas.drawText(title, 40, y, textPaint);
                canvas.drawText(cat, 180, y, textPaint);
                canvas.drawText(prio, 270, y, textPaint);
                canvas.drawText(stat, 340, y, textPaint);
                canvas.drawText(dateStr, 420, y, textPaint);
                canvas.drawText("৳" + currencyFormatter.format(cost), 510, y, textPaint);
                y += 18;
            }
        }

        document.finishPage(page);
        document.writeTo(out);
        document.close();
    }

    public static void exportMaintenanceReportCsv(Context context, List<com.rob.houserental.model.MaintenanceRecord> records, OutputStream out) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Title,PropertyID,UnitID,Category,Priority,Status,Scheduled Date,Completed Date,Estimated Cost,Actual Cost,Vendor Name,Vendor Phone\n");
        if (records != null) {
            for (com.rob.houserental.model.MaintenanceRecord r : records) {
                sb.append(r.getId()).append(",")
                        .append(csvEscape(r.getTitle())).append(",")
                        .append(r.getPropertyId()).append(",")
                        .append(r.getUnitId() != null ? r.getUnitId() : "").append(",")
                        .append(csvEscape(com.rob.houserental.adapter.MaintenanceAdapter.getCategoryDisplay(context, r.getCategory()))).append(",")
                        .append(csvEscape(com.rob.houserental.adapter.MaintenanceAdapter.getPriorityDisplay(context, r.getPriority()))).append(",")
                        .append(csvEscape(com.rob.houserental.adapter.MaintenanceAdapter.getStatusDisplay(context, r.getStatus()))).append(",")
                        .append(csvEscape(r.getScheduledDate())).append(",")
                        .append(csvEscape(r.getCompletedDate())).append(",")
                        .append(r.getEstimatedCost()).append(",")
                        .append(r.getActualCost()).append(",")
                        .append(csvEscape(r.getVendorName())).append(",")
                        .append(csvEscape(r.getVendorPhone())).append("\n");
            }
        }
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String csvEscape(String text) {
        if (text == null) return "";
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
