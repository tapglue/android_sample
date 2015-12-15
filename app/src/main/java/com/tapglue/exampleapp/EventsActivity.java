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

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.tapglue.Tapglue;
import com.tapglue.model.TGEvent;
import com.tapglue.model.TGFeed;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

public class EventsActivity extends FeedActivity {

    @Override
    protected void loadData() {
        Tapglue.feed().retrieveEventsForCurrentUser(new TGRequestCallback<TGFeed>() {
            @Override
            public boolean callbackIsEnabled() {
                return callbackEnabled;
            }

            @Override
            public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                Toast.makeText(EventsActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestFinished(final TGFeed tgFeed, boolean b) {
                mPosts.post(new Runnable() {
                    @Override
                    public void run() {
                        if (tgFeed != null && tgFeed.getEvents() != null && tgFeed.getEvents().size() > 0)
                            showData(tgFeed.getEvents());
                        else
                            Toast.makeText(EventsActivity.this, "No events from current user", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tapglue.event().removeEvent(id, new TGRequestCallback<Object>() {
                    @Override
                    public boolean callbackIsEnabled() {
                        return callbackEnabled;
                    }

                    @Override
                    public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                        Toast.makeText(EventsActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onRequestFinished(Object o, boolean b) {
                        loadData();
                        Toast.makeText(EventsActivity.this, "Removed event, reloading events list...", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.eventsmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            TGEvent event = new TGEvent()
                    .setVisibility(TGEvent.TGEventVisibility.Private)
                    .setType("defaultType");
            Tapglue.event().createEvent(event, new TGRequestCallback<TGEvent>() {
                @Override
                public boolean callbackIsEnabled() {
                    return callbackEnabled;
                }

                @Override
                public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                    Toast.makeText(EventsActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRequestFinished(TGEvent tgEvent, boolean b) {
                    loadData();
                }
            });
            return true;
        } else if (item.getItemId() == R.id.action_add2) {
            TGEvent event = new TGEvent()
                    .setVisibility(TGEvent.TGEventVisibility.Connections)
                    .setType("defaultType");
            Tapglue.event().createEvent(event, new TGRequestCallback<TGEvent>() {
                @Override
                public boolean callbackIsEnabled() {
                    return callbackEnabled;
                }

                @Override
                public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                    Toast.makeText(EventsActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRequestFinished(TGEvent tgEvent, boolean b) {
                    loadData();
                }
            });
        } else if (item.getItemId() == R.id.action_add3) {
            TGEvent event = new TGEvent()
                    .setVisibility(TGEvent.TGEventVisibility.Public)
                    .setType("defaultType");
            Tapglue.event().createEvent(event, new TGRequestCallback<TGEvent>() {
                @Override
                public boolean callbackIsEnabled() {
                    return callbackEnabled;
                }

                @Override
                public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                    Toast.makeText(EventsActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRequestFinished(TGEvent tgEvent, boolean b) {
                    loadData();
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }
}
