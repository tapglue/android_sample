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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.tapglue.Tapglue;
import com.tapglue.model.TGEvent;
import com.tapglue.model.TGFeed;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FeedActivity extends AppCompatActivity {

    boolean callbackEnabled;
    @Bind(R.id.posts_list)
    ListView mPosts;

    protected void loadData() {
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
            public void onRequestFinished(final TGFeed tgFeed, boolean b) {
                mPosts.post(new Runnable() {
                    @Override
                    public void run() {

                        if (tgFeed != null && tgFeed.getEvents() != null && tgFeed.getEvents().size() > 0) {
                            showData(tgFeed.getEvents());
                        } else {
                            Toast.makeText(FeedActivity.this, "Feed is empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        ButterKnife.bind(this);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
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
        callbackEnabled = true;
        loadData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    void showData(List<TGEvent> events) {
        List<String> items = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        if (events == null)
            return;
        for (TGEvent event : events) {
            items.add("Event from user: " + event.getUserId() + " visibility:" + event.getVisibility());
            ids.add(event.getID());
        }
        StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, items, ids);
        mPosts.setAdapter(adapter);
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
