package cz.cvut.fel.jinocvla.naviterier.component;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Timer;
import java.util.TimerTask;

import cz.cvut.fel.jinocvla.naviterier.R;

/**
 * Created by usul on 7.3.14.
 */
public class DownloadDialog extends ProgressDialog {

    /**
     * Download ID
     */
    private final Long downloadId;

    /**
     * Download manager instance
     */
    private final DownloadManager downloadManager;

    public DownloadDialog(Context context, Long downloadId) {
        super(context);
        this.downloadId = downloadId;
        this.downloadManager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
    }

    public DownloadDialog(Context context, int theme, Long downloadId) {
        super(context, theme);
        this.downloadId = downloadId;
        this.downloadManager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setProgress(0);
        setMax(100);

        setProgressNumberFormat(null);

        // Progress bar updater
        final Timer myTimer = new Timer();
        final Handler mHandler = new Handler();

        // Receiver after download for fix download progressbar.
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    Cursor c = downloadManager.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c
                                .getColumnIndex(DownloadManager.COLUMN_STATUS);

                        final int bytes_total = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                        myTimer.cancel();
                        mHandler.post(new Runnable(){
                            @Override
                            public void run(){
                                setProgress(bytes_total);
                                setMax(bytes_total);
                                dismiss();
                            }
                        });

                    }
                }
            }
        };

        getContext().registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(downloadId);
                Cursor cursor = downloadManager.query(q);
                cursor.moveToFirst();
                final int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                final int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                cursor.close();
                if (bytes_total > 0) {
                    setProgressNumberFormat("%1d/%2d B");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setProgress(bytes_downloaded);
                            setMax(bytes_total);
                        }
                    });
                }

            }

        }, 0, 100);
    }

    @Override
    public int getProgress() {
        return super.getProgress();
    }

    @Override
    public int getMax() {
        return super.getMax();
    }
}
