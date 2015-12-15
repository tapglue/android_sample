/*
 * Copyright (c) 2015 Tapglue (https://www.tapglue.com/). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tapglue.exampleapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tapglue.Tapglue;
import com.tapglue.model.TGConnectionUser;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

public class UserDetailsActivity extends AppCompatActivity {

    private static final String INTENTEXTRA_USER = "USER";
    /**
     * User data to show
     */
    @State
    TGConnectionUser mData;
    @Bind(R.id.follow)
    Button mFollow;
    @Bind(R.id.friend)
    Button mFriend;
    @Bind(R.id.isfollowing)
    TextView mIsFollowing;
    @Bind(R.id.name)
    TextView mName;
    @Bind(R.id.surname)
    TextView mSurname;
    @Bind(R.id.username)
    TextView mUsername;
    private boolean callbackEnabled;

    public static Intent showUserDetails(Context ctx, TGConnectionUser user) {
        Intent output = new Intent(ctx, UserDetailsActivity.class);
        output.putExtra(INTENTEXTRA_USER, user);
        return output;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setContentView(R.layout.activity_user_details);
        mData = (TGConnectionUser) getIntent().getExtras().getSerializable(INTENTEXTRA_USER);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        callbackEnabled = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        callbackEnabled = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void updateFollowInfo() {
        if (mData.isFollowed()) {
            mFollow.setText(R.string.unfollow);
            mFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Tapglue.connections().unFollowUser(mData.getID(), new TGRequestCallback<Boolean>() {
                        @Override
                        public boolean callbackIsEnabled() {
                            return callbackEnabled;
                        }

                        @Override
                        public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                            Toast.makeText(UserDetailsActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRequestFinished(Boolean aBoolean, boolean b) {
                            mData.setIsFollowed(false);
                            v.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateFollowInfo();
                                }
                            });
                        }
                    });
                }
            });
        } else {
            mFollow.setText(R.string.follow);
            mFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Tapglue.connections().followUser(mData.getID(), new TGRequestCallback<Boolean>() {
                        @Override
                        public boolean callbackIsEnabled() {
                            return callbackEnabled;
                        }

                        @Override
                        public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                            Toast.makeText(UserDetailsActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRequestFinished(Boolean aBoolean, boolean b) {
                            mData.setIsFollowed(true);
                            v.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateFollowInfo();
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    private void updateUI() {
        if (mData == null) {
            mFollow.setEnabled(false);
            mFriend.setEnabled(false);
            return;
        }
        mName.setText(!TextUtils.isEmpty(mData.getFirstName()) ? mData.getFirstName() : "Unknown");
        mSurname.setText(!TextUtils.isEmpty(mData.getLastName()) ? mData.getLastName() : "Unknown");
        mUsername.setText(!TextUtils.isEmpty(mData.getUserName()) ? mData.getUserName() : "Unknown");
        mIsFollowing.setText(mData.isFollower() ? "This user is following you" : "This user is not following you");
        if (mData.isFriend()) {
            mFriend.setText(R.string.unfriend);
            mFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFriend.setEnabled(false);
                    Tapglue.connections().unFriendUser(mData.getID(), new TGRequestCallback<Boolean>() {
                        @Override
                        public boolean callbackIsEnabled() {
                            return callbackEnabled;
                        }

                        @Override
                        public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                            Toast.makeText(UserDetailsActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                            mFriend.setEnabled(true);
                        }

                        @Override
                        public void onRequestFinished(Boolean aBoolean, boolean b) {
                            mFriend.post(new Runnable() {
                                @Override
                                public void run() {
                                    mFriend.setEnabled(true);
                                    mFriend.setText(R.string.addfriend);
                                    mFriend.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Tapglue.connections().friendUser(mData.getID(), new TGRequestCallback<Boolean>() {
                                                @Override
                                                public boolean callbackIsEnabled() {
                                                    return callbackEnabled;
                                                }

                                                @Override
                                                public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                                                    Toast.makeText(UserDetailsActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onRequestFinished(Boolean aBoolean, boolean b) {
                                                    Toast.makeText(UserDetailsActivity.this, "Pending friend request created successfully!", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            mFriend.setText(R.string.pendingfriendrequests);
                                            mFriend.setEnabled(false);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });
        } else {
            mFriend.setText(R.string.addtofriends);
            mFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tapglue.connections().friendUser(mData.getID(), new TGRequestCallback<Boolean>() {
                        @Override
                        public boolean callbackIsEnabled() {
                            return callbackEnabled;
                        }

                        @Override
                        public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                            Toast.makeText(UserDetailsActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRequestFinished(Boolean aBoolean, boolean b) {
                            Toast.makeText(UserDetailsActivity.this, "Pending friend request created successfully!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    mFriend.setText(R.string.pendindfriendrequests);
                    mFriend.setEnabled(false);
                }
            });
        }
        updateFollowInfo();
    }
}
