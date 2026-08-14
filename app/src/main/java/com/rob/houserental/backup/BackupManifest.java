package com.rob.houserental.backup;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BackupManifest {

    public static final int CURRENT_BACKUP_FORMAT_VERSION = 1;
    public static final int CURRENT_DATABASE_VERSION = 10;

    private int backupFormatVersion = CURRENT_BACKUP_FORMAT_VERSION;
    private String appVersion = "1.0";
    private int databaseVersion = CURRENT_DATABASE_VERSION;
    private long createdAt;
    private String backupId;
    private String databaseChecksum;
    private int documentCount;
    private long backupSizeBytes;
    private String deviceModel;
    private int androidVersion;

    // Entity Counts from verified snapshot
    private int propertyCount;
    private int unitCount;
    private int tenantCount;
    private int tenancyCount;
    private int rentRecordCount;
    private int paymentCount;
    private int utilityBillCount;
    private int billPaymentCount;
    private int expenseCount;
    private int appDocumentCount;
    private int tenantDocumentCount;

    // Property Names list for preview & verification
    private List<String> propertyNames = new ArrayList<>();

    public BackupManifest() {
        this.createdAt = System.currentTimeMillis();
        this.deviceModel = Build.MANUFACTURER + " " + Build.MODEL;
        this.androidVersion = Build.VERSION.SDK_INT;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("backupFormatVersion", backupFormatVersion);
        json.put("appVersion", appVersion);
        json.put("databaseVersion", databaseVersion);
        json.put("createdAt", createdAt);
        json.put("backupId", backupId != null ? backupId : "");
        json.put("databaseChecksum", databaseChecksum != null ? databaseChecksum : "");
        json.put("documentCount", documentCount);
        json.put("backupSizeBytes", backupSizeBytes);
        json.put("deviceModel", deviceModel != null ? deviceModel : "");
        json.put("androidVersion", androidVersion);

        json.put("propertyCount", propertyCount);
        json.put("unitCount", unitCount);
        json.put("tenantCount", tenantCount);
        json.put("tenancyCount", tenancyCount);
        json.put("rentRecordCount", rentRecordCount);
        json.put("paymentCount", paymentCount);
        json.put("utilityBillCount", utilityBillCount);
        json.put("billPaymentCount", billPaymentCount);
        json.put("expenseCount", expenseCount);
        json.put("appDocumentCount", appDocumentCount);
        json.put("tenantDocumentCount", tenantDocumentCount);

        JSONArray propsArray = new JSONArray();
        if (propertyNames != null) {
            for (String p : propertyNames) {
                propsArray.put(p);
            }
        }
        json.put("propertyNames", propsArray);

        return json;
    }

    public String toJson() throws JSONException {
        return toJsonObject().toString(2);
    }

    public static BackupManifest fromJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
        BackupManifest manifest = new BackupManifest();
        manifest.setBackupFormatVersion(json.optInt("backupFormatVersion", 1));
        manifest.setAppVersion(json.optString("appVersion", "1.0"));
        manifest.setDatabaseVersion(json.optInt("databaseVersion", 10));
        manifest.setCreatedAt(json.optLong("createdAt", System.currentTimeMillis()));
        manifest.setBackupId(json.optString("backupId", ""));
        manifest.setDatabaseChecksum(json.optString("databaseChecksum", ""));
        manifest.setDocumentCount(json.optInt("documentCount", 0));
        manifest.setBackupSizeBytes(json.optLong("backupSizeBytes", 0));
        manifest.setDeviceModel(json.optString("deviceModel", ""));
        manifest.setAndroidVersion(json.optInt("androidVersion", 0));

        manifest.setPropertyCount(json.optInt("propertyCount", 0));
        manifest.setUnitCount(json.optInt("unitCount", 0));
        manifest.setTenantCount(json.optInt("tenantCount", 0));
        manifest.setTenancyCount(json.optInt("tenancyCount", 0));
        manifest.setRentRecordCount(json.optInt("rentRecordCount", 0));
        manifest.setPaymentCount(json.optInt("paymentCount", 0));
        manifest.setUtilityBillCount(json.optInt("utilityBillCount", 0));
        manifest.setBillPaymentCount(json.optInt("billPaymentCount", 0));
        manifest.setExpenseCount(json.optInt("expenseCount", 0));
        manifest.setAppDocumentCount(json.optInt("appDocumentCount", 0));
        manifest.setTenantDocumentCount(json.optInt("tenantDocumentCount", 0));

        JSONArray propsArray = json.optJSONArray("propertyNames");
        List<String> names = new ArrayList<>();
        if (propsArray != null) {
            for (int i = 0; i < propsArray.length(); i++) {
                names.add(propsArray.optString(i));
            }
        }
        manifest.setPropertyNames(names);

        return manifest;
    }

    public int getBackupFormatVersion() {
        return backupFormatVersion;
    }

    public void setBackupFormatVersion(int backupFormatVersion) {
        this.backupFormatVersion = backupFormatVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public int getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(int databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getBackupId() {
        return backupId;
    }

    public void setBackupId(String backupId) {
        this.backupId = backupId;
    }

    public String getDatabaseChecksum() {
        return databaseChecksum;
    }

    public void setDatabaseChecksum(String databaseChecksum) {
        this.databaseChecksum = databaseChecksum;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
    }

    public long getBackupSizeBytes() {
        return backupSizeBytes;
    }

    public void setBackupSizeBytes(long backupSizeBytes) {
        this.backupSizeBytes = backupSizeBytes;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public int getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(int androidVersion) {
        this.androidVersion = androidVersion;
    }

    public int getPropertyCount() {
        return propertyCount;
    }

    public void setPropertyCount(int propertyCount) {
        this.propertyCount = propertyCount;
    }

    public int getUnitCount() {
        return unitCount;
    }

    public void setUnitCount(int unitCount) {
        this.unitCount = unitCount;
    }

    public int getTenantCount() {
        return tenantCount;
    }

    public void setTenantCount(int tenantCount) {
        this.tenantCount = tenantCount;
    }

    public int getTenancyCount() {
        return tenancyCount;
    }

    public void setTenancyCount(int tenancyCount) {
        this.tenancyCount = tenancyCount;
    }

    public int getRentRecordCount() {
        return rentRecordCount;
    }

    public void setRentRecordCount(int rentRecordCount) {
        this.rentRecordCount = rentRecordCount;
    }

    public int getPaymentCount() {
        return paymentCount;
    }

    public void setPaymentCount(int paymentCount) {
        this.paymentCount = paymentCount;
    }

    public int getUtilityBillCount() {
        return utilityBillCount;
    }

    public void setUtilityBillCount(int utilityBillCount) {
        this.utilityBillCount = utilityBillCount;
    }

    public int getBillPaymentCount() {
        return billPaymentCount;
    }

    public void setBillPaymentCount(int billPaymentCount) {
        this.billPaymentCount = billPaymentCount;
    }

    public int getExpenseCount() {
        return expenseCount;
    }

    public void setExpenseCount(int expenseCount) {
        this.expenseCount = expenseCount;
    }

    public int getAppDocumentCount() {
        return appDocumentCount;
    }

    public void setAppDocumentCount(int appDocumentCount) {
        this.appDocumentCount = appDocumentCount;
    }

    public int getTenantDocumentCount() {
        return tenantDocumentCount;
    }

    public void setTenantDocumentCount(int tenantDocumentCount) {
        this.tenantDocumentCount = tenantDocumentCount;
    }

    public List<String> getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(List<String> propertyNames) {
        this.propertyNames = propertyNames != null ? propertyNames : new ArrayList<>();
    }

    public String getPropertySummary() {
        if (propertyNames == null || propertyNames.isEmpty()) {
            return propertyCount > 0 ? propertyCount + " Properties" : "0 Properties";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(propertyNames.size(), 3); i++) {
            if (i > 0) sb.append(", ");
            sb.append(propertyNames.get(i));
        }
        if (propertyNames.size() > 3) {
            sb.append(" (+").append(propertyNames.size() - 3).append(" more)");
        }
        return sb.toString();
    }
}
