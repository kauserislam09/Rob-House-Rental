package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.BillPayment;

import java.util.List;

@Dao
public interface BillPaymentDao {

    @Insert
    long insert(BillPayment payment);

    @Update
    void update(BillPayment payment);

    @Delete
    void delete(BillPayment payment);

    @Query("SELECT * FROM bill_payments WHERE id = :id LIMIT 1")
    BillPayment getPaymentById(long id);

    @Query("SELECT * FROM bill_payments WHERE billId = :billId ORDER BY id DESC")
    List<BillPayment> getPaymentsByBill(long billId);

    @Query("SELECT * FROM bill_payments ORDER BY id DESC")
    List<BillPayment> getAllPayments();

    @Query("SELECT COALESCE(SUM(amount), 0) FROM bill_payments WHERE billId = :billId")
    double getTotalPaidForBill(long billId);
}
