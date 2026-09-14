package com.example.cryptogay.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CachedCoinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CachedCoin> coins);

    @Query("SELECT * FROM cached_coins ORDER BY marketCapRank ASC")
    List<CachedCoin> getAllCachedCoins();

    @Query("DELETE FROM cached_coins")
    void deleteAll();

    @Query("SELECT MIN(cachedAt) FROM cached_coins")
    Long getOldestCacheTimestamp();

    @Query("SELECT MAX(cachedAt) FROM cached_coins")
    Long getLatestCacheTimestamp();

    @Query("SELECT COUNT(*) FROM cached_coins")
    int getCachedCoinsCount();
}
