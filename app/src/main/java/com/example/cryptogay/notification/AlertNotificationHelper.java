package com.example.cryptogay.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.cryptogay.MainActivity;
import com.example.cryptogay.R;
import com.example.cryptogay.data.local.PriceAlert;
import com.example.cryptogay.util.CurrencyFormatter;

public class AlertNotificationHelper {

    public static final String CHANNEL_ID = "crypto_price_alerts_channel";
    private static final String CHANNEL_NAME = "Alerty cenowe";
    private static final String CHANNEL_DESC = "Powiadomienia o przekroczeniu ustawionych progów cenowych";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showPriceAlertNotification(Context context, PriceAlert alert, double currentPrice) {
        if (context == null || alert == null) return;

        createNotificationChannel(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("coin_id", alert.getCoinId());

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                alert.getCoinId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        String conditionText = alert.isAbove()
                ? context.getString(R.string.alert_condition_reached_above)
                : context.getString(R.string.alert_condition_reached_below);

        String title = context.getString(R.string.alert_notification_title, alert.getCoinName(), alert.getCoinSymbol());
        String body = context.getString(
                R.string.alert_notification_body,
                CurrencyFormatter.formatPrice(currentPrice),
                conditionText,
                CurrencyFormatter.formatPrice(alert.getTargetPrice())
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(alert.getCoinId().hashCode(), builder.build());
    }
}
