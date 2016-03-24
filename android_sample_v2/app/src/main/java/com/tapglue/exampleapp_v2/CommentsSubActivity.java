package com.tapglue.exampleapp_v2;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
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

public class CommentsSubActivity extends ListActivity {
    public static final String POST = "POST_ID";


    @Bind(R.id.button_post)
    Button mPostButton;

    @Bind(R.id.edit_post)
    EditText mEditPost;

    @Bind(R.id.user_name)
    TextView mUserName;

    @Bind(R.id.post_date)
    TextView mPostDate;

    @Bind(R.id.post_text)
    TextView mPostText;

    @Bind(R.id.user_pic)
    ImageView mUserPic;

    @Bind(R.id.button_like)
    ImageButton mButtonLike;

    @Bind(R.id.button_comment)
    ImageButton mButtonComment;

    @Bind(R.id.comments_list)
    ListView mComments;

    private TGPost post;
    private List<TGUser> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        post = (TGPost) getIntent().getExtras().get(POST);
        setContentView(R.layout.activity_comments);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mButtonComment.setVisibility(View.GONE);
        mButtonLike.setVisibility(View.GONE);
        //Send comment for the post
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TGComment comment = new TGComment();
                comment.setContent(mEditPost.getText().toString());
                String postId = post.getID();

                Tapglue.posts().createComment(postId, comment, new TGRequestCallback<TGComment>() {
                    @Override
                    public boolean callbackIsEnabled() {
                        return true;
                    }

                    @Override
                    public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                        Toast.makeText(CommentsSubActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRequestFinished(TGComment comment, boolean b) {
                        mEditPost.setText("");
                        loadEvents();
                    }
                });
            }
        });

        Tapglue.user().retrieveFriendsForCurrentUser(new TGRequestCallback<TGUsersList>() {
            @Override
            public boolean callbackIsEnabled() {
                return true;
            }

            @Override
            public void onRequestError(TGRequestErrorType tgRequestErrorType) {

            }

            @Override
            public void onRequestFinished(TGUsersList object, boolean b) {
                if (object != null) {
                    users = object.getUsers();
                }
                loadPost();
                loadEvents();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /*
     *  Loads post information
     */
    private void loadPost() {

        long userId = post.getUserId();
        TGUser eventUser = null;
        TGUser currentUser = Tapglue.user().getCurrentUser();
        if ((currentUser != null) && (currentUser.getID() == userId)) {
            eventUser = currentUser;
        } else {
            if (users != null) {
                for (TGUser user : users) {
                    if (user.getID() == userId) {
                        eventUser = user;
                        break;
                    }
                }
            }
        }
        if (eventUser != null) {
            mUserName.setText(eventUser.getFirstName() + " " + eventUser.getLastName());
            setUserPic(eventUser, mUserPic);
            mPostDate.setText(post.getCreatedAt());
            if ((post.getAttachments() != null) && (post.getAttachments().size() > 0)) {
                mPostText.setText(post.getAttachments().get(0).getContent());
            }
        }
    }

    @Override
    public void loadEvents() {
        Tapglue.posts().getPostComments(post.getID(), new TGRequestCallback<TGCommentsList>() {
            @Override
            public boolean callbackIsEnabled() {
                return true;
            }

            @Override
            public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                Toast.makeText(CommentsSubActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestFinished(final TGCommentsList commentsList, boolean changeDoneOnline) {
                mComments.post(new Runnable() {
                    @Override
                    public void run() {
                        if (commentsList != null && commentsList.getComments() != null && commentsList.getComments().size() > 0)
                            showComments(commentsList.getComments());
                        else
                            Toast.makeText(CommentsSubActivity.this, R.string.toast_no_comments, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /*
     * Shows comments for the post
     */
    private void showComments(final List<TGComment> commentsList) {
        if (commentsList == null)
            return;

        if (users == null) {
            Tapglue.user().retrieveFriendsForCurrentUser(new TGRequestCallback<TGUsersList>() {
                @Override
                public boolean callbackIsEnabled() {
                    return true;
                }

                @Override
                public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                    Toast.makeText(CommentsSubActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRequestFinished(TGUsersList object, boolean b) {
                    if (object != null) {
                        users = object.getUsers();
                    }
                    CommentsArrayAdapter adapter = new CommentsArrayAdapter(CommentsSubActivity.this, R.layout.comment, commentsList);
                    mComments.setAdapter(adapter);
                }
            });
        } else {
            CommentsArrayAdapter adapter = new CommentsArrayAdapter(CommentsSubActivity.this, R.layout.comment, commentsList);
            mComments.setAdapter(adapter);
        }
    }


    protected class CommentsArrayAdapter extends EventsArrayAdapter<TGComment> {

        List<TGComment> comments;

        @Bind(R.id.user_name)
        TextView mUserName;

        @Bind(R.id.user_pic)
        ImageView mUserPic;

        @Bind(R.id.comment_text)
        TextView mCommentText;

        public CommentsArrayAdapter(Context context, int layoutResourceId, List<TGComment> comments) {
            super(context, layoutResourceId, comments);
            this.comments = comments;

    }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = bind(this, convertView, parent);
            long userId = comments.get(position).getUserId();

            TGUser eventUser = null;
            TGUser currentUser = Tapglue.user().getCurrentUser();
            if ((currentUser != null) && (currentUser.getID() == userId)) {
                eventUser = currentUser;
            } else {
                for (TGUser user : users) {
                    if (user.getID() == userId) {
                        eventUser = user;
                        break;
                    }
                }
            }
            if (eventUser != null) {
                setUserPic(eventUser, mUserPic);
                TGComment comment = comments.get(position);
                mUserName.setText(eventUser.getFirstName() + " " + eventUser.getLastName());
                mCommentText.setText(comment.getContent());
            }
            return convertView;
        }

    }
}
