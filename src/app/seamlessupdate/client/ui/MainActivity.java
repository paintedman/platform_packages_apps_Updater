package app.seamlessupdate.client.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.NumberFormat;

import app.seamlessupdate.client.BroadcastHandler;
import app.seamlessupdate.client.PeriodicJob;
import app.seamlessupdate.client.R;
import app.seamlessupdate.client.RebootReceiver;
import app.seamlessupdate.client.Service;
import app.seamlessupdate.client.Settings;
import app.seamlessupdate.client.misc.StringGenerator;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private UpdateInfoReceiver updateInfoReceiver;
    private TextView tvBuildVersion;
    private TextView tvBuildDate;
    private ProgressBar pbProgressBar;
    private TextView tvProgressText;
    private Button btnUpdateAction;
    private Button btnReboot;
    private View updateAvailable;
    private View updateNotAvailable;
    private View updateDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prepareUi();
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateInfoReceiver = new UpdateInfoReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastHandler.NOTIFY_UPDATE_INFO);
        intentFilter.addAction(BroadcastHandler.NOTIFY_UPDATE_DOWNLOAD_PROGRESS);
        intentFilter.addAction(BroadcastHandler.NOTIFY_UPDATE_INSTALL_PROGRESS);
        intentFilter.addAction(BroadcastHandler.NOTIFY_UPDATE_NOT_AVAILABLE);
        intentFilter.addAction(BroadcastHandler.NOTIFY_UPDATE_FAILED);
        intentFilter.addAction(BroadcastHandler.NOTIFY_UPDATE_DONE);
        LocalBroadcastManager.getInstance(this).registerReceiver(updateInfoReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateInfoReceiver);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForUpdates();

        switch (Settings.getUpdateStatus(this)) {
            case NotAvailable:
                prepareUiUpdateNotAvailable();
                break;
            case Available:
                prepareUiUpdateAvailable();
                break;
            case Downloading:
                prepareUiUpdateDownloading();
                break;
            case Installing:
                prepareUiUpdateInstalling();
                break;
            case UpdateDone:
                prepareUiUpdateDone();
                break;
        }
        if (Settings.getIsWaitingForReboot(this)) {
            prepareUiUpdateDone();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh: {
                checkForUpdates();
                updateLastCheckedString();
                showSnackbar(R.string.checking_for_updates, Snackbar.LENGTH_SHORT);
                return true;
            }
            case R.id.menu_preferences: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    void showSnackbar(int stringId, int duration) {
        Snackbar.make(findViewById(R.id.main_container), stringId, duration).show();
    }

    void checkForUpdates() {
        if (Settings.getUpdateStatus(this) == Settings.UpdateStatus.NotAvailable) {
            if (!Settings.getIsWaitingForReboot(this)) {
                PeriodicJob.schedule(this, true);
            }
            Settings.setLastUpdateCheck(this);
        }
    }

    void prepareUi() {
        updateAvailable = findViewById(R.id.update_available_layout);
        updateNotAvailable = findViewById(R.id.update_not_available);
        updateDone = findViewById(R.id.update_waits_for_reboot);
        tvBuildVersion = findViewById(R.id.build_version);
        tvBuildDate = findViewById(R.id.build_date);
        pbProgressBar = findViewById(R.id.progress_bar);
        tvProgressText = findViewById(R.id.progress_text);

        TextView headerTitle = findViewById(R.id.header_title);
        headerTitle.setText(getString(
                R.string.header_title_text,
                Build.VERSION.INCREMENTAL));

        TextView headerBuildVersion = findViewById(R.id.header_build_version);
        headerBuildVersion.setText(getString(
                R.string.header_android_version,
                Build.VERSION.RELEASE));

        TextView headerBuildDate = findViewById(R.id.header_build_date);
        headerBuildDate.setText(StringGenerator.getDateLocalizedUTC(this,
                DateFormat.LONG, Build.TIME / 1000));

        btnUpdateAction = findViewById(R.id.update_action);
        btnUpdateAction.setOnClickListener(v -> {
            startService(new Intent(Service.SERVICE_ACTION_INSTALL, null, this, Service.class));
            prepareUiUpdateDownloading();
            pbProgressBar.setIndeterminate(true);
        });

        btnReboot = findViewById(R.id.reboot_button);
        btnReboot.setOnClickListener(v -> {
            sendBroadcast(new Intent(this, RebootReceiver.class));
        });

        checkForUpdates();

        updateLastCheckedString();
        updateAvailableBuildInfo();
    }

    private void updateLastCheckedString() {
        long lastCheck = Settings.getLastUpdateCheck(this);
        String lastCheckString = getString(R.string.header_last_updates_check,
                StringGenerator.getDateLocalized(this, DateFormat.LONG, lastCheck),
                StringGenerator.getTimeLocalized(this, lastCheck));

        TextView headerLastCheck = findViewById(R.id.header_last_check);
        headerLastCheck.setText(lastCheckString);
    }

    private void updateAvailableBuildInfo() {
        tvBuildVersion.setText(Settings.getAvailableUpdateVersion(this));
        tvBuildDate.setText(StringGenerator.getDateLocalizedUTC(this, DateFormat.LONG,
                Settings.getAvailableUpdateDate(this)));
    }

    private void prepareUiUpdateDone() {
        updateAvailable.setVisibility(View.INVISIBLE);
        updateNotAvailable.setVisibility(View.INVISIBLE);
        updateDone.setVisibility(View.VISIBLE);
    }

    private void prepareUiUpdateNotAvailable() {
        updateAvailable.setVisibility(View.INVISIBLE);
        updateNotAvailable.setVisibility(View.VISIBLE);
        updateDone.setVisibility(View.INVISIBLE);
    }

    private void prepareUiUpdateAvailable() {
        updateAvailable.setVisibility(View.VISIBLE);
        updateNotAvailable.setVisibility(View.INVISIBLE);
        updateDone.setVisibility(View.INVISIBLE);
        pbProgressBar.setVisibility(View.GONE);
        tvProgressText.setVisibility(View.GONE);

        btnUpdateAction.setText(getString(R.string.update_action_install));
        btnUpdateAction.setEnabled(true);
    }

    private void prepareUiUpdateDownloading() {
        updateAvailable.setVisibility(View.VISIBLE);
        updateNotAvailable.setVisibility(View.INVISIBLE);
        pbProgressBar.setVisibility(View.VISIBLE);
        tvProgressText.setVisibility(View.VISIBLE);

        btnUpdateAction.setText(getText(R.string.update_action_downloading));
        btnUpdateAction.setEnabled(false);
        pbProgressBar.setIndeterminate(true);
    }

    private void prepareUiUpdateInstalling() {
        updateAvailable.setVisibility(View.VISIBLE);
        updateNotAvailable.setVisibility(View.INVISIBLE);
        pbProgressBar.setVisibility(View.VISIBLE);
        tvProgressText.setVisibility(View.VISIBLE);

        btnUpdateAction.setText(getText(R.string.update_action_installing));
        btnUpdateAction.setEnabled(false);
        pbProgressBar.setIndeterminate(true);
    }

    private class UpdateInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BroadcastHandler.NOTIFY_UPDATE_INFO:
                    prepareUiUpdateAvailable();
                    onReceiveUpdateInfo(context, intent);
                    break;
                case BroadcastHandler.NOTIFY_UPDATE_DOWNLOAD_PROGRESS:
                    prepareUiUpdateDownloading();
                    onReceiveUpdateDownloadProgress(context, intent);
                    break;
                case BroadcastHandler.NOTIFY_UPDATE_INSTALL_PROGRESS:
                    prepareUiUpdateInstalling();
                    onReceiveUpdateInstallProgress(context, intent);
                    break;
                case BroadcastHandler.NOTIFY_UPDATE_DONE:
                    prepareUiUpdateDone();
                    break;
                case BroadcastHandler.NOTIFY_UPDATE_NOT_AVAILABLE:
                    prepareUiUpdateNotAvailable();
                    break;
                case BroadcastHandler.NOTIFY_UPDATE_FAILED:
                    prepareUiUpdateNotAvailable();
                    showSnackbar(R.string.update_failed, Snackbar.LENGTH_SHORT);
                    break;
            }
        }

        private void onReceiveUpdateDownloadProgress(Context context, Intent intent) {
            long downloaded = intent.getLongExtra("updateProgress", 0);
            long buildSize = intent.getLongExtra("buildSize", 1);

            pbProgressBar.setIndeterminate(false);
            pbProgressBar.setMax((int) buildSize);
            pbProgressBar.setProgress((int) downloaded);
            tvProgressText.setText(getString(R.string.update_download_progress,
                    StringGenerator.bytesToMegabytes(context, downloaded),
                    StringGenerator.bytesToMegabytes(context, buildSize),
                    NumberFormat.getPercentInstance().format((float) downloaded / buildSize)));
        }

        private void onReceiveUpdateInstallProgress(Context context, Intent intent) {
            long progress = intent.getLongExtra("installProgress", 0);
            long total = intent.getLongExtra("installMax", 1);

            pbProgressBar.setIndeterminate(false);
            pbProgressBar.setMax((int) total);
            pbProgressBar.setProgress((int) progress);
            tvProgressText.setText(getString(R.string.update_install_progress, progress + " %"));
        }

        private void onReceiveUpdateInfo(Context context, Intent intent) {
            String buildVersion = intent.getStringExtra("buildVersion");
            long buildDate = intent.getLongExtra("buildDate", -1);

            tvBuildVersion.setText(buildVersion);
            String buildDateString = StringGenerator.getDateLocalizedUTC(context, DateFormat.LONG, buildDate);
            tvBuildDate.setText(buildDateString);
        }
    }
}
