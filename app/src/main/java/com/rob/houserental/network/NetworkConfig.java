package com.rob.houserental.network;

public class NetworkConfig {

    public enum Environment {
        DEVELOPMENT,
        STAGING,
        PRODUCTION
    }

    private static Environment activeEnvironment = Environment.DEVELOPMENT;

    public static Environment getActiveEnvironment() {
        return activeEnvironment;
    }

    public static void setActiveEnvironment(Environment env) {
        if (env != null) {
            activeEnvironment = env;
        }
    }

    public static String getBaseUrl() {
        switch (activeEnvironment) {
            case STAGING:
                return "https://staging-api.robhouserental.com/v1/";
            case PRODUCTION:
                return "https://api.robhouserental.com/v1/";
            case DEVELOPMENT:
            default:
                return "https://dev-api.robhouserental.com/v1/";
        }
    }

    public static boolean isSecureHttps(String url) {
        return url != null && url.toLowerCase().startsWith("https://");
    }
}
