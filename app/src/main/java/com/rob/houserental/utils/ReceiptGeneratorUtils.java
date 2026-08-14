package com.rob.houserental.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;

import com.rob.houserental.R;
import com.rob.houserental.model.RentRecordDisplayItem;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiptGeneratorUtils {

    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static void generateRentReceiptPdf(Context context, RentRecordDisplayItem item, OutputStream out) throws Exception {
        AppPreferences prefs = new AppPreferences(context);

        PdfDocument document = new PdfDocument();
        int pageWidth = 595;
        int pageHeight = 400; // Half A4 landscape style receipt

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setTextSize(18f);
        titlePaint.setFakeBoldText(true);
        titlePaint.setColor(Color.BLACK);

        Paint boldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boldPaint.setTextSize(12f);
        boldPaint.setFakeBoldText(true);
        boldPaint.setColor(Color.BLACK);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(11f);
        textPaint.setColor(Color.DKGRAY);

        Paint stampPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stampPaint.setTextSize(24f);
        stampPaint.setFakeBoldText(true);

        int y = 40;

        String businessTitle = prefs.getBusinessName();
        if (businessTitle == null || businessTitle.trim().isEmpty()) {
            businessTitle = context.getString(R.string.app_name);
        }

        canvas.drawText(businessTitle, 40, y, titlePaint);
        y += 20;

        String receiptHeader = context.getString(R.string.receipt_title) + " #" + item.id;
        canvas.drawText(receiptHeader, 40, y, boldPaint);
        canvas.drawText(context.getString(R.string.receipt_date) + ": " + dateFormat.format(new Date()), 380, y, textPaint);
        y += 24;

        canvas.drawLine(40, y, pageWidth - 40, y, textPaint);
        y += 20;

        String curr = context.getString(R.string.currency_symbol);

        canvas.drawText(context.getString(R.string.receipt_tenant) + ": " + item.tenantName + " (" + item.tenantPhone + ")", 40, y, textPaint);
        y += 18;
        canvas.drawText(context.getString(R.string.receipt_property) + ": " + item.propertyName + " (" + context.getString(R.string.reports_total_units) + ": " + item.unitNumber + ")", 40, y, textPaint);
        y += 18;
        canvas.drawText(context.getString(R.string.receipt_billing_month) + ": " + item.billingMonth, 40, y, textPaint);
        y += 24;

        canvas.drawText(context.getString(R.string.reports_expected_rent) + ": " + curr + currencyFormatter.format(item.amountDue), 40, y, boldPaint);
        canvas.drawText(context.getString(R.string.reports_collected_rent) + ": " + curr + currencyFormatter.format(item.amountPaid), 220, y, boldPaint);
        canvas.drawText(context.getString(R.string.reports_outstanding_rent) + ": " + curr + currencyFormatter.format(item.remainingAmount), 400, y, boldPaint);
        y += 28;

        canvas.drawLine(40, y, pageWidth - 40, y, textPaint);
        y += 25;

        // Landlord Details
        String landlordName = prefs.getLandlordName();
        if (landlordName != null && !landlordName.trim().isEmpty()) {
            canvas.drawText(context.getString(R.string.receipt_landlord) + ": " + landlordName + " (" + prefs.getLandlordPhone() + ")", 40, y, textPaint);
            y += 18;
        }

        // Status Stamp
        if ("PAID".equalsIgnoreCase(item.status)) {
            stampPaint.setColor(Color.parseColor("#2E7D32"));
            canvas.drawText(context.getString(R.string.receipt_paid_stamp), 380, y, stampPaint);
        } else if ("OVERDUE".equalsIgnoreCase(item.status)) {
            stampPaint.setColor(Color.parseColor("#C62828"));
            canvas.drawText(context.getString(R.string.receipt_overdue_stamp), 380, y, stampPaint);
        } else {
            stampPaint.setColor(Color.parseColor("#EF6C00"));
            canvas.drawText("• " + context.getString(R.string.receipt_partial_stamp) + " •", 380, y, stampPaint);
        }

        document.finishPage(page);
        document.writeTo(out);
        document.close();
    }

    public static String buildPaymentReminderMessage(Context context, RentRecordDisplayItem item) {
        AppPreferences prefs = new AppPreferences(context);
        String curr = context.getString(R.string.currency_symbol);
        String formattedAmount = curr + currencyFormatter.format(item.remainingAmount > 0 ? item.remainingAmount : item.amountDue);

        String landlord = prefs.getLandlordName();
        String accounts = prefs.getPaymentAccounts();

        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(R.string.reminder_greeting, item.tenantName)).append("\n\n");
        sb.append(context.getString(R.string.reminder_body, item.propertyName, item.unitNumber, item.billingMonth, formattedAmount, item.dueDate)).append("\n\n");

        if (accounts != null && !accounts.trim().isEmpty()) {
            sb.append(context.getString(R.string.reminder_payment_info, accounts)).append("\n\n");
        }

        if (landlord != null && !landlord.trim().isEmpty()) {
            sb.append("— ").append(landlord);
        }

        return sb.toString();
    }

    public static void sendWhatsAppReminder(Context context, String phoneNumber, String message) {
        try {
            String cleanPhone = phoneNumber != null ? phoneNumber.replaceAll("[^0-9+]", "") : "";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone=" + cleanPhone + "&text=" + URLEncoder.encode(message, "UTF-8");
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        } catch (Exception e) {
            sendSmsReminder(context, phoneNumber, message);
        }
    }

    public static void sendSmsReminder(Context context, String phoneNumber, String message) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("smsto:" + (phoneNumber != null ? phoneNumber : "")));
            intent.putExtra("sms_body", message);
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }
}
