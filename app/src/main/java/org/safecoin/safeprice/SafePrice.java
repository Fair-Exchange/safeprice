package org.safecoin.safeprice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import java.util.Locale;


public class SafePrice extends Service {
    public static final String UPDATE = "UPDATE";
    public static final String STOP = "STOP";

    SharedPreferences prefs;
    private Exchange exchange;
    private Handler h = new Handler();
    private Runnable update = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    double rate = exchange.getExchangeRate();
                    if (rate == 0) {
                        h.postDelayed(update, 1000);
                    } else {
                        NotificationManagerCompat.from(SafePrice.this).notify(1, new NotificationCompat.Builder(SafePrice.this, getString(R.string.app_name))
                                .setSmallIcon(R.drawable.ic_stat_name)
                                .setContentTitle(getString(R.string.app_name))
                                .setTicker(exchange.getExchange())
                                .setContentText(String.format(Locale.ENGLISH, "%S: %.8f", exchange.getTruePair(), rate))
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(String.format(Locale.ENGLISH, "%S: %.8f", exchange.getTruePair(), rate))
                                        .setBigContentTitle(getString(R.string.app_name))
                                        .setSummaryText(exchange.getExchange()))
                                .addAction(new NotificationCompat.Action(R.drawable.ic_stat_name, getString(R.string.update), getUpdatePendingIntent()))
                                .addAction(new NotificationCompat.Action(R.drawable.ic_stat_name, getString(R.string.settings), getSettingsIntent()))
                                .addAction(new NotificationCompat.Action(R.drawable.ic_stat_name, getString(R.string.stop), getStopPendingIntent()))
                                .setShowWhen(prefs.getBoolean("when", false))
                                .setOnlyAlertOnce(true)
                                .build());
                        h.postDelayed(update, prefs.getInt("delay", 5) * 60000);
                    }
                }
            }).start();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(1, buildNotification());
        }

        h.post(update);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (UPDATE.equals(intent.getAction())) {
                stopHandler();
                NotificationManagerCompat.from(this).notify(1, buildNotification());
                h.post(update);
            } else if (STOP.equals(intent.getAction())) {
                stopSelfForeground();
            }
        }
        return START_STICKY;
    }

    private Notification buildNotification() {
        exchange = new Exchange(prefs.getString("exchange", "SafeTrade"), prefs.getString("market", "safebtc"));
        return new NotificationCompat.Builder(this, getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(getString(R.string.app_name))
                .setTicker(exchange.getExchange())
                .setContentText(getString(R.string.updating))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.updating))
                        .setBigContentTitle(getString(R.string.app_name))
                        .setSummaryText(exchange.getExchange()))
                .addAction(new NotificationCompat.Action(R.drawable.ic_stat_name, getString(R.string.stop), getStopPendingIntent()))
                .setShowWhen(prefs.getBoolean("when", false))
                .setOnlyAlertOnce(true)
                .build();
    }

    private PendingIntent getStopPendingIntent() {
        Intent stop = new Intent(this, SafePrice.class);
        stop.setAction(STOP);
        return PendingIntent.getService(this, 0, stop, PendingIntent.FLAG_ONE_SHOT);
    }

    private PendingIntent getSettingsIntent() {
        Intent stop = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 0, stop, 0);
    }

    private PendingIntent getUpdatePendingIntent() {
        Intent update = new Intent(this, SafePrice.class);
        update.setAction(UPDATE);
        return PendingIntent.getService(this, 0, update, 0);
    }

    public void stopHandler() {
        h.removeCallbacks(update);
    }

    public final void stopSelfForeground() {
        stopHandler();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
