package app.seamlessupdate.client;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import app.seamlessupdate.client.ui.MainActivity;

import static android.app.NotificationManager.IMPORTANCE_LOW;

public class NotificationHandler {

    private static final int NOTIFICATION_ID_PROGRESS = 1;
    private static final int NOTIFICATION_ID_REBOOT = 3;
    private static final int NOTIFICATION_ID_UPDATE_AVAILABLE = 4;
    private static final String NOTIFICATION_CHANNEL_ID = "updates2";
    private static final String NOTIFICATION_CHANNEL_ID_PROGRESS = "progress";
    private static final int PENDING_REBOOT_ID = 1;
    private static final int PENDING_SETTINGS_ID = 2;

    private final Context context;
    private final NotificationManager notificationManager;

    NotificationHandler(Context context) {
        this.context = context;
        this.notificationManager = context.getSystemService(NotificationManager.class);
        createProgressNotificationChannel();
    }

    void showDownloadNotification(IntentService service, int progress, int max) {
        String title = context.getString(R.string.notification_download_title);
        Notification.Builder builder = new Notification.Builder(context, NOTIFICATION_CHANNEL_ID_PROGRESS)
                .setContentIntent(getPendingSettingsIntent())
                .setContentTitle(title)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_system_update_white_24dp);
        if (max <= 0) builder.setProgress(0, 0, true);
        else builder.setProgress(max, progress, false);

        /* We need startForeground to not allow Android to close
        * service when we close MainActivity */
        service.startForeground(NOTIFICATION_ID_PROGRESS, builder.build());
    }

    void cancelDownloadNotification() {
        notificationManager.cancel(NOTIFICATION_ID_PROGRESS);
    }

    void showRebootNotification() {
        final PendingIntent reboot = PendingIntent.getBroadcast(context, PENDING_REBOOT_ID, new Intent(context, RebootReceiver.class), 0);

        final NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.enableVibration(true);
        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(NOTIFICATION_ID_REBOOT, new Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
                .addAction(R.drawable.ic_restart, context.getString(R.string.notification_reboot_action), reboot)
                .setContentIntent(getPendingSettingsIntent())
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_text))
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_system_update_white_24dp)
                .build());
    }

    void showInstallNotification(IntentService service, int progress, int max) {
        String title = context.getString(R.string.notification_install_title);
        Notification.Builder builder = new Notification.Builder(context, NOTIFICATION_CHANNEL_ID_PROGRESS)
                .setContentIntent(getPendingSettingsIntent())
                .setContentTitle(title)
                .setOngoing(true)
                .setProgress(max, progress, false)
                .setSmallIcon(R.drawable.ic_system_update_white_24dp);

        /* We need startForeground to not allow Android to close
         * service when we close MainActivity */
        service.startForeground(NOTIFICATION_ID_PROGRESS, builder.build());
    }

    void cancelInstallNotification() {
        notificationManager.cancel(NOTIFICATION_ID_PROGRESS);
    }

    void showUpdateAvailableNotification() {
        final NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.notification_new_update_title), NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.enableVibration(true);
        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(NOTIFICATION_ID_UPDATE_AVAILABLE, new Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentIntent(getPendingSettingsIntent())
                .setContentTitle( context.getString(R.string.checking_for_updates ) )
                .setContentText( context.getString(R.string.notification_new_update_title ) )
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_system_update_white_24dp)
                .setOnlyAlertOnce(true)
                .build());
    }

    void cancelUpdateAvailableNotification() {
        notificationManager.cancel(NOTIFICATION_ID_UPDATE_AVAILABLE);
    }

    private void createProgressNotificationChannel() {
        final NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_PROGRESS,
                context.getString(R.string.notification_channel_progress), IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
    }

    private PendingIntent getPendingSettingsIntent() {
        return PendingIntent.getActivity(context, PENDING_SETTINGS_ID, new Intent(context, MainActivity.class), 0);
    }

}
