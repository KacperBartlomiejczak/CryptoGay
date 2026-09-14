package com.example.cryptogay.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.cryptogay.data.local.AppDatabase;
import com.example.cryptogay.data.local.PriceAlert;
import com.example.cryptogay.data.local.PriceAlertDao;
import com.example.cryptogay.data.model.Coin;
import com.example.cryptogay.data.remote.ApiClient;
import com.example.cryptogay.notification.AlertNotificationHelper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;

public class PriceAlertWorker extends Worker {

    public static final String WORK_NAME_PERIODIC = "crypto_price_alert_periodic_work";

    public PriceAlertWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        PriceAlertDao alertDao = AppDatabase.getInstance(context).priceAlertDao();
        List<PriceAlert> activeAlerts = alertDao.getActiveAlertsList();

        if (activeAlerts == null || activeAlerts.isEmpty()) {
            return Result.success();
        }

        StringBuilder idsBuilder = new StringBuilder();
        for (int i = 0; i < activeAlerts.size(); i++) {
            if (i > 0) idsBuilder.append(",");
            idsBuilder.append(activeAlerts.get(i).getCoinId());
        }

        try {
            Response<List<Coin>> response = ApiClient.getApiService()
                    .getCoinDetails("usd", idsBuilder.toString())
                    .execute();

            if (response.isSuccessful() && response.body() != null) {
                List<Coin> coins = response.body();

                for (PriceAlert alert : activeAlerts) {
                    for (Coin coin : coins) {
                        if (alert.getCoinId().equalsIgnoreCase(coin.getId())) {
                            double currentPrice = coin.getCurrentPrice();
                            boolean triggered = false;

                            if (alert.isAbove() && currentPrice >= alert.getTargetPrice()) {
                                triggered = true;
                            } else if (!alert.isAbove() && currentPrice <= alert.getTargetPrice()) {
                                triggered = true;
                            }

                            if (triggered) {
                                AlertNotificationHelper.showPriceAlertNotification(context, alert, currentPrice);
                                alertDao.updateActiveStatus(alert.getCoinId(), false);
                            }
                            break;
                        }
                    }
                }
                return Result.success();
            } else {
                return Result.retry();
            }
        } catch (IOException e) {
            return Result.retry();
        } catch (Exception e) {
            return Result.failure();
        }
    }

    public static void schedulePeriodicCheck(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                PriceAlertWorker.class,
                15,
                TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        WORK_NAME_PERIODIC,
                        ExistingPeriodicWorkPolicy.KEEP,
                        request
                );
    }

    public static void checkOnce(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(PriceAlertWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueue(request);
    }
}
