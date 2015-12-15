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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tapglue.Tapglue;
import com.tapglue.model.TGConnection;
import com.tapglue.model.TGConnectionUser;
import com.tapglue.model.TGConnectionUsersList;
import com.tapglue.model.TGPendingConnections;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

public class PeopleActivity extends AppCompatActivity {

    private static final String INTENTEXTRA_MODE = "MODE";
    boolean callbacksEnabled = false;
    /**
     * This variable determines activity components
     */
    @State
    ActivityMode mCurrentActivityMode = ActivityMode.FRIENDS_LIST;
    @State
    String mCurrentSearchPhrase = null;
    TGConnectionUsersList mData;
    @Bind(R.id.mode)
    TextView mMode;
    @State
    Boolean mNeedForUpdate = true;
    @Bind(R.id.people_list)
    ListView mPeopleList;
    @Bind(R.id.search_area)
    View mSearchArea;
    @Bind(R.id.search_button)
    ImageButton mSearchButton;
    @Bind(R.id.search_text)
    TextView mSearchText;
    private TGPendingConnections mPendingData;

    /**
     * Mode of this activity
     */
    public enum ActivityMode {
        FRIENDS_LIST, FOLLOWERS, FOLLOWS, PENDING_FRIENDS, SEARCH_NEW_FRIENDS
    }

    public static Intent createIntent(Context context, ActivityMode mode) {
        Intent intent = new Intent(context, PeopleActivity.class);
        intent.putExtra(INTENTEXTRA_MODE, mode);
        return intent;
    }

    private void loadData() {
        switch (mCurrentActivityMode) {
            case PENDING_FRIENDS:
                Tapglue.connections().getPendingConnections(new TGRequestCallback<TGPendingConnections>() {
                    @Override
                    public boolean callbackIsEnabled() {
                        return callbacksEnabled;
                    }

                    @Override
                    public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                        Toast.makeText(PeopleActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRequestFinished(TGPendingConnections tgPendingConnections, boolean b) {
                        mPendingData = tgPendingConnections;
                        populateListData();
                    }
                });
                break;
            case FRIENDS_LIST:
                Tapglue.feed().retrieveFriendsForCurrentUser(new TGRequestCallback<TGConnectionUsersList>() {
                    @Override
                    public boolean callbackIsEnabled() {
                        return callbacksEnabled;
                    }

                    @Override
                    public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                        Toast.makeText(PeopleActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRequestFinished(final TGConnectionUsersList tgConnectionUsersList, boolean b) {
                        mSearchArea.post(new Runnable() {
                            @Override
                            public void run() {
                                mData = tgConnectionUsersList;
                                populateListData();
                            }
                        });
                    }
                });
                break;
            case FOLLOWERS:
                Tapglue.feed().retrieveFollowersForCurrentUser(new TGRequestCallback<TGConnectionUsersList>() {
                    @Override
                    public boolean callbackIsEnabled() {
                        return callbacksEnabled;
                    }

                    @Override
                    public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                        Toast.makeText(PeopleActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRequestFinished(final TGConnectionUsersList tgConnectionUsersList, boolean b) {
                        mSearchArea.post(new Runnable() {
                            @Override
                            public void run() {
                                mData = tgConnectionUsersList;
                                populateListData();
                            }
                        });
                    }
                });
                break;
            case FOLLOWS:
                Tapglue.feed().retrieveFollowsForCurrentUser(new TGRequestCallback<TGConnectionUsersList>() {
                    @Override
                    public boolean callbackIsEnabled() {
                        return callbacksEnabled;
                    }

                    @Override
                    public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                        Toast.makeText(PeopleActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRequestFinished(final TGConnectionUsersList tgConnectionUsersList, boolean b) {
                        mSearchArea.post(new Runnable() {
                            @Override
                            public void run() {
                                mData = tgConnectionUsersList;
                                populateListData();
                            }
                        });
                    }
                });
                break;
            case SEARCH_NEW_FRIENDS:
                if (!TextUtils.isEmpty(mCurrentSearchPhrase)) {
                    mSearchButton.setEnabled(false);
                    mSearchText.setEnabled(false);
                    Tapglue.user().search(mCurrentSearchPhrase, new TGRequestCallback<TGConnectionUsersList>() {
                        @Override
                        public boolean callbackIsEnabled() {
                            return callbacksEnabled;
                        }

                        @Override
                        public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                            mSearchButton.setEnabled(true);
                            mSearchText.setEnabled(true);
                            Toast.makeText(PeopleActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRequestFinished(TGConnectionUsersList tgConnectionUsersList, boolean b) {
                            mData = tgConnectionUsersList;
                            mSearchArea.post(new Runnable() {
                                @Override
                                public void run() {
                                    populateListData();
                                }
                            });
                            mSearchButton.setEnabled(true);
                            mSearchText.setEnabled(true);
                        }
                    });
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        if (savedInstanceState == null) {
            mCurrentActivityMode = (ActivityMode) getIntent().getExtras().get(INTENTEXTRA_MODE);
        }
        // determine current mode
        prepareActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mCurrentActivityMode == ActivityMode.FRIENDS_LIST)
            getMenuInflater().inflate(R.menu.peoplelistmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_myevents:
                startActivity(new Intent(this, EventsActivity.class));
                return true;
            case R.id.action_feed:
                startActivity(new Intent(this, FeedActivity.class));
                return true;
            case R.id.action_pending:
                mNeedForUpdate = true;
                startActivityForResult(createIntent(this, ActivityMode.PENDING_FRIENDS), 1);
                return true;
            case R.id.action_search:
                mNeedForUpdate = true;
                startActivityForResult(createIntent(this, ActivityMode.SEARCH_NEW_FRIENDS), 1);
                return true;
            case R.id.action_followed:
                startActivityForResult(createIntent(this, ActivityMode.FOLLOWERS), 1);
                return true;
            case R.id.action_follows:
                startActivityForResult(createIntent(this, ActivityMode.FOLLOWS), 1);
                return true;
            case R.id.action_logout:
                Tapglue.user().logout(new TGRequestCallback<Boolean>() {
                    @Override
                    public boolean callbackIsEnabled() {
                        return callbacksEnabled;
                    }

                    @Override
                    public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                        if (tgRequestErrorType.getCode().intValue() == 1001) {
                            startActivity(new Intent(PeopleActivity.this, LoginActivity.class));
                            finish();
                        } else
                            Toast.makeText(PeopleActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRequestFinished(Boolean aBoolean, boolean b) {
                        startActivity(new Intent(PeopleActivity.this, LoginActivity.class));
                        finish();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        callbacksEnabled = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        callbacksEnabled = true;
        updateActivityData();
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

    private void populateListData() {
        List<String> items = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        if (mData == null && mPendingData == null)
            return;
        if (mData != null)
            for (TGConnectionUser user : mData.getUsers()) {
                items.add(user.getUserName());
                ids.add(user.getID());
            }
        else {
            // pending
            for (TGConnection item : mPendingData.getIncoming()) {
                items.add("Pending request from userID: " + item.getUserFromId());
                ids.add(item.getUserFromId());
            }
        }
        StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, items, ids);
        mPeopleList.setAdapter(adapter);
        mPeopleList.invalidate();
        mPeopleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (mData != null)
                    startActivity(UserDetailsActivity.showUserDetails(PeopleActivity.this, mData.getUsers().get(position)));
                else {
                    // confirm
                    Tapglue.connections()
                           .confirmConnection(mPendingData.getIncoming().get(position).getUserFromId(), TGConnection.TGConnectionType.FRIEND, new TGRequestCallback<Boolean>() {
                               @Override
                               public boolean callbackIsEnabled() {
                                   return callbacksEnabled;
                               }

                               @Override
                               public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                                   Toast.makeText(PeopleActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                               }

                               @Override
                               public void onRequestFinished(Boolean aBoolean, boolean b) {
                                   mPeopleList.post(new Runnable() {
                                       @Override
                                       public void run() {
                                           Toast.makeText(PeopleActivity.this, "Friend request confirmed", Toast.LENGTH_SHORT).show();
                                           mPendingData.getIncoming().remove(position);
                                           populateListData();
                                       }
                                   });
                               }
                           });
                }
            }
        });
    }

    private void prepareActivity() {
        setContentView(R.layout.activity_people);
        ButterKnife.bind(this);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (getSupportActionBar() == null) return;

        switch (mCurrentActivityMode) {
            case SEARCH_NEW_FRIENDS:
                mSearchArea.setVisibility(View.VISIBLE);
                mMode.setText(R.string.search);
                mSearchText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.toString().length() > 3)
                            mSearchButton.setEnabled(true);
                        else
                            mSearchButton.setEnabled(false);
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }
                });
                mSearchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCurrentSearchPhrase = mSearchText.getText().toString();
                        loadData();
                    }
                });
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                break;
            case FRIENDS_LIST:
                mMode.setText(R.string.friends);
                mSearchArea.setVisibility(View.GONE);
                break;
            case FOLLOWERS:
                mMode.setText(R.string.followedby);
                mSearchArea.setVisibility(View.GONE);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                break;
            case FOLLOWS:
                mMode.setText(R.string.follows);
                mSearchArea.setVisibility(View.GONE);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                break;
            case PENDING_FRIENDS:
                mMode.setText(R.string.pendingfriendrequests);
                mSearchArea.setVisibility(View.GONE);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                break;
            default:
                break;
        }

    }

    private void updateActivityData() {
        if (mCurrentActivityMode == ActivityMode.SEARCH_NEW_FRIENDS) {
            if (!TextUtils.isEmpty(mCurrentSearchPhrase))
                mSearchText.setText(mCurrentSearchPhrase);
            else
                mSearchText.setText("");
        }
        if (mNeedForUpdate) {
            // load view data
            loadData();
        } else
            // show current view data
            populateListData();
    }

    /**
     * Simple array adapter showing string data
     */
    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Long> mIdMap = new HashMap<>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects, List<Long> ids) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), ids.get(i));
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
