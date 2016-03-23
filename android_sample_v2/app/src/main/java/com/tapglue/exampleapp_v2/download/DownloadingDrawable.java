package com.tapglue.exampleapp_v2.download;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.lang.ref.WeakReference;

public class DownloadingDrawable extends ColorDrawable {
    private final WeakReference<DownloadBitmapToViewTask> weakTask;

    public DownloadingDrawable(DownloadBitmapToViewTask task) {
        super(Color.BLACK);
        weakTask = new WeakReference<>(task);
    }

    public DownloadBitmapToViewTask getBitmapDownloaderTask() {
        return weakTask.get();
    }
}
