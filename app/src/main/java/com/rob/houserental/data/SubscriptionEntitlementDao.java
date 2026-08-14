package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.SubscriptionEntitlement;

import java.util.List;

@Dao
public interface SubscriptionEntitlementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(SubscriptionEntitlement entitlement);

    @Update
    void update(SubscriptionEntitlement entitlement);

    @Query("SELECT * FROM subscription_entitlements WHERE userId = :userId AND entitlementType = 'PREMIUM' LIMIT 1")
    SubscriptionEntitlement getEntitlementByUserId(String userId);

    @Query("SELECT * FROM subscription_entitlements WHERE entitlementId = :entitlementId LIMIT 1")
    SubscriptionEntitlement getById(String entitlementId);

    @Query("SELECT * FROM subscription_entitlements ORDER BY createdAt DESC")
    List<SubscriptionEntitlement> getAll();
}
