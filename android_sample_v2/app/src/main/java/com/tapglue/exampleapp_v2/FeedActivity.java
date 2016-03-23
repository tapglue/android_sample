package com.tapglue.exampleapp_v2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.tapglue.Tapglue;
import com.tapglue.model.*;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

import java.util.List;


public class FeedActivity extends ListActivity {

    @Bind(R.id.events_list)
    ListView mEvents;

    private List<TGUser> users;

    private boolean callbackEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentActivity(R.id.menu_events);
        setContentView(R.layout.activity_events);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        ButterKnife.bind(this);
        loadEvents();
    }

    @Override
    public void loadEvents() {
        Tapglue.feed().retrieveNewsFeedForCurrentUser(new TGRequestCallback<TGFeed>() {
            @Override
            public boolean callbackIsEnabled() {
                return callbackEnabled;
            }

            @Override
            public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                Toast.makeText(FeedActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestFinished(final TGFeed tgFeed, boolean changeDoneOnline) {
                mEvents.post(new Runnable() {
                    @Override
                    public void run() {
                        if (tgFeed != null && tgFeed.getPosts() != null && tgFeed.getPosts().size() > 0)
                            showEvents(tgFeed.getPosts());
                        else
                            Toast.makeText(FeedActivity.this, R.string.toast_no_news, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showEvents(final List<TGPost> posts) {
        if (posts == null)
            return;

        if (users == null) {
            Tapglue.user().retrieveFriendsForCurrentUser(new TGRequestCallback<TGUsersList>() {
                @Override
                public boolean callbackIsEnabled() {
                    return true;
                }

                @Override
                public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                    Toast.makeText(FeedActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRequestFinished(TGUsersList usersList, boolean b) {
                    users = usersList.getUsers();
                    EventsArrayAdapter adapter = new FriendsPostsArrayAdapter(FeedActivity.this, R.layout.event, posts);
                    mEvents.setAdapter(adapter);
                }
            });
        } else {
            EventsArrayAdapter adapter = new FriendsPostsArrayAdapter(this, R.layout.event, posts);
            mEvents.setAdapter(adapter);
        }

    }


    protected class FriendsPostsArrayAdapter extends EventsArrayAdapter<TGPost> {

        List<TGPost> posts;

        @Bind(R.id.user_name)
        TextView userName;

        @Bind(R.id.post_date)
        TextView postDate;

        @Bind(R.id.post_text)
        TextView postText;

        @Bind(R.id.user_pic)
        ImageView userPic;

        @Bind(R.id.pic_attachment)
        ImageView attachmentPic;

        @Bind(R.id.button_like)
        ImageButton buttonLike;

        @Bind(R.id.button_comment)
        ImageButton buttonComments;

        public FriendsPostsArrayAdapter(Context context, int layoutResourceId, List<TGPost> posts) {
            super(context, layoutResourceId, posts);
            this.posts = posts;
        }

        protected TGUser getEventUser(int position) {
            TGUser eventUser = null;
            long userId = posts.get(position).getUserId();
            for (TGUser user : users) {
                if (user.getID().equals(userId)) {
                    eventUser = user;
                    break;
                }
            }
            return eventUser;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = bind(this, convertView, parent);
            TGUser eventUser = getEventUser(position);
            userName.setText("");
            userPic.setImageResource(R.drawable.userpic);
            postDate.setText("");
            attachmentPic.setImageResource(0);
            postText.setText("");
            if (eventUser != null) {
                setUserPic(eventUser, userPic);
                final TGPost post = posts.get(position);
                userName.setText(eventUser.getFirstName() + " " + eventUser.getLastName());
                postDate.setText(post.getCreatedAt());
                if (post.getAttachments() != null) {
                    for (TGAttachment attachment : post.getAttachments()) {
                        if (attachment.getType().equals("text")) {
                            postText.setText(attachment.getContent());
                        } else if (attachment.getType().equals("url")) {
                            downloadPicture(attachment.getContent(), attachmentPic, 0);
                        }
                    }
                }
                if (post.getIsLiked()) {
                    buttonLike.setColorFilter(ContextCompat.getColor(getContext(), R.color.liked_tilt));
                } else {
                    buttonLike.setColorFilter(ContextCompat.getColor(getContext(), R.color.comment_tools_tilt));
                }
                buttonLike.setOnClickListener(new LikeListener(getContext(), posts, position, buttonLike));
                buttonComments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FeedActivity.this, CommentsSubActivity.class);
                        intent.putExtra(CommentsSubActivity.POST, post);
                        startActivity(intent);
                    }
                });

            }
            return convertView;
        }
    }
}
