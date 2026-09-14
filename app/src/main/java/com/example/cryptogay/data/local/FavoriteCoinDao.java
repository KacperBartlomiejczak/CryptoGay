package com.example.cryptogay.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteCoinDao {

    @Query("SELECT * FROM favorite_coins ORDER BY addedAt DESC")
    LiveData<List<FavoriteCoin>> getAllFavorites();

    @Query("SELECT id FROM favorite_coins")
    LiveData<List<String>> getAllFavoriteIds();

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_coins WHERE id = :coinId)")
    LiveData<Boolean> isFavorite(String coinId);

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_coins WHERE id = :coinId)")
    boolean isFavoriteSync(String coinId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(FavoriteCoin coin);

    @Query("DELETE FROM favorite_coins WHERE id = :coinId")
    void deleteFavoriteById(String coinId);
}
