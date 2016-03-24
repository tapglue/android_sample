package com.tapglue.exampleapp_v2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import com.tapglue.exampleapp_v2.download.DownloadBitmapToViewTask;
import com.tapglue.exampleapp_v2.download.DownloadingDrawable;
import com.tapglue.exampleapp_v2.download.PictureCache;
import com.tapglue.Tapglue;
import com.tapglue.model.TGImage;
import com.tapglue.model.TGUser;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;


public class
WithMenuActivity extends AppCompatActivity {

    private long currentActivity;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == currentActivity) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_events: {
                startActivity(new Intent(this, FeedActivity.class));
                finish();
                return true;
            }
            case R.id.menu_posts: {
                startActivity(new Intent(this, PostsActivity.class));
                finish();
                return true;
            }
            case R.id.menu_friends: {
                startActivity(new Intent(this, FriendsActivity.class));
                finish();
                return true;
            }
            case R.id.menu_profile: {
                startActivity(new Intent(this, EditProfileActivity.class));
                finish();
                return true;
            }
            case R.id.logout: {
                Tapglue.user().logout(new TGRequestCallback<Boolean>() {
                    @Override
                    public boolean callbackIsEnabled() {
                        return true;
                    }

                    @Override
                    public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                        Toast.makeText(WithMenuActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRequestFinished(Boolean object, boolean b) {
                        if (object) {
                            startActivity(new Intent(WithMenuActivity.this, LoginActivity.class));
                            finish();
                            Toast.makeText(WithMenuActivity.this, R.string.toast_loggedout, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return true;
            }
        }
        return false;
    }

    protected void setUserPic(TGUser user, ImageView userPhoto) {
        HashMap<String, TGImage> images = user.getImages();
        if (images != null) {
            TGImage image = images.get("UserPic");
            if (image != null) {
                downloadPicture(image.getURL(), userPhoto, R.drawable.userpic);
            }
        }
    }

    /*
     * Asynchronously downloads picture, sets default if error occurs
     */
    protected void downloadPicture(String url, ImageView imageView, int defaultPictureRes) {
        if (cancelPotentialDownload(url, imageView)) {
            if (PictureCache.isInCache(url)) {
                imageView.setImageBitmap(PictureCache.getFromCache(url));
                return;
            }
            DownloadBitmapToViewTask downloadTask = new DownloadBitmapToViewTask(imageView, defaultPictureRes);
            DownloadingDrawable downloadingDrawable = new DownloadingDrawable(downloadTask);
            imageView.setImageDrawable(downloadingDrawable);
            try {
                downloadTask.execute(new URL(url));
            } catch (MalformedURLException e) {
                Toast.makeText(WithMenuActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                imageView.setImageResource(defaultPictureRes);
            }
        }
    }

    /*
     * Cancels download if another one started
     */
    protected boolean cancelPotentialDownload(String url, ImageView imageView) {
        DownloadBitmapToViewTask bitmapDownloaderTask = DownloadBitmapToViewTask.getDownloadBitmapToViewTask(imageView);
        if (bitmapDownloaderTask != null && bitmapDownloaderTask.getUrl() != null) {
            String bitmapUrl = bitmapDownloaderTask.getUrl().toString();
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    public void setCurrentActivity(long currentActivity) {
        this.currentActivity = currentActivity;
    }


}
