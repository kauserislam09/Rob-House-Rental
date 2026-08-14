package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.PaymentOrder;

import java.util.List;

@Dao
public interface PaymentOrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(PaymentOrder order);

    @Update
    void update(PaymentOrder order);

    @Query("SELECT * FROM payment_orders WHERE orderId = :orderId LIMIT 1")
    PaymentOrder getOrderById(String orderId);

    @Query("SELECT * FROM payment_orders WHERE userId = :userId ORDER BY createdAt DESC")
    List<PaymentOrder> getOrdersByUserId(String userId);

    @Query("SELECT * FROM payment_orders WHERE transactionId = :transactionId LIMIT 1")
    PaymentOrder getOrderByTransactionId(String transactionId);

    @Query("SELECT * FROM payment_orders ORDER BY createdAt DESC")
    List<PaymentOrder> getAllOrders();
}
