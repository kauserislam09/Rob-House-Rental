package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.UtilityBill;
import com.rob.houserental.model.UtilityBillDisplayItem;

import java.util.List;

@Dao
public interface UtilityBillDao {

    @Insert
    long insert(UtilityBill bill);

    @Update
    void update(UtilityBill bill);

    @Delete
    void delete(UtilityBill bill);

    @Query("SELECT * FROM utility_bills WHERE id = :id LIMIT 1")
    UtilityBill getBillById(long id);

    @Query("SELECT * FROM utility_bills WHERE propertyId = :propertyId ORDER BY id DESC")
    List<UtilityBill> getBillsByProperty(long propertyId);

    @Query("SELECT * FROM utility_bills WHERE unitId = :unitId ORDER BY id DESC")
    List<UtilityBill> getBillsByUnit(long unitId);

    @Query("SELECT * FROM utility_bills WHERE billingMonth = :billingMonth ORDER BY id DESC")
    List<UtilityBill> getBillsByMonth(String billingMonth);

    @Query("SELECT * FROM utility_bills WHERE propertyId = :propertyId AND unitId = :unitId AND billType = :billType AND billingMonth = :billingMonth LIMIT 1")
    UtilityBill getDuplicateBill(long propertyId, long unitId, String billType, String billingMonth);

    @Query("SELECT * FROM utility_bills WHERE billType = :billType ORDER BY id DESC")
    List<UtilityBill> getBillsByType(String billType);

    @Query("SELECT * FROM utility_bills WHERE status = :status ORDER BY id DESC")
    List<UtilityBill> getBillsByStatus(String status);

    @Query("SELECT * FROM utility_bills ORDER BY id DESC")
    List<UtilityBill> getAllBills();

    @Query("SELECT COALESCE(SUM(amountDue), 0) FROM utility_bills WHERE billingMonth = :billingMonth")
    double getTotalExpectedBillsByMonth(String billingMonth);

    @Query("SELECT COALESCE(SUM(amountPaid), 0) FROM utility_bills WHERE billingMonth = :billingMonth")
    double getTotalCollectedBillsByMonth(String billingMonth);

    @Query("SELECT COALESCE(SUM(amountPaid), 0) FROM utility_bills WHERE billingMonth >= :startMonth AND billingMonth <= :endMonth")
    double getTotalCollectedBillsByMonthRange(String startMonth, String endMonth);

    @Query("SELECT COALESCE(SUM(amountPaid), 0) FROM utility_bills WHERE propertyId = :propertyId AND billingMonth >= :startMonth AND billingMonth <= :endMonth")
    double getPropertyCollectedBillsByMonthRange(long propertyId, String startMonth, String endMonth);

    @Query("SELECT COALESCE(SUM(amountPaid), 0) FROM utility_bills WHERE propertyId = :propertyId")
    double getPropertyCollectedBillsAllTime(long propertyId);

    @Query("SELECT COALESCE(SUM(remainingAmount), 0) FROM utility_bills WHERE billingMonth = :billingMonth")
    double getTotalOutstandingBillsByMonth(String billingMonth);

    @Query("SELECT COALESCE(SUM(remainingAmount), 0) FROM utility_bills WHERE billingMonth = :billingMonth AND status = 'OVERDUE'")
    double getTotalOverdueBillsByMonth(String billingMonth);

    @Query("SELECT COALESCE(SUM(amountDue), 0) FROM utility_bills")
    double getTotalExpectedBillsAllTime();

    @Query("SELECT COALESCE(SUM(amountPaid), 0) FROM utility_bills")
    double getTotalCollectedBillsAllTime();

    @Query("SELECT COALESCE(SUM(remainingAmount), 0) FROM utility_bills")
    double getTotalOutstandingBillsAllTime();

    @Query("SELECT COALESCE(SUM(remainingAmount), 0) FROM utility_bills WHERE status = 'OVERDUE'")
    double getTotalOverdueBillsAllTime();

    @Query("SELECT " +
            "b.id AS id, " +
            "b.propertyId AS propertyId, " +
            "b.unitId AS unitId, " +
            "b.tenancyId AS tenancyId, " +
            "b.billType AS billType, " +
            "b.billingMonth AS billingMonth, " +
            "b.dueDate AS dueDate, " +
            "b.amountDue AS amountDue, " +
            "b.amountPaid AS amountPaid, " +
            "b.remainingAmount AS remainingAmount, " +
            "b.status AS status, " +
            "b.meterNumber AS meterNumber, " +
            "b.previousReading AS previousReading, " +
            "b.currentReading AS currentReading, " +
            "b.unitsConsumed AS unitsConsumed, " +
            "b.ratePerUnit AS ratePerUnit, " +
            "b.fixedCharge AS fixedCharge, " +
            "b.vatOrTax AS vatOrTax, " +
            "b.billNumber AS billNumber, " +
            "b.lastPaymentDate AS lastPaymentDate, " +
            "b.paymentMethod AS paymentMethod, " +
            "b.notes AS notes, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor, " +
            "t.fullName AS tenantName, " +
            "t.phoneNumber AS tenantPhone " +
            "FROM utility_bills b " +
            "JOIN properties p ON b.propertyId = p.id " +
            "LEFT JOIN units u ON b.unitId = u.id " +
            "LEFT JOIN tenancies ty ON b.tenancyId = ty.id " +
            "LEFT JOIN tenants t ON ty.tenantId = t.id " +
            "ORDER BY b.id DESC")
    List<UtilityBillDisplayItem> getAllBillDisplayItems();

    @Query("SELECT " +
            "b.id AS id, " +
            "b.propertyId AS propertyId, " +
            "b.unitId AS unitId, " +
            "b.tenancyId AS tenancyId, " +
            "b.billType AS billType, " +
            "b.billingMonth AS billingMonth, " +
            "b.dueDate AS dueDate, " +
            "b.amountDue AS amountDue, " +
            "b.amountPaid AS amountPaid, " +
            "b.remainingAmount AS remainingAmount, " +
            "b.status AS status, " +
            "b.meterNumber AS meterNumber, " +
            "b.previousReading AS previousReading, " +
            "b.currentReading AS currentReading, " +
            "b.unitsConsumed AS unitsConsumed, " +
            "b.ratePerUnit AS ratePerUnit, " +
            "b.fixedCharge AS fixedCharge, " +
            "b.vatOrTax AS vatOrTax, " +
            "b.billNumber AS billNumber, " +
            "b.lastPaymentDate AS lastPaymentDate, " +
            "b.paymentMethod AS paymentMethod, " +
            "b.notes AS notes, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor, " +
            "t.fullName AS tenantName, " +
            "t.phoneNumber AS tenantPhone " +
            "FROM utility_bills b " +
            "JOIN properties p ON b.propertyId = p.id " +
            "LEFT JOIN units u ON b.unitId = u.id " +
            "LEFT JOIN tenancies ty ON b.tenancyId = ty.id " +
            "LEFT JOIN tenants t ON ty.tenantId = t.id " +
            "WHERE b.billingMonth = :billingMonth " +
            "ORDER BY b.id DESC")
    List<UtilityBillDisplayItem> getBillDisplayItemsByMonth(String billingMonth);

    @Query("SELECT " +
            "b.id AS id, " +
            "b.propertyId AS propertyId, " +
            "b.unitId AS unitId, " +
            "b.tenancyId AS tenancyId, " +
            "b.billType AS billType, " +
            "b.billingMonth AS billingMonth, " +
            "b.dueDate AS dueDate, " +
            "b.amountDue AS amountDue, " +
            "b.amountPaid AS amountPaid, " +
            "b.remainingAmount AS remainingAmount, " +
            "b.status AS status, " +
            "b.meterNumber AS meterNumber, " +
            "b.previousReading AS previousReading, " +
            "b.currentReading AS currentReading, " +
            "b.unitsConsumed AS unitsConsumed, " +
            "b.ratePerUnit AS ratePerUnit, " +
            "b.fixedCharge AS fixedCharge, " +
            "b.vatOrTax AS vatOrTax, " +
            "b.billNumber AS billNumber, " +
            "b.lastPaymentDate AS lastPaymentDate, " +
            "b.paymentMethod AS paymentMethod, " +
            "b.notes AS notes, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor, " +
            "t.fullName AS tenantName, " +
            "t.phoneNumber AS tenantPhone " +
            "FROM utility_bills b " +
            "JOIN properties p ON b.propertyId = p.id " +
            "LEFT JOIN units u ON b.unitId = u.id " +
            "LEFT JOIN tenancies ty ON b.tenancyId = ty.id " +
            "LEFT JOIN tenants t ON ty.tenantId = t.id " +
            "WHERE b.id = :id LIMIT 1")
    UtilityBillDisplayItem getBillDisplayItemById(long id);

    @Query("SELECT " +
            "b.id AS id, " +
            "b.propertyId AS propertyId, " +
            "b.unitId AS unitId, " +
            "b.tenancyId AS tenancyId, " +
            "b.billType AS billType, " +
            "b.billingMonth AS billingMonth, " +
            "b.dueDate AS dueDate, " +
            "b.amountDue AS amountDue, " +
            "b.amountPaid AS amountPaid, " +
            "b.remainingAmount AS remainingAmount, " +
            "b.status AS status, " +
            "b.meterNumber AS meterNumber, " +
            "b.previousReading AS previousReading, " +
            "b.currentReading AS currentReading, " +
            "b.unitsConsumed AS unitsConsumed, " +
            "b.ratePerUnit AS ratePerUnit, " +
            "b.fixedCharge AS fixedCharge, " +
            "b.vatOrTax AS vatOrTax, " +
            "b.billNumber AS billNumber, " +
            "b.lastPaymentDate AS lastPaymentDate, " +
            "b.paymentMethod AS paymentMethod, " +
            "b.notes AS notes, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor, " +
            "t.fullName AS tenantName, " +
            "t.phoneNumber AS tenantPhone " +
            "FROM utility_bills b " +
            "JOIN properties p ON b.propertyId = p.id " +
            "LEFT JOIN units u ON b.unitId = u.id " +
            "LEFT JOIN tenancies ty ON b.tenancyId = ty.id " +
            "LEFT JOIN tenants t ON ty.tenantId = t.id " +
            "WHERE b.propertyId = :propertyId " +
            "ORDER BY b.id DESC")
    List<UtilityBillDisplayItem> getBillDisplayItemsByProperty(long propertyId);

    @Query("SELECT " +
            "b.id AS id, " +
            "b.propertyId AS propertyId, " +
            "b.unitId AS unitId, " +
            "b.tenancyId AS tenancyId, " +
            "b.billType AS billType, " +
            "b.billingMonth AS billingMonth, " +
            "b.dueDate AS dueDate, " +
            "b.amountDue AS amountDue, " +
            "b.amountPaid AS amountPaid, " +
            "b.remainingAmount AS remainingAmount, " +
            "b.status AS status, " +
            "b.meterNumber AS meterNumber, " +
            "b.previousReading AS previousReading, " +
            "b.currentReading AS currentReading, " +
            "b.unitsConsumed AS unitsConsumed, " +
            "b.ratePerUnit AS ratePerUnit, " +
            "b.fixedCharge AS fixedCharge, " +
            "b.vatOrTax AS vatOrTax, " +
            "b.billNumber AS billNumber, " +
            "b.lastPaymentDate AS lastPaymentDate, " +
            "b.paymentMethod AS paymentMethod, " +
            "b.notes AS notes, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor, " +
            "t.fullName AS tenantName, " +
            "t.phoneNumber AS tenantPhone " +
            "FROM utility_bills b " +
            "JOIN properties p ON b.propertyId = p.id " +
            "LEFT JOIN units u ON b.unitId = u.id " +
            "LEFT JOIN tenancies ty ON b.tenancyId = ty.id " +
            "LEFT JOIN tenants t ON ty.tenantId = t.id " +
            "WHERE b.billingMonth >= :startMonth AND b.billingMonth <= :endMonth " +
            "ORDER BY b.billingMonth DESC, b.id DESC")
    List<UtilityBillDisplayItem> getBillDisplayItemsByMonthRange(String startMonth, String endMonth);
}
