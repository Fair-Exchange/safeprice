package org.safecoin.safeprice;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.h6ah4i.android.preference.NumberPickerPreferenceCompat;
import com.h6ah4i.android.preference.NumberPickerPreferenceDialogFragmentCompat;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new PreferenceFragment())
                .commit();
        customStartService(this, new Intent(this, SafePrice.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        customStartService(this, new Intent(this, SafePrice.class));
    }

    public static class PreferenceFragment extends PreferenceFragmentCompat {
        private static final String DIALOG_FRAGMENT_TAG =
                "androidx.preference.PreferenceFragment.DIALOG";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            final ListPreference market = findPreference("market");
            market.setEntries(Exchange.getMarkets(getPreferenceManager().getSharedPreferences().getString("exchange", "SafeTrade")));
            market.setEntryValues(Exchange.getMarketValues(getPreferenceManager().getSharedPreferences().getString("exchange", "SafeTrade")));
            market.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.equals(market.getValue())) {
                        return false;
                    }
                    updateNotification(getContext());
                    return true;
                }
            });

            final ListPreference exchange = findPreference("exchange");
            exchange.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.equals(exchange.getValue())) {
                        return false;
                    }

                    market.setEntries(Exchange.getMarkets((String) newValue));
                    market.setEntryValues(Exchange.getMarketValues((String) newValue));
                    market.setValueIndex(0);
                    updateNotification(getContext());
                    return true;
                }
            });

            NumberPickerPreferenceCompat frequency = findPreference("frequency");
            frequency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updateNotification(getContext());
                    return true;
                }
            });


            SwitchPreference when = findPreference("when");
            when.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updateNotification(getContext());
                    return true;
                }
            });

            Preference sources = findPreference("sources");
            sources.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/fair-exchange/safeprice")));
                    return true;
                }
            });
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            // check if dialog is already showing
            if (getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
                return;
            }

            final DialogFragment f;

            if (preference instanceof NumberPickerPreferenceCompat) {
                f = NumberPickerPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            } else {
                f = null;
            }

            if (f != null) {
                f.setTargetFragment(this, 0);
                f.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }
    }

    static void updateNotification(Context context) {
        Intent update = new Intent(context, SafePrice.class);
        update.setAction(SafePrice.UPDATE);
        customStartService(context, update);
    }

    static void customStartService(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(context.getString(R.string.app_name), context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(false);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null, null);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

}
