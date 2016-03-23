package com.tapglue.exampleapp_v2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.tapglue.Tapglue;
import com.tapglue.model.TGAttachment;
import com.tapglue.model.TGPost;
import com.tapglue.model.TGPostsList;
import com.tapglue.model.TGUser;
import com.tapglue.networking.TGCustomCacheObject;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class PostsActivity extends FeedActivity {

    @Bind(R.id.events_list)
    ListView mPosts;

    @Nullable
    @Bind(R.id.button_post)
    Button mPostButton;

    @Nullable
    @Bind(R.id.edit_post)
    EditText mEditPost;

    @Nullable
    @Bind(R.id.attach_button)
    ImageView mAttachButton;

    @Nullable
    @Bind(R.id.post_image)
    ImageView mPostImage;

    private boolean callbackEnabled = true;
    private String attachmentURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentActivity(R.id.menu_posts);
        setContentView(R.layout.activity_posts);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        ButterKnife.bind(this);
        if (mPostButton == null || mEditPost == null || mAttachButton == null || mPostImage == null) return;

        mAttachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachmentURL = "";
                LayoutInflater inflater = PostsActivity.this.getLayoutInflater();
                final View selectPicView = inflater.inflate(R.layout.popup_selectimage, null);
                final EditText urlEdit = (EditText) selectPicView.findViewById(R.id.url_edit);
                AlertDialog.Builder builder = new AlertDialog.Builder(PostsActivity.this);
                builder.setTitle(R.string.enter_image_url)
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                URL url = null;
                                try {
                                    String urlText = urlEdit.getText().toString();
                                    url = new URL(urlText);
                                } catch (MalformedURLException e) {
                                    Toast.makeText(PostsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                if (url != null) {
                                    attachmentURL = url.toString();
                                    downloadPicture(attachmentURL, mPostImage, 0);
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .setView(selectPicView);
                builder.create().show();
            }
        });

        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TGPost post = new TGPost();
                TGAttachment textAttachment = new TGAttachment(TGCustomCacheObject.TGCacheObjectType.Post);
                textAttachment.setName("Post");
                textAttachment.setContent(mEditPost.getText().toString());
                textAttachment.setType("text");
                List<TGAttachment> attachments = new ArrayList<>();
                attachments.add(textAttachment);
                if (attachmentURL != null) {
                    TGAttachment picAttachment = new TGAttachment(TGCustomCacheObject.TGCacheObjectType.Post);
                    picAttachment.setName("Attachment");
                    picAttachment.setContent(attachmentURL);
                    picAttachment.setType("url");
                    attachments.add(picAttachment);
                }
                post.setAttachments(attachments);
                Tapglue.posts().createPost(post, new TGRequestCallback<TGPost>() {
                    @Override
                    public boolean callbackIsEnabled() {
                        return true;
                    }

                    @Override
                    public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                        Toast.makeText(PostsActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRequestFinished(TGPost object, boolean b) {
                        mEditPost.setText("");
                        mPostImage.setImageResource(0);
                        loadEvents();
                    }
                });
            }
        });
        loadEvents();
    }


    @Override
    public void loadEvents() {
        Tapglue.feed().retrievePostsFeedForCurrentUser(new TGRequestCallback<TGPostsList>() {
            @Override
            public boolean callbackIsEnabled() {
                return callbackEnabled;
            }

            @Override
            public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                Toast.makeText(PostsActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestFinished(final TGPostsList postsList, boolean changeDoneOnline) {
                mPosts.post(new Runnable() {
                    @Override
                    public void run() {
                        if (postsList != null && postsList.getPosts() != null && postsList.getPosts().size() > 0)
                            showEvents(postsList.getPosts());
                        else
                            Toast.makeText(PostsActivity.this, R.string.toast_no_posts, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showEvents(final List<TGPost> posts) {
        if (posts == null)
            return;
        EventsArrayAdapter adapter = new CurrentUserPostsArrayAdapter(this, R.layout.event, posts);
        mPosts.setAdapter(adapter);
    }

    public class CurrentUserPostsArrayAdapter extends FriendsPostsArrayAdapter {

        public CurrentUserPostsArrayAdapter(Context context, int layoutResourceId, List<TGPost> posts) {
            super(context, layoutResourceId, posts);
        }

        @Override
        protected TGUser getEventUser(int position) {
            return Tapglue.user().getCurrentUser();
        }
    }
}
