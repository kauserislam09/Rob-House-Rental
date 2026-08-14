package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.Payment;

import java.util.List;

@Dao
public interface PaymentDao {

    @Insert
    long insert(Payment payment);

    @Update
    void update(Payment payment);

    @Delete
    void delete(Payment payment);

    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    Payment getPaymentById(long id);

    @Query("SELECT * FROM payments WHERE rentRecordId = :rentRecordId ORDER BY id DESC")
    List<Payment> getPaymentsByRent(long rentRecordId);

    @Query("SELECT * FROM payments ORDER BY id DESC")
    List<Payment> getAllPayments();

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE rentRecordId = :rentRecordId")
    double getTotalPaidForRent(long rentRecordId);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE paymentDate >= :startDate AND paymentDate <= :endDate")
    double getCollectedRentByPaymentDate(String startDate, String endDate);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM payments p " +
            "JOIN rent_records r ON p.rentRecordId = r.id " +
            "JOIN tenancies ty ON r.tenancyId = ty.id " +
            "JOIN units u ON ty.unitId = u.id " +
            "WHERE u.propertyId = :propertyId AND p.paymentDate >= :startDate AND p.paymentDate <= :endDate")
    double getPropertyCollectedRentByPaymentDate(long propertyId, String startDate, String endDate);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM payments p " +
            "JOIN rent_records r ON p.rentRecordId = r.id " +
            "JOIN tenancies ty ON r.tenancyId = ty.id " +
            "JOIN units u ON ty.unitId = u.id " +
            "WHERE u.propertyId = :propertyId")
    double getPropertyCollectedRentAllTime(long propertyId);

    @Query("SELECT " +
            "p.id AS id, " +
            "p.rentRecordId AS rentRecordId, " +
            "p.amount AS amount, " +
            "p.paymentDate AS paymentDate, " +
            "p.paymentMethod AS paymentMethod, " +
            "p.reference AS reference, " +
            "p.notes AS notes, " +
            "t.fullName AS tenantName, " +
            "t.phoneNumber AS tenantPhone, " +
            "prop.name AS propertyName, " +
            "prop.id AS propertyId, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor, " +
            "r.billingMonth AS billingMonth " +
            "FROM payments p " +
            "JOIN rent_records r ON p.rentRecordId = r.id " +
            "JOIN tenancies ty ON r.tenancyId = ty.id " +
            "JOIN tenants t ON ty.tenantId = t.id " +
            "JOIN units u ON ty.unitId = u.id " +
            "JOIN properties prop ON u.propertyId = prop.id " +
            "ORDER BY p.id DESC")
    List<com.rob.houserental.model.PaymentDisplayItem> getAllPaymentDisplayItems();

    @Query("SELECT " +
            "p.id AS id, " +
            "p.rentRecordId AS rentRecordId, " +
            "p.amount AS amount, " +
            "p.paymentDate AS paymentDate, " +
            "p.paymentMethod AS paymentMethod, " +
            "p.reference AS reference, " +
            "p.notes AS notes, " +
            "t.fullName AS tenantName, " +
            "t.phoneNumber AS tenantPhone, " +
            "prop.name AS propertyName, " +
            "prop.id AS propertyId, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor, " +
            "r.billingMonth AS billingMonth " +
            "FROM payments p " +
            "JOIN rent_records r ON p.rentRecordId = r.id " +
            "JOIN tenancies ty ON r.tenancyId = ty.id " +
            "JOIN tenants t ON ty.tenantId = t.id " +
            "JOIN units u ON ty.unitId = u.id " +
            "JOIN properties prop ON u.propertyId = prop.id " +
            "WHERE p.paymentDate >= :startDate AND p.paymentDate <= :endDate " +
            "ORDER BY p.id DESC")
    List<com.rob.houserental.model.PaymentDisplayItem> getPaymentDisplayItemsByDateRange(String startDate, String endDate);
}
