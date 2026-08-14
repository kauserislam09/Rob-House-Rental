package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.RentRecord;
import com.rob.houserental.model.RentRecordDisplayItem;

import java.util.List;

@Dao
public interface RentDao {

    @Insert
    long insert(RentRecord rentRecord);

    @Update
    void update(RentRecord rentRecord);

    @Delete
    void delete(RentRecord rentRecord);

    @Query("SELECT * FROM rent_records WHERE id = :id LIMIT 1")
    RentRecord getRentRecordById(long id);

    @Query("SELECT * FROM rent_records WHERE tenancyId = :tenancyId AND billingMonth = :billingMonth LIMIT 1")
    RentRecord getRentRecordByTenancyAndMonth(long tenancyId, String billingMonth);

    @Query("SELECT * FROM rent_records WHERE tenancyId = :tenancyId ORDER BY id DESC")
    List<RentRecord> getRentRecordsByTenancy(long tenancyId);

    @Query("SELECT * FROM rent_records ORDER BY id DESC")
    List<RentRecord> getAllRentRecords();

    @Query("SELECT * FROM rent_records WHERE status = :status ORDER BY id DESC")
    List<RentRecord> getRentRecordsByStatus(String status);

    @Query("SELECT * FROM rent_records WHERE billingMonth = :billingMonth ORDER BY id DESC")
    List<RentRecord> getRentRecordsByMonth(String billingMonth);

    @Query("SELECT COALESCE(SUM(amountDue), 0) FROM rent_records WHERE billingMonth = :billingMonth")
    double getTotalExpectedRentByMonth(String billingMonth);

    @Query("SELECT COALESCE(SUM(amountDue), 0) FROM rent_records WHERE billingMonth >= :startMonth AND billingMonth <= :endMonth")
    double getTotalExpectedRentByMonthRange(String startMonth, String endMonth);

    @Query("SELECT COALESCE(SUM(r.amountDue), 0) FROM rent_records r " +
            "JOIN tenancies ty ON r.tenancyId = ty.id " +
            "JOIN units u ON ty.unitId = u.id " +
            "WHERE u.propertyId = :propertyId AND r.billingMonth >= :startMonth AND r.billingMonth <= :endMonth")
    double getPropertyExpectedRentByMonthRange(long propertyId, String startMonth, String endMonth);

    @Query("SELECT COALESCE(SUM(r.amountDue), 0) FROM rent_records r " +
            "JOIN tenancies ty ON r.tenancyId = ty.id " +
            "JOIN units u ON ty.unitId = u.id " +
            "WHERE u.propertyId = :propertyId")
    double getPropertyExpectedRentAllTime(long propertyId);

    @Query("SELECT COALESCE(SUM(r.remainingAmount), 0) FROM rent_records r " +
            "JOIN tenancies ty ON r.tenancyId = ty.id " +
            "JOIN units u ON ty.unitId = u.id " +
            "WHERE u.propertyId = :propertyId AND r.billingMonth <= :maxBillingMonth AND r.status NOT IN ('PAID', 'WAIVED') AND r.remainingAmount > 0")
    double getPropertyCumulativeOutstandingRent(long propertyId, String maxBillingMonth);

    @Query("SELECT COALESCE(SUM(r.remainingAmount), 0) FROM rent_records r " +
            "JOIN tenancies ty ON r.tenancyId = ty.id " +
            "WHERE ty.unitId = :unitId AND r.billingMonth <= :maxBillingMonth AND r.status NOT IN ('PAID', 'WAIVED') AND r.remainingAmount > 0")
    double getUnitCumulativeOutstandingRent(long unitId, String maxBillingMonth);

    @Query("SELECT COALESCE(SUM(amountPaid), 0) FROM rent_records WHERE billingMonth = :billingMonth")
    double getTotalCollectedRentByMonth(String billingMonth);

    @Query("SELECT COALESCE(SUM(remainingAmount), 0) FROM rent_records WHERE billingMonth = :billingMonth")
    double getTotalOutstandingRentByMonth(String billingMonth);

    @Query("SELECT COALESCE(SUM(remainingAmount), 0) FROM rent_records WHERE billingMonth = :billingMonth AND status = 'OVERDUE'")
    double getTotalOverdueRentByMonth(String billingMonth);

    @Query("SELECT COALESCE(SUM(amountDue), 0) FROM rent_records")
    double getTotalExpectedRentAllTime();

    @Query("SELECT COALESCE(SUM(amountPaid), 0) FROM rent_records")
    double getTotalCollectedRentAllTime();

    @Query("SELECT COALESCE(SUM(remainingAmount), 0) FROM rent_records")
    double getTotalOutstandingRentAllTime();

    @Query("SELECT COALESCE(SUM(remainingAmount), 0) FROM rent_records WHERE status = 'OVERDUE'")
    double getTotalOverdueRentAllTime();

    @Query("SELECT " +
            "r.id AS id, " +
            "r.tenancyId AS tenancyId, " +
            "r.billingMonth AS billingMonth, " +
            "r.dueDate AS dueDate, " +
            "r.amountDue AS amountDue, " +
            "r.amountPaid AS amountPaid, " +
            "r.remainingAmount AS remainingAmount, " +
            "r.status AS status, " +
            "r.lastPaymentDate AS lastPaymentDate, " +
            "r.paymentMethod AS paymentMethod, " +
            "r.notes AS notes, " +
            "t.fullName AS tenantName, " +
            "t.phoneNumber AS tenantPhone, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor, " +
            "p.name AS propertyName, " +
            "p.id AS propertyId " +
            "FROM rent_records r " +
            "JOIN tenancies ty ON r.tenancyId = ty.id " +
            "JOIN tenants t ON ty.tenantId = t.id " +
            "JOIN units u ON ty.unitId = u.id " +
            "JOIN properties p ON u.propertyId = p.id " +
            "ORDER BY r.id DESC")
    List<RentRecordDisplayItem> getAllRentDisplayItems();

    @Query("SELECT " +
            "r.id AS id, " +
            "r.tenancyId AS tenancyId, " +
            "r.billingMonth AS billingMonth, " +
            "r.dueDate AS dueDate, " +
            "r.amountDue AS amountDue, " +
            "r.amountPaid AS amountPaid, " +
            "r.remainingAmount AS remainingAmount, " +
            "r.status AS status, " +
            "r.lastPaymentDate AS lastPaymentDate, " +
            "r.paymentMethod AS paymentMethod, " +
            "r.notes AS notes, " +
            "t.fullName AS tenantName, " +
            "t.phoneNumber AS tenantPhone, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor, " +
            "p.name AS propertyName, " +
            "p.id AS propertyId " +
            "FROM rent_records r " +
            "JOIN tenancies ty ON r.tenancyId = ty.id " +
            "JOIN tenants t ON ty.tenantId = t.id " +
            "JOIN units u ON ty.unitId = u.id " +
            "JOIN properties p ON u.propertyId = p.id " +
            "WHERE r.billingMonth = :billingMonth " +
            "ORDER BY r.id DESC")
    List<RentRecordDisplayItem> getRentDisplayItemsByMonth(String billingMonth);

    @Query("SELECT " +
            "r.id AS id, " +
            "r.tenancyId AS tenancyId, " +
            "r.billingMonth AS billingMonth, " +
            "r.dueDate AS dueDate, " +
            "r.amountDue AS amountDue, " +
            "r.amountPaid AS amountPaid, " +
            "r.remainingAmount AS remainingAmount, " +
            "r.status AS status, " +
            "r.lastPaymentDate AS lastPaymentDate, " +
            "r.paymentMethod AS paymentMethod, " +
            "r.notes AS notes, " +
            "t.fullName AS tenantName, " +
            "t.phoneNumber AS tenantPhone, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor, " +
            "p.name AS propertyName, " +
            "p.id AS propertyId " +
            "FROM rent_records r " +
            "JOIN tenancies ty ON r.tenancyId = ty.id " +
            "JOIN tenants t ON ty.tenantId = t.id " +
            "JOIN units u ON ty.unitId = u.id " +
            "JOIN properties p ON u.propertyId = p.id " +
            "WHERE r.id = :id LIMIT 1")
    RentRecordDisplayItem getRentDisplayItemById(long id);

    @Query("SELECT " +
            "r.id AS id, " +
            "r.tenancyId AS tenancyId, " +
            "r.billingMonth AS billingMonth, " +
            "r.dueDate AS dueDate, " +
            "r.amountDue AS amountDue, " +
            "r.amountPaid AS amountPaid, " +
            "r.remainingAmount AS remainingAmount, " +
            "r.status AS status, " +
            "r.lastPaymentDate AS lastPaymentDate, " +
            "r.paymentMethod AS paymentMethod, " +
            "r.notes AS notes, " +
            "t.fullName AS tenantName, " +
            "t.phoneNumber AS tenantPhone, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor, " +
            "p.name AS propertyName, " +
            "p.id AS propertyId " +
            "FROM rent_records r " +
            "JOIN tenancies ty ON r.tenancyId = ty.id " +
            "JOIN tenants t ON ty.tenantId = t.id " +
            "JOIN units u ON ty.unitId = u.id " +
            "JOIN properties p ON u.propertyId = p.id " +
            "WHERE r.tenancyId = :tenancyId " +
            "ORDER BY r.id DESC")
    List<RentRecordDisplayItem> getRentDisplayItemsByTenancy(long tenancyId);

    @Query("SELECT COALESCE(SUM(remainingAmount), 0) FROM rent_records WHERE billingMonth <= :maxBillingMonth AND status NOT IN ('PAID', 'WAIVED') AND remainingAmount > 0")
    double getTotalCumulativeOutstandingRent(String maxBillingMonth);

    @Query("SELECT COALESCE(SUM(remainingAmount), 0) FROM rent_records WHERE tenancyId = :tenancyId AND billingMonth <= :maxBillingMonth AND status NOT IN ('PAID', 'WAIVED') AND remainingAmount > 0")
    double getTenancyCumulativeOutstandingRent(long tenancyId, String maxBillingMonth);

    @Query("SELECT " +
            "r.id AS id, " +
            "r.tenancyId AS tenancyId, " +
            "r.billingMonth AS billingMonth, " +
            "r.dueDate AS dueDate, " +
            "r.amountDue AS amountDue, " +
            "r.amountPaid AS amountPaid, " +
            "r.remainingAmount AS remainingAmount, " +
            "r.status AS status, " +
            "r.lastPaymentDate AS lastPaymentDate, " +
            "r.paymentMethod AS paymentMethod, " +
            "r.notes AS notes, " +
            "t.fullName AS tenantName, " +
            "t.phoneNumber AS tenantPhone, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor, " +
            "p.name AS propertyName, " +
            "p.id AS propertyId " +
            "FROM rent_records r " +
            "JOIN tenancies ty ON r.tenancyId = ty.id " +
            "JOIN tenants t ON ty.tenantId = t.id " +
            "JOIN units u ON ty.unitId = u.id " +
            "JOIN properties p ON u.propertyId = p.id " +
            "WHERE r.billingMonth <= :maxBillingMonth AND r.status NOT IN ('PAID', 'WAIVED') AND r.remainingAmount > 0 " +
            "ORDER BY r.billingMonth ASC, r.id ASC")
    List<RentRecordDisplayItem> getCumulativeOutstandingRentDisplayItems(String maxBillingMonth);

    @Query("SELECT " +
            "r.id AS id, " +
            "r.tenancyId AS tenancyId, " +
            "r.billingMonth AS billingMonth, " +
            "r.dueDate AS dueDate, " +
            "r.amountDue AS amountDue, " +
            "r.amountPaid AS amountPaid, " +
            "r.remainingAmount AS remainingAmount, " +
            "r.status AS status, " +
            "r.lastPaymentDate AS lastPaymentDate, " +
            "r.paymentMethod AS paymentMethod, " +
            "r.notes AS notes, " +
            "t.fullName AS tenantName, " +
            "t.phoneNumber AS tenantPhone, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor, " +
            "p.name AS propertyName, " +
            "p.id AS propertyId " +
            "FROM rent_records r " +
            "JOIN tenancies ty ON r.tenancyId = ty.id " +
            "JOIN tenants t ON ty.tenantId = t.id " +
            "JOIN units u ON ty.unitId = u.id " +
            "JOIN properties p ON u.propertyId = p.id " +
            "WHERE r.billingMonth >= :startMonth AND r.billingMonth <= :endMonth " +
            "ORDER BY r.billingMonth DESC, r.id DESC")
    List<RentRecordDisplayItem> getRentDisplayItemsByMonthRange(String startMonth, String endMonth);
}
