package com.tapglue.exampleapp_v2;


import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;
import butterknife.ButterKnife;
import com.tapglue.Tapglue;
import com.tapglue.model.TGLike;
import com.tapglue.model.TGPost;
import com.tapglue.model.TGPostsList;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

import java.util.List;

public abstract class ListActivity extends WithMenuActivity {

    public abstract void loadEvents();

    public class EventsArrayAdapter<T> extends ArrayAdapter<T> {

        private int layoutResourceId;

        public EventsArrayAdapter(Context context, int layoutResourceId, List<T> events) {
            super(context, layoutResourceId, events);
            this.layoutResourceId = layoutResourceId;
        }

        protected View bind(EventsArrayAdapter adapter, View view, ViewGroup parent) {
            if (view == null) {
                LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
                view = inflater.inflate(layoutResourceId, parent, false);
                ButterKnife.bind(adapter, view);
            } else {
                ButterKnife.bind(adapter, view);
            }
            return view;
        }
    }

    public class LikeListener implements View.OnClickListener {

        private ImageButton buttonLike;
        private List<TGPost> posts;
        private int position;
        private Context context;

        public LikeListener(Context context, List<TGPost> posts, int position, ImageButton buttonLike) {
            this.context = context;
            this.posts = posts;
            this.buttonLike = buttonLike;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            TGPost post = posts.get(position);
            if (!post.getIsLiked()) {
                Tapglue.posts().likePost(post.getID(), new TGRequestCallback<TGLike>() {
                    @Override
                    public boolean callbackIsEnabled() {
                        return true;
                    }

                    @Override
                    public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                        Toast.makeText(ListActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRequestFinished(TGLike like, boolean b) {
                        buttonLike.setColorFilter(ContextCompat.getColor(context, R.color.liked_tilt));
                        updatePost();
                    }
                });
            } else {
                Tapglue.posts().unlikePost(post.getID(), new TGRequestCallback<Object>() {
                    @Override
                    public boolean callbackIsEnabled() {
                        return true;
                    }

                    @Override
                    public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                        Toast.makeText(ListActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRequestFinished(Object object, boolean b) {
                        buttonLike.setColorFilter(ContextCompat.getColor(context, R.color.comment_tools_tilt));
                        updatePost();
                    }
                });
            }
        }



        private void updatePost() {
            Tapglue.posts().getUserPosts( posts.get(position).getUserId(),new TGRequestCallback<TGPostsList>() {
                @Override
                public boolean callbackIsEnabled() {
                    return true;
                }

                @Override
                public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                    Toast.makeText(ListActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRequestFinished(final TGPostsList tgFeed, boolean changeDoneOnline) {
                    List<TGPost> feedPosts = tgFeed.getPosts();
                    for (TGPost currentPost: feedPosts) {
                        if (currentPost.getID().equals( posts.get(position).getID())) {
                            posts.set(position, currentPost);
                            break;
                        }
                    }
                }
            });
        }
    }
}
