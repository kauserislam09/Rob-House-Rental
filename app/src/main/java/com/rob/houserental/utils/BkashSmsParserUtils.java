package com.rob.houserental.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BkashSmsParserUtils {

    public static class BkashSmsResult {
        public String transactionId;
        public double amount;
        public String accountNumber;
        public String paymentDate;
        public boolean isValid;

        @Override
        public String toString() {
            return "TrxID: " + transactionId + ", Amount: ৳" + amount + ", Acc: " + accountNumber + ", Date: " + paymentDate;
        }
    }

    private static final Pattern TRX_PATTERN = Pattern.compile("TrxID\\s*:?\\s*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern BDT_AMOUNT_PATTERN = Pattern.compile("(?:BDT|Tk|৳)\\s*:?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ACC_PATTERN = Pattern.compile("(?:Acc|Account|Meter|No|Ref)\\s*:?\\s*([A-Za-z0-9_-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("at\\s+([0-9]{1,2}/[0-9]{1,2}/[0-9]{2,4}(?:\\s+[0-9]{1,2}:[0-9]{2})?)", Pattern.CASE_INSENSITIVE);

    public static BkashSmsResult parseBkashSms(String smsContent) {
        BkashSmsResult result = new BkashSmsResult();
        if (smsContent == null || smsContent.trim().isEmpty()) {
            result.isValid = false;
            return result;
        }

        String cleaned = smsContent.trim();

        // 1. Extract TrxID
        Matcher trxMatcher = TRX_PATTERN.matcher(cleaned);
        if (trxMatcher.find()) {
            result.transactionId = trxMatcher.group(1);
        }

        // 2. Extract Amount
        Matcher amountMatcher = BDT_AMOUNT_PATTERN.matcher(cleaned);
        if (amountMatcher.find()) {
            String rawAmt = amountMatcher.group(1).replace(",", "");
            try {
                result.amount = Double.parseDouble(rawAmt);
            } catch (NumberFormatException ignored) {
            }
        }

        // 3. Extract Account / Meter Number
        Matcher accMatcher = ACC_PATTERN.matcher(cleaned);
        if (accMatcher.find()) {
            result.accountNumber = accMatcher.group(1);
        }

        // 4. Extract Date
        Matcher dateMatcher = DATE_PATTERN.matcher(cleaned);
        if (dateMatcher.find()) {
            result.paymentDate = dateMatcher.group(1);
        }

        result.isValid = (result.transactionId != null && !result.transactionId.isEmpty()) || (result.amount > 0);
        return result;
    }
}
