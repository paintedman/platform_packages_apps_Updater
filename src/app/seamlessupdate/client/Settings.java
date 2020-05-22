package app.seamlessupdate.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.PreferenceManager;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class Settings {
    private static final String KEY_CHANNEL = "channel";
    private static final String KEY_NETWORK_TYPE = "network_type";
    private static final String KEY_BATTERY_NOT_LOW = "battery_not_low";
    private static final String KEY_IDLE_REBOOT = "idle_reboot";
    static final String KEY_WAITING_FOR_REBOOT = "waiting_for_reboot";
    static final String KEY_UPDATE_STATUS = "update_status";
    private static final String KEY_LAST_UPDATE_CHECK = "last_update_check";
    private static final String KEY_AVAILABLE_UPDATE_VERSION = "available_update_version";
    private static final String KEY_AVAILABLE_UPDATE_DATE = "available_update_date";
    private static final String KEY_AVAILABLE_UPDATE_DESCRIPTION = "available_update_description";

    public enum UpdateStatus {
        NotAvailable,
        Available,
        Downloading,
        Installing,
        UpdateDone
    }

    static SharedPreferences getPreferences(final Context context) {
        final Context deviceContext = context.createDeviceProtectedStorageContext();
        return PreferenceManager.getDefaultSharedPreferences(deviceContext);
    }

    static String getChannel(final Context context) {
        String def = context.getString(R.string.channel_default);
        return getPreferences(context).getString(KEY_CHANNEL, def);
    }

    static int getNetworkType(final Context context) {
        int def = Integer.parseInt(context.getString(R.string.network_type_default));
        return getPreferences(context).getInt(KEY_NETWORK_TYPE, def);
    }

    static boolean getBatteryNotLow(final Context context) {
        boolean def = Boolean.parseBoolean(context.getString(R.string.battery_not_low_default));
        return getPreferences(context).getBoolean(KEY_BATTERY_NOT_LOW, def);
    }

    static boolean getIdleReboot(final Context context) {
        boolean def = Boolean.parseBoolean(context.getString(R.string.idle_reboot_default));
        return getPreferences(context).getBoolean(KEY_IDLE_REBOOT, def);
    }

    public static boolean getIsWaitingForReboot(final Context context) {
        return getPreferences(context).getBoolean(KEY_WAITING_FOR_REBOOT, false);
    }

    public static UpdateStatus getUpdateStatus(final Context context) {
        String enumString = getPreferences(context).getString(KEY_UPDATE_STATUS,
                UpdateStatus.NotAvailable.toString());
        return UpdateStatus.valueOf(enumString);
    }

    public static void setUpdateStatus(final Context context, UpdateStatus status) {
        SharedPreferences preferences = getPreferences( context );
        preferences.edit().putString(KEY_UPDATE_STATUS, status.toString()).apply();
    }

    public static long getLastUpdateCheck(final Context context) {
        return getPreferences(context).getLong(KEY_LAST_UPDATE_CHECK, -1) / 1000;
    }

    public static void setLastUpdateCheck(final Context context) {
        SharedPreferences preferences = getPreferences( context );
        long millis = System.currentTimeMillis();
        preferences.edit().putLong( KEY_LAST_UPDATE_CHECK, millis ).apply();
    }

    public static String getAvailableUpdateVersion(final Context context) {
        return getPreferences(context).getString(KEY_AVAILABLE_UPDATE_VERSION, "");
    }

    public static void setAvailableUpdateVersion(final Context context, String newValue) {
        getPreferences(context).edit().putString(KEY_AVAILABLE_UPDATE_VERSION, newValue).apply();
    }

    public static long getAvailableUpdateDate(final Context context) {
        return getPreferences(context).getLong(KEY_AVAILABLE_UPDATE_DATE, -1);
    }

    public static void setAvailableUpdateDate(final Context context, long newValue) {
        getPreferences(context).edit().putLong(KEY_AVAILABLE_UPDATE_DATE, newValue).apply();
    }

    public static String getAvailableUpdateDescription(final Context context) {
        return getPreferences(context).getString(KEY_AVAILABLE_UPDATE_DESCRIPTION, "");
    }

    public static void setAvailableUpdateDescription(final Context context, String newValue) {
        getPreferences(context).edit().putString(KEY_AVAILABLE_UPDATE_DESCRIPTION, newValue).apply();
    }

    /* UI for SettingsActivity*/
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            UserManager userManager = (UserManager) getContext().getSystemService(Context.USER_SERVICE);
            if (!userManager.isSystemUser()) {
                throw new SecurityException("system user only");
            }
            getPreferenceManager().setStorageDeviceProtected();
            PreferenceManager.setDefaultValues(getContext().createDeviceProtectedStorageContext(), R.xml.settings, false);
            addPreferencesFromResource(R.xml.settings);

            final Preference channel = findPreference(KEY_CHANNEL);
            channel.setOnPreferenceChangeListener((final Preference preference, final Object newValue) -> {
                getPreferences(getContext()).edit().putString(KEY_CHANNEL, (String) newValue).apply();
                if (!getIsWaitingForReboot(getContext())) {
                    PeriodicJob.schedule(getContext());
                }
                return true;
            });

            final Preference networkType = findPreference(KEY_NETWORK_TYPE);
            networkType.setOnPreferenceChangeListener((final Preference preference, final Object newValue) -> {
                final int value = Integer.parseInt((String) newValue);
                getPreferences(getContext()).edit().putInt(KEY_NETWORK_TYPE, value).apply();
                if (!getIsWaitingForReboot(getContext())) {
                    PeriodicJob.schedule(getContext());
                }
                return true;
            });

            final Preference batteryNotLow = findPreference(KEY_BATTERY_NOT_LOW);
            batteryNotLow.setOnPreferenceChangeListener((final Preference preference, final Object newValue) -> {
                getPreferences(getContext()).edit().putBoolean(KEY_BATTERY_NOT_LOW, (boolean) newValue).apply();
                if (!getIsWaitingForReboot(getContext())) {
                    PeriodicJob.schedule(getContext());
                }
                return true;
            });

            final Preference idleReboot = findPreference(KEY_IDLE_REBOOT);
            idleReboot.setOnPreferenceChangeListener((final Preference preference, final Object newValue) -> {
                final boolean value = (Boolean) newValue;
                if (!value) {
                    IdleReboot.cancel(getContext());
                }
                return true;
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            final ListPreference networkType = (ListPreference) findPreference(KEY_NETWORK_TYPE);
            networkType.setValue(Integer.toString(getNetworkType(getContext())));
        }
    }
}
