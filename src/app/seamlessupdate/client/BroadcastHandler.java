package app.seamlessupdate.client;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class BroadcastHandler {
    private final Context context;
    private final LocalBroadcastManager broadcastManager;

    public static final String NOTIFY_UPDATE_INFO = "update_info";
    public static final String NOTIFY_UPDATE_DOWNLOAD_PROGRESS = "update_progress";
    public static final String NOTIFY_UPDATE_INSTALL_PROGRESS = "install_progress";
    public static final String NOTIFY_UPDATE_FAILED = "update_failed";
    public static final String NOTIFY_UPDATE_NOT_AVAILABLE = "update_not_available";
    public static final String NOTIFY_UPDATE_DONE = "update_done";

    BroadcastHandler(Context context) {
        this.context = context;
        this.broadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void sendUpdateFailed() {
        Intent intent = new Intent(NOTIFY_UPDATE_FAILED);
        broadcastManager.sendBroadcast(intent);
    }

    public void sendUpdateNotAvailable() {
        Intent intent = new Intent(NOTIFY_UPDATE_NOT_AVAILABLE);
        broadcastManager.sendBroadcast(intent);
    }

    public void sendUpdateInfo(String version, long date, String changelog) {
        Intent intent = new Intent(NOTIFY_UPDATE_INFO);
        intent.putExtra("buildVersion", version);
        intent.putExtra("buildDate", date);
        intent.putExtra("buildDescription", changelog);
        broadcastManager.sendBroadcast(intent);
    }

    public void sendUpdateDownloadProgress(long downloaded, long size) {
        Intent intent = new Intent(NOTIFY_UPDATE_DOWNLOAD_PROGRESS);
        intent.putExtra("updateProgress", downloaded);
        intent.putExtra("buildSize", size);
        broadcastManager.sendBroadcast(intent);
    }

    public void sendUpdateInstallProgress(long progress, long max) {
        Intent intent = new Intent(NOTIFY_UPDATE_INSTALL_PROGRESS);
        intent.putExtra("installProgress", progress);
        intent.putExtra("installMax", max);
        broadcastManager.sendBroadcast(intent);
    }

    public void sendUpdateDone() {
        Intent intent = new Intent(NOTIFY_UPDATE_DONE);
        broadcastManager.sendBroadcast(intent);
    }
}
