package com.rob.houserental.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.rob.houserental.model.AppDocument;
import com.rob.houserental.model.BackupHistory;
import com.rob.houserental.model.BillPayment;
import com.rob.houserental.model.Expense;
import com.rob.houserental.model.MaintenanceRecord;
import com.rob.houserental.model.Payment;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.Reminder;
import com.rob.houserental.model.RentRecord;
import com.rob.houserental.model.Tenant;
import com.rob.houserental.model.Tenancy;
import com.rob.houserental.model.TenantDocument;
import com.rob.houserental.model.Unit;
import com.rob.houserental.model.UtilityBill;

import com.rob.houserental.model.PaymentOrder;
import com.rob.houserental.model.SubscriptionEntitlement;
import com.rob.houserental.model.UserAccount;

@Database(
        entities = {
                Property.class,
                Unit.class,
                Tenant.class,
                Tenancy.class,
                TenantDocument.class,
                RentRecord.class,
                Payment.class,
                UtilityBill.class,
                BillPayment.class,
                Expense.class,
                AppDocument.class,
                BackupHistory.class,
                MaintenanceRecord.class,
                Reminder.class,
                UserAccount.class,
                SubscriptionEntitlement.class,
                PaymentOrder.class
        },
        version = 13,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PropertyDao propertyDao();
    public abstract UnitDao unitDao();
    public abstract TenantDao tenantDao();
    public abstract TenantDocumentDao tenantDocumentDao();
    public abstract TenancyDao tenancyDao();
    public abstract RentDao rentDao();
    public abstract PaymentDao paymentDao();
    public abstract UtilityBillDao utilityBillDao();
    public abstract BillPaymentDao billPaymentDao();
    public abstract ExpenseDao expenseDao();
    public abstract AppDocumentDao appDocumentDao();
    public abstract BackupHistoryDao backupHistoryDao();
    public abstract MaintenanceDao maintenanceDao();
    public abstract ReminderDao reminderDao();
    public abstract UserAccountDao userAccountDao();
    public abstract SubscriptionEntitlementDao subscriptionEntitlementDao();
    public abstract PaymentOrderDao paymentOrderDao();

    private static volatile AppDatabase INSTANCE;

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add new columns to tenants table
            database.execSQL("ALTER TABLE `tenants` ADD COLUMN `status` TEXT DEFAULT 'ACTIVE'");
            database.execSQL("ALTER TABLE `tenants` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `tenants` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tenants_status` ON `tenants` (`status`)");

            // Create tenant_documents table
            database.execSQL("CREATE TABLE IF NOT EXISTS `tenant_documents` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`tenantId` INTEGER NOT NULL, " +
                    "`documentType` TEXT, " +
                    "`displayName` TEXT, " +
                    "`filePath` TEXT, " +
                    "`mimeType` TEXT, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`tenantId`) REFERENCES `tenants`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE" +
                    ")");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tenant_documents_tenantId` ON `tenant_documents` (`tenantId`)");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add createdAt and updatedAt to tenancies
            database.execSQL("ALTER TABLE `tenancies` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `tenancies` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_tenancies_status` ON `tenancies` (`status`)");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create rent_records table
            database.execSQL("CREATE TABLE IF NOT EXISTS `rent_records` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`tenancyId` INTEGER NOT NULL, " +
                    "`billingMonth` TEXT, " +
                    "`dueDate` TEXT, " +
                    "`amountDue` REAL NOT NULL, " +
                    "`amountPaid` REAL NOT NULL, " +
                    "`remainingAmount` REAL NOT NULL, " +
                    "`status` TEXT, " +
                    "`lastPaymentDate` TEXT, " +
                    "`paymentMethod` TEXT, " +
                    "`notes` TEXT, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`tenancyId`) REFERENCES `tenancies`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT" +
                    ")");

            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_rent_records_tenancyId_billingMonth` ON `rent_records` (`tenancyId`, `billingMonth`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_rent_records_tenancyId` ON `rent_records` (`tenancyId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_rent_records_billingMonth` ON `rent_records` (`billingMonth`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_rent_records_status` ON `rent_records` (`status`)");

            // Create payments table
            database.execSQL("CREATE TABLE IF NOT EXISTS `payments` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`rentRecordId` INTEGER NOT NULL, " +
                    "`amount` REAL NOT NULL, " +
                    "`paymentDate` TEXT, " +
                    "`paymentMethod` TEXT, " +
                    "`reference` TEXT, " +
                    "`notes` TEXT, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`rentRecordId`) REFERENCES `rent_records`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE" +
                    ")");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_rentRecordId` ON `payments` (`rentRecordId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_paymentDate` ON `payments` (`paymentDate`)");
        }
    };

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add rentDueDay to tenancies table safely
            database.execSQL("ALTER TABLE `tenancies` ADD COLUMN `rentDueDay` INTEGER NOT NULL DEFAULT 10");
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create utility_bills table
            database.execSQL("CREATE TABLE IF NOT EXISTS `utility_bills` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`propertyId` INTEGER NOT NULL, " +
                    "`unitId` INTEGER NOT NULL, " +
                    "`tenancyId` INTEGER NOT NULL, " +
                    "`billType` TEXT, " +
                    "`billingMonth` TEXT, " +
                    "`dueDate` TEXT, " +
                    "`amountDue` REAL NOT NULL, " +
                    "`amountPaid` REAL NOT NULL, " +
                    "`remainingAmount` REAL NOT NULL, " +
                    "`status` TEXT, " +
                    "`meterNumber` TEXT, " +
                    "`previousReading` REAL NOT NULL, " +
                    "`currentReading` REAL NOT NULL, " +
                    "`unitsConsumed` REAL NOT NULL, " +
                    "`ratePerUnit` REAL NOT NULL, " +
                    "`fixedCharge` REAL NOT NULL, " +
                    "`vatOrTax` REAL NOT NULL, " +
                    "`billNumber` TEXT, " +
                    "`lastPaymentDate` TEXT, " +
                    "`paymentMethod` TEXT, " +
                    "`notes` TEXT, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`propertyId`) REFERENCES `properties`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT" +
                    ")");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_utility_bills_propertyId` ON `utility_bills` (`propertyId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_utility_bills_unitId` ON `utility_bills` (`unitId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_utility_bills_tenancyId` ON `utility_bills` (`tenancyId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_utility_bills_billingMonth` ON `utility_bills` (`billingMonth`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_utility_bills_billType` ON `utility_bills` (`billType`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_utility_bills_status` ON `utility_bills` (`status`)");

            // Create bill_payments table
            database.execSQL("CREATE TABLE IF NOT EXISTS `bill_payments` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`billId` INTEGER NOT NULL, " +
                    "`amount` REAL NOT NULL, " +
                    "`paymentDate` TEXT, " +
                    "`paymentMethod` TEXT, " +
                    "`reference` TEXT, " +
                    "`notes` TEXT, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`billId`) REFERENCES `utility_bills`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE" +
                    ")");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_bill_payments_billId` ON `bill_payments` (`billId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_bill_payments_paymentDate` ON `bill_payments` (`paymentDate`)");
        }
    };

    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add unique index on (propertyId, unitId, billType, billingMonth) to prevent duplicate bills
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_utility_bills_propertyId_unitId_billType_billingMonth` ON `utility_bills` (`propertyId`, `unitId`, `billType`, `billingMonth`)");
        }
    };

    public static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create expenses table
            database.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`propertyId` INTEGER NOT NULL, " +
                    "`unitId` INTEGER NOT NULL, " +
                    "`category` TEXT, " +
                    "`amount` REAL NOT NULL, " +
                    "`expenseDate` TEXT, " +
                    "`expenseMonth` TEXT, " +
                    "`description` TEXT, " +
                    "`receiptPath` TEXT, " +
                    "`receiptName` TEXT, " +
                    "`receiptMimeType` TEXT, " +
                    "`notes` TEXT, " +
                    "`isArchived` INTEGER NOT NULL DEFAULT 0, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`propertyId`) REFERENCES `properties`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT" +
                    ")");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_propertyId` ON `expenses` (`propertyId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_unitId` ON `expenses` (`unitId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_category` ON `expenses` (`category`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_expenseMonth` ON `expenses` (`expenseMonth`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_expenseDate` ON `expenses` (`expenseDate`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_isArchived` ON `expenses` (`isArchived`)");
        }
    };

    public static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create app_documents table
            database.execSQL("CREATE TABLE IF NOT EXISTS `app_documents` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`documentType` TEXT, " +
                    "`category` TEXT, " +
                    "`displayName` TEXT, " +
                    "`fileName` TEXT, " +
                    "`filePath` TEXT, " +
                    "`mimeType` TEXT, " +
                    "`fileSize` INTEGER NOT NULL DEFAULT 0, " +
                    "`propertyId` INTEGER NOT NULL DEFAULT 0, " +
                    "`unitId` INTEGER NOT NULL DEFAULT 0, " +
                    "`tenantId` INTEGER NOT NULL DEFAULT 0, " +
                    "`relatedRecordId` INTEGER NOT NULL DEFAULT 0, " +
                    "`notes` TEXT, " +
                    "`isArchived` INTEGER NOT NULL DEFAULT 0, " +
                    "`createdAt` INTEGER NOT NULL DEFAULT 0, " +
                    "`updatedAt` INTEGER NOT NULL DEFAULT 0" +
                    ")");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_app_documents_documentType` ON `app_documents` (`documentType`);");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_app_documents_category` ON `app_documents` (`category`);");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_app_documents_propertyId` ON `app_documents` (`propertyId`);");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_app_documents_tenantId` ON `app_documents` (`tenantId`);");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_app_documents_isArchived` ON `app_documents` (`isArchived`);");
        }
    };

    public static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create backup_history table
            database.execSQL("CREATE TABLE IF NOT EXISTS `backup_history` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`backupId` TEXT, " +
                    "`fileName` TEXT, " +
                    "`driveFileId` TEXT, " +
                    "`createdAt` INTEGER NOT NULL DEFAULT 0, " +
                    "`completedAt` INTEGER NOT NULL DEFAULT 0, " +
                    "`sizeBytes` INTEGER NOT NULL DEFAULT 0, " +
                    "`status` TEXT, " +
                    "`backupType` TEXT, " +
                    "`errorMessage` TEXT, " +
                    "`checksum` TEXT, " +
                    "`appVersion` TEXT, " +
                    "`documentCount` INTEGER NOT NULL DEFAULT 0" +
                    ")");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_backup_history_createdAt` ON `backup_history` (`createdAt`);");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_backup_history_status` ON `backup_history` (`status`);");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_backup_history_backupType` ON `backup_history` (`backupType`);");
        }
    };

    public static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Deduplicate existing duplicate unitNumbers within the same property before creating unique index
            android.database.Cursor cursor = database.query(
                    "SELECT id, propertyId, unitNumber FROM units ORDER BY propertyId ASC, LOWER(TRIM(unitNumber)) ASC, id ASC"
            );
            if (cursor != null) {
                java.util.Map<String, Integer> seenCount = new java.util.HashMap<>();
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    long propertyId = cursor.getLong(1);
                    String rawUnitNumber = cursor.getString(2);
                    String normUnitNumber = rawUnitNumber != null ? rawUnitNumber.trim().toLowerCase() : "";
                    String key = propertyId + "_" + normUnitNumber;

                    int count = seenCount.getOrDefault(key, 0);
                    if (count > 0) {
                        String newUnitNumber = (rawUnitNumber != null ? rawUnitNumber.trim() : "") + " (" + (count + 1) + ")";
                        android.content.ContentValues cv = new android.content.ContentValues();
                        cv.put("unitNumber", newUnitNumber);
                        database.update("units", android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, cv, "id = ?", new Object[]{id});
                    }
                    seenCount.put(key, count + 1);
                }
                cursor.close();
            }

            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_units_propertyId_unitNumber` ON `units` (`propertyId`, `unitNumber`)");
        }
    };

    public static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create maintenance_records table
            database.execSQL("CREATE TABLE IF NOT EXISTS `maintenance_records` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`propertyId` INTEGER NOT NULL, " +
                    "`unitId` INTEGER, " + // Nullable Long: NULL = Property-wide maintenance
                    "`title` TEXT, " +
                    "`description` TEXT, " +
                    "`category` TEXT, " +
                    "`priority` TEXT, " +
                    "`status` TEXT, " +
                    "`estimatedCost` REAL NOT NULL DEFAULT 0.0, " +
                    "`actualCost` REAL NOT NULL DEFAULT 0.0, " +
                    "`expenseId` INTEGER, " + // Nullable Long: ID of converted expense
                    "`vendorName` TEXT, " +
                    "`vendorPhone` TEXT, " +
                    "`scheduledDate` TEXT, " +
                    "`completedDate` TEXT, " +
                    "`notes` TEXT, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`propertyId`) REFERENCES `properties`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT" +
                    ")");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_maintenance_records_propertyId` ON `maintenance_records` (`propertyId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_maintenance_records_unitId` ON `maintenance_records` (`unitId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_maintenance_records_category` ON `maintenance_records` (`category`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_maintenance_records_priority` ON `maintenance_records` (`priority`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_maintenance_records_status` ON `maintenance_records` (`status`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_maintenance_records_scheduledDate` ON `maintenance_records` (`scheduledDate`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_maintenance_records_completedDate` ON `maintenance_records` (`completedDate`)");

            // Create reminders table
            database.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`title` TEXT, " +
                    "`description` TEXT, " +
                    "`reminderType` TEXT, " +
                    "`relatedEntityType` TEXT, " +
                    "`relatedEntityId` INTEGER NOT NULL DEFAULT 0, " +
                    "`reminderDate` TEXT, " +
                    "`reminderTime` TEXT, " +
                    "`repeatType` TEXT, " +
                    "`repeatInterval` INTEGER NOT NULL DEFAULT 1, " +
                    "`isEnabled` INTEGER NOT NULL DEFAULT 1, " +
                    "`isCompleted` INTEGER NOT NULL DEFAULT 0, " +
                    "`lastTriggeredAt` INTEGER NOT NULL DEFAULT 0, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL" +
                    ")");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_reminderType` ON `reminders` (`reminderType`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_relatedEntityType` ON `reminders` (`relatedEntityType`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_relatedEntityId` ON `reminders` (`relatedEntityId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_reminderDate` ON `reminders` (`reminderDate`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_isEnabled` ON `reminders` (`isEnabled`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_isCompleted` ON `reminders` (`isCompleted`)");
        }
    };

    public static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `user_accounts` (" +
                    "`userId` TEXT PRIMARY KEY NOT NULL, " +
                    "`emailOrPhone` TEXT, " +
                    "`fullName` TEXT, " +
                    "`status` TEXT, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL" +
                    ")");

            database.execSQL("CREATE TABLE IF NOT EXISTS `subscription_entitlements` (" +
                    "`entitlementId` TEXT PRIMARY KEY NOT NULL, " +
                    "`userId` TEXT, " +
                    "`entitlementType` TEXT, " +
                    "`status` TEXT, " +
                    "`planCode` TEXT, " +
                    "`startedAt` INTEGER NOT NULL, " +
                    "`expiresAt` INTEGER NOT NULL, " +
                    "`graceUntil` INTEGER NOT NULL, " +
                    "`source` TEXT, " +
                    "`orderId` TEXT, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL" +
                    ")");

            database.execSQL("CREATE TABLE IF NOT EXISTS `payment_orders` (" +
                    "`orderId` TEXT PRIMARY KEY NOT NULL, " +
                    "`userId` TEXT, " +
                    "`planCode` TEXT, " +
                    "`amountMinor` INTEGER NOT NULL, " +
                    "`currency` TEXT, " +
                    "`paymentMethod` TEXT, " +
                    "`status` TEXT, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`expiresAt` INTEGER NOT NULL, " +
                    "`submittedAt` INTEGER NOT NULL, " +
                    "`verifiedAt` INTEGER NOT NULL, " +
                    "`rejectedAt` INTEGER NOT NULL, " +
                    "`transactionId` TEXT, " +
                    "`rejectionReason` TEXT, " +
                    "`verifiedBy` TEXT, " +
                    "`entitlementId` TEXT" +
                    ")");
        }
    };

    public static AppDatabase getInstance(Context context) {

        if (INSTANCE == null) {

            synchronized (AppDatabase.class) {

                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "rob_house_rental.db"
                            )
                            .addMigrations(
                                    MIGRATION_1_2,
                                    MIGRATION_2_3,
                                    MIGRATION_3_4,
                                    MIGRATION_4_5,
                                    MIGRATION_5_6,
                                    MIGRATION_6_7,
                                    MIGRATION_7_8,
                                    MIGRATION_8_9,
                                    MIGRATION_9_10,
                                    MIGRATION_10_11,
                                    MIGRATION_11_12,
                                    MIGRATION_12_13
                            )
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    public static synchronized void closeDatabase() {
        if (INSTANCE != null) {
            if (INSTANCE.isOpen()) {
                INSTANCE.close();
            }
            INSTANCE = null;
        }
    }
}