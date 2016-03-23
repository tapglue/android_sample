package com.tapglue.exampleapp_v2.download;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadBitmapToViewTask extends AsyncTask<URL, Void, Bitmap> {

    private final WeakReference<ImageView> mPicture;
    private int defaultPicResource;
    private URL url;
    private int size = 200;

    public DownloadBitmapToViewTask(ImageView mUserPhoto, int defaultPicResource) {
        this.mPicture = new WeakReference<>(mUserPhoto);
        this.defaultPicResource = defaultPicResource;
    }

    public DownloadBitmapToViewTask(ImageView mUserPhoto, int defaultPicResource, int size) {
        this.mPicture = new WeakReference<>(mUserPhoto);
        this.defaultPicResource = defaultPicResource;
        this.size = size;
    }

    @Override
    protected Bitmap doInBackground(URL... urls) {
        Bitmap bitmap = null;
        try {
            url = urls[0];
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-agent", "Mozilla/4.0");
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = scale(BitmapFactory.decodeStream(input), size);
        } catch (Exception e) {

        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        ImageView imageView = mPicture.get();
        if (imageView !=null) {
            DownloadBitmapToViewTask downloadTask = getDownloadBitmapToViewTask(imageView);
            if (this != downloadTask) {
                return;
            }
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                PictureCache.putInCache(url.toString(), bitmap);
            } else {
                imageView.setImageResource(defaultPicResource);
            }
        }
    }

    public static DownloadBitmapToViewTask getDownloadBitmapToViewTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadingDrawable) {
                DownloadingDrawable downloadedDrawable = (DownloadingDrawable)drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    public URL getUrl() {
        return url;
    }

    private Bitmap scale(Bitmap originalImage, int scaleSize) {
        int height = originalImage.getHeight();
        int width = originalImage.getWidth();
        float scale = height > width ? (float) height / scaleSize : (float) width / scaleSize;
        height = (int) (height / scale);
        width = (int) (width / scale);
        return Bitmap.createScaledBitmap(originalImage, width, height, false);
    }
}