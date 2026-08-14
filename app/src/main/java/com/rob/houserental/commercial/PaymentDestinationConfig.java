package com.rob.houserental.commercial;

public class PaymentDestinationConfig {

    public static final String METHOD_BKASH = "BKASH";
    public static final String METHOD_NAGAD = "NAGAD";
    public static final String METHOD_ROCKET = "ROCKET";

    // Configurable MFS Send Money destination numbers
    private static String bkashNumber = "01700000000";
    private static String nagadNumber = "01800000000";
    private static String rocketNumber = "01900000000";

    public static String getDestinationNumber(String method) {
        if (method == null) return bkashNumber;
        switch (method.toUpperCase()) {
            case METHOD_NAGAD: return nagadNumber;
            case METHOD_ROCKET: return rocketNumber;
            case METHOD_BKASH:
            default: return bkashNumber;
        }
    }

    public static void setDestinationNumbers(String bkash, String nagad, String rocket) {
        if (bkash != null) bkashNumber = bkash;
        if (nagad != null) nagadNumber = nagad;
        if (rocket != null) rocketNumber = rocket;
    }
}
