package com.rob.houserental.utils;

import android.content.Context;

import com.rob.houserental.R;

/**
 * Centralised payment method utilities for display and selection across the app.
 *
 * Stored codes in the database are free-text strings (the display label at time of entry).
 * This class normalises legacy values and provides canonical display labels.
 *
 * New method codes (stored going forward):
 * CASH, BANK_TRANSFER, BKASH, NAGAD, ROCKET, CARD, OTHER
 *
 * Legacy stored values handled:
 * "Mobile Banking (bKash/Nagad/Rocket)" displayed as "Mobile Banking"
 * "MOBILE_BANKING" displayed as "Mobile Banking"
 * Any value containing BKASH/NAGAD/ROCKET/MOBILE displayed as "Mobile Banking"
 */
public class PaymentMethodUtils {

    // Internal storage codes (written to DB for NEW payments)
    public static final String CODE_CASH = "CASH";
    public static final String CODE_BANK_TRANSFER = "BANK_TRANSFER";
    public static final String CODE_BKASH = "BKASH";
    public static final String CODE_NAGAD = "NAGAD";
    public static final String CODE_ROCKET = "ROCKET";
    public static final String CODE_CARD = "CARD";
    public static final String CODE_OTHER = "OTHER";

    /**
     * Returns the ordered list of storage codes for the 7 payment methods.
     * Parallel to getDisplayMethods().
     */
    public static String[] getMethodCodes() {
        return new String[]{
                CODE_CASH,
                CODE_BANK_TRANSFER,
                CODE_BKASH,
                CODE_NAGAD,
                CODE_ROCKET,
                CODE_CARD,
                CODE_OTHER
        };
    }

    /**
     * Returns localised display labels for the 7 payment methods, in the same order
     * as getMethodCodes().
     */
    public static String[] getDisplayMethods(Context context) {
        return new String[]{
                context.getString(R.string.payment_method_cash),
                context.getString(R.string.payment_method_bank),
                context.getString(R.string.payment_method_bkash),
                context.getString(R.string.payment_method_nagad),
                context.getString(R.string.payment_method_rocket),
                context.getString(R.string.payment_method_card),
                context.getString(R.string.payment_method_other)
        };
    }

    /**
     * Returns a localised display name for any stored payment method value.
     * Handles both new codes and legacy free-text strings stored before this utility existed.
     *
     * @param context Android context for string resource access
     * @param storedValue The raw value retrieved from the database
     * @return Localised display string; falls back to storedValue if unrecognised
     */
    public static String getDisplayName(Context context, String storedValue) {
        if (storedValue == null || storedValue.trim().isEmpty()) {
            return context.getString(R.string.payment_method_other);
        }

        String upper = storedValue.trim().toUpperCase();

        // Exact new codes
        if (upper.equals(CODE_CASH)) return context.getString(R.string.payment_method_cash);
        if (upper.equals(CODE_BANK_TRANSFER)) return context.getString(R.string.payment_method_bank);
        if (upper.equals(CODE_BKASH)) return context.getString(R.string.payment_method_bkash);
        if (upper.equals(CODE_NAGAD)) return context.getString(R.string.payment_method_nagad);
        if (upper.equals(CODE_ROCKET)) return context.getString(R.string.payment_method_rocket);
        if (upper.equals(CODE_CARD)) return context.getString(R.string.payment_method_card);
        if (upper.equals(CODE_OTHER)) return context.getString(R.string.payment_method_other);

        // Legacy pattern matching
        if (upper.contains("CASH")) return context.getString(R.string.payment_method_cash);
        if (upper.contains("BKASH")) return context.getString(R.string.payment_method_bkash);
        if (upper.contains("NAGAD")) return context.getString(R.string.payment_method_nagad);
        if (upper.contains("ROCKET")) return context.getString(R.string.payment_method_rocket);
        // "Mobile Banking (bKash/Nagad/Rocket)" and "MOBILE_BANKING" generic Mobile Banking
        if (upper.contains("MOBILE")) return context.getString(R.string.payment_method_mobile);
        if (upper.contains("BANK")) return context.getString(R.string.payment_method_bank);
        if (upper.contains("CARD")) return context.getString(R.string.payment_method_card);
        if (upper.contains("CHEQUE")) return context.getString(R.string.payment_method_cheque);

        // Unknown legacy value – return as-is
        return storedValue;
    }

    /**
     * Returns the index of the given stored code in getMethodCodes(), or 0 (Cash) if not found.
     * Useful for pre-selecting the dropdown item.
     */
    public static int getIndexForCode(String storedValue) {
        if (storedValue == null) return 0;
        String upper = storedValue.trim().toUpperCase();
        String[] codes = getMethodCodes();
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(upper)) return i;
        }
        // Legacy fallback
        if (upper.contains("BANK")) return 1; // BANK_TRANSFER
        if (upper.contains("BKASH")) return 2;
        if (upper.contains("NAGAD")) return 3;
        if (upper.contains("ROCKET")) return 4;
        if (upper.contains("CARD")) return 5;
        return 0; // Default to Cash
    }
}
