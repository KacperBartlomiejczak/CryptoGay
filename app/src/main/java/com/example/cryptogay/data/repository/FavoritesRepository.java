package com.example.cryptogay.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.cryptogay.data.local.AppDatabase;
import com.example.cryptogay.data.local.FavoriteCoin;
import com.example.cryptogay.data.local.FavoriteCoinDao;
import com.example.cryptogay.data.model.Coin;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FavoritesRepository {

    public interface Callback<T> {
        void onComplete(T result);
    }

    private final FavoriteCoinDao favoriteCoinDao;
    private final Executor executor;

    public FavoritesRepository(Context context) {
        this(AppDatabase.getInstance(context).favoriteCoinDao(), Executors.newSingleThreadExecutor());
    }

    public FavoritesRepository(FavoriteCoinDao favoriteCoinDao, Executor executor) {
        this.favoriteCoinDao = favoriteCoinDao;
        this.executor = executor;
    }

    public LiveData<Boolean> isFavorite(String coinId) {
        return favoriteCoinDao.isFavorite(coinId);
    }

    public LiveData<List<FavoriteCoin>> getAllFavorites() {
        return favoriteCoinDao.getAllFavorites();
    }

    public LiveData<List<String>> getAllFavoriteIds() {
        return favoriteCoinDao.getAllFavoriteIds();
    }

    public void toggleFavorite(Coin coin, Callback<Boolean> callback) {
        if (coin == null || coin.getId() == null) {
            if (callback != null) callback.onComplete(false);
            return;
        }

        executor.execute(() -> {
            boolean exists = favoriteCoinDao.isFavoriteSync(coin.getId());
            if (exists) {
                favoriteCoinDao.deleteFavoriteById(coin.getId());
                if (callback != null) {
                    callback.onComplete(false);
                }
            } else {
                FavoriteCoin fav = new FavoriteCoin(
                        coin.getId(),
                        coin.getSymbol(),
                        coin.getName(),
                        coin.getImage(),
                        coin.getCurrentPrice(),
                        coin.getPriceChangePercentage24h(),
                        System.currentTimeMillis()
                );
                favoriteCoinDao.insertFavorite(fav);
                if (callback != null) {
                    callback.onComplete(true);
                }
            }
        });
    }
}
