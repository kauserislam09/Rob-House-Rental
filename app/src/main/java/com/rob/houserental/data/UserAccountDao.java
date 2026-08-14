package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.UserAccount;

@Dao
public interface UserAccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(UserAccount userAccount);

    @Update
    void update(UserAccount userAccount);

    @Query("SELECT * FROM user_accounts WHERE userId = :userId LIMIT 1")
    UserAccount getUserById(String userId);

    @Query("SELECT * FROM user_accounts LIMIT 1")
    UserAccount getPrimaryUser();
}
