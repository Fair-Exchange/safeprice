package org.safecoin.safeprice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) && PreferenceManager.getDefaultSharedPreferences(context).getBoolean("start_at_boot", false)) {
            MainActivity.customStartService(context, new Intent(context, SafePrice.class));
        }
    }
}
