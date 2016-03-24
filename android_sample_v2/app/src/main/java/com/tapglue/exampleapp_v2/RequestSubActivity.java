package com.tapglue.exampleapp_v2;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.tapglue.Tapglue;
import com.tapglue.model.TGConnection;
import com.tapglue.model.TGPendingConnections;
import com.tapglue.model.TGUser;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

import java.util.ArrayList;
import java.util.List;

public class RequestSubActivity extends ListActivity {
    @Bind(R.id.friends_list)
    ListView mFriendsList;

    private List<TGConnection> requests;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        ButterKnife.bind(this);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        loadEvents();
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

    @Override
    public void loadEvents() {
        Tapglue.connection().getPendingConnections(new TGRequestCallback<TGPendingConnections>() {
            @Override
            public boolean callbackIsEnabled() {
                return true;
            }

            @Override
            public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                Toast.makeText(RequestSubActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestFinished(TGPendingConnections requestsList, boolean b) {
                if (requestsList != null) {
                    requests = requestsList.getIncoming();
                } else {
                    requests = new ArrayList<>();
                }
                RequestsArrayAdapter adapter = new RequestsArrayAdapter(RequestSubActivity.this, R.layout.friend, requests);
                mFriendsList.setAdapter(adapter);

            }
        });
    }

    protected class RequestsArrayAdapter extends EventsArrayAdapter<TGConnection> {

        @Bind(R.id.friend_name)
        TextView mFriendName;

        public RequestsArrayAdapter(Context context, int layoutResourceId, List<TGConnection> requests) {
            super(context, layoutResourceId, requests);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = bind(this, convertView, parent);
            final long userId = requests.get(position).getUserFromId();
            Tapglue.user().retrieveUserWithId(userId, new TGRequestCallback<TGUser>() {
                @Override
                public boolean callbackIsEnabled() {
                    return true;
                }

                @Override
                public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                    Toast.makeText(RequestSubActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRequestFinished(TGUser user, boolean b) {
                    updateRequest(user);
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tapglue.connection()
                            .confirmConnection(userId, TGConnection.TGConnectionType.FRIEND, new TGRequestCallback<Boolean>() {
                                @Override
                                public boolean callbackIsEnabled() {
                                    return true;
                                }

                                @Override
                                public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                                    Toast.makeText(RequestSubActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onRequestFinished(Boolean aBoolean, boolean b) {
                                    mFriendsList.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(RequestSubActivity.this, "Friend request confirmed", Toast.LENGTH_SHORT).show();
                                            requests.remove(position);
                                            loadEvents();
                                        }
                                    });
                                }
                            });
                }
            });
            return convertView;
        }

        private void updateRequest(TGUser user) {
            if (user != null) {
                mFriendName.setText(user.getFirstName() + " " + user.getLastName());
            }
        }
    }
}
