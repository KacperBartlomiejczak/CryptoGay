package com.example.cryptogay.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PriceAlertDao {

    @Query("SELECT * FROM price_alerts ORDER BY createdAt DESC")
    LiveData<List<PriceAlert>> getAllAlerts();

    @Query("SELECT * FROM price_alerts WHERE isActive = 1")
    List<PriceAlert> getActiveAlertsList();

    @Query("SELECT * FROM price_alerts WHERE coinId = :coinId LIMIT 1")
    LiveData<PriceAlert> getAlertForCoin(String coinId);

    @Query("SELECT * FROM price_alerts WHERE coinId = :coinId LIMIT 1")
    PriceAlert getAlertForCoinSync(String coinId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(PriceAlert alert);

    @Delete
    void delete(PriceAlert alert);

    @Query("DELETE FROM price_alerts WHERE coinId = :coinId")
    void deleteByCoinId(String coinId);

    @Query("UPDATE price_alerts SET isActive = :isActive WHERE coinId = :coinId")
    void updateActiveStatus(String coinId, boolean isActive);
}
