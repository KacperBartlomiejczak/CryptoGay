package com.example.cryptogay.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.cryptogay.data.local.AppDatabase;
import com.example.cryptogay.data.local.PriceAlert;
import com.example.cryptogay.data.local.PriceAlertDao;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AlertsRepository {

    public interface Callback<T> {
        void onComplete(T result);
    }

    private final PriceAlertDao priceAlertDao;
    private final Executor executor;
    private final Context context;

    public AlertsRepository(Context context) {
        this(AppDatabase.getInstance(context).priceAlertDao(), Executors.newSingleThreadExecutor(), context.getApplicationContext());
    }

    public AlertsRepository(PriceAlertDao priceAlertDao, Executor executor) {
        this(priceAlertDao, executor, null);
    }

    public AlertsRepository(PriceAlertDao priceAlertDao, Executor executor, Context context) {
        this.priceAlertDao = priceAlertDao;
        this.executor = executor;
        this.context = context;
    }

    public LiveData<List<PriceAlert>> getAllAlerts() {
        return priceAlertDao.getAllAlerts();
    }

    public LiveData<PriceAlert> getAlertForCoin(String coinId) {
        return priceAlertDao.getAlertForCoin(coinId);
    }

    public void saveAlert(PriceAlert alert, Callback<Void> callback) {
        executor.execute(() -> {
            priceAlertDao.insertOrUpdate(alert);
            if (context != null) {
                com.example.cryptogay.worker.PriceAlertWorker.checkOnce(context);
            }
            if (callback != null) {
                callback.onComplete(null);
            }
        });
    }

    public void deleteAlert(PriceAlert alert, Callback<Void> callback) {
        executor.execute(() -> {
            priceAlertDao.delete(alert);
            if (callback != null) {
                callback.onComplete(null);
            }
        });
    }

    public void deleteAlertByCoinId(String coinId, Callback<Void> callback) {
        executor.execute(() -> {
            priceAlertDao.deleteByCoinId(coinId);
            if (callback != null) {
                callback.onComplete(null);
            }
        });
    }

    public void toggleActive(String coinId, boolean isActive, Callback<Void> callback) {
        executor.execute(() -> {
            priceAlertDao.updateActiveStatus(coinId, isActive);
            if (callback != null) {
                callback.onComplete(null);
            }
        });
    }
}
