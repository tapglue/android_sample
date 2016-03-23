package com.tapglue.exampleapp_v2;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.tapglue.Tapglue;
import com.tapglue.model.TGUser;
import com.tapglue.model.TGUsersList;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

import java.util.List;

public class AddFriendsSubActivity extends FriendsActivity {
    @Nullable
    @Bind(R.id.search_text)
    EditText mSearchText;

    @Nullable
    @Bind(R.id.addfriends_list)
    ListView mFriendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mSearchText != null && mFriendsList != null) {
            mSearchText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    updateList();
                }
            });
        }
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
     * Updates list of users using mSearchField
     */
    private void updateList() {
        if ((mSearchText == null) || (mFriendsList == null)) {
            return;
        }
        String search = mSearchText.getText().toString();
        Tapglue.user().search(search, new TGRequestCallback<TGUsersList>() {
            @Override
            public boolean callbackIsEnabled() {
                return true;
            }

            @Override
            public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                Toast.makeText(AddFriendsSubActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestFinished(TGUsersList usersList, boolean b) {
                if (usersList != null) {
                    FriendsArrayAdapter adapter = new AddFriendsArrayAdapter(AddFriendsSubActivity.this, R.layout.friend, usersList.getUsers());
                    mFriendsList.setAdapter(adapter);
                }
            }
        });
    }

    public class AddFriendsArrayAdapter extends FriendsArrayAdapter {

        private List<TGUser> friends;

        public AddFriendsArrayAdapter(Context context, int layoutResourceId, List<TGUser> friends) {
            super(context, layoutResourceId, friends);
            this.friends = friends;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            final TGUser friend = friends.get(position);

            //Send connection request
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tapglue.connection().friendUser(friend.getID(), new TGRequestCallback<Boolean>() {
                        @Override
                        public boolean callbackIsEnabled() {
                            return true;
                        }

                        @Override
                        public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                            Toast.makeText(AddFriendsSubActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRequestFinished(Boolean object, boolean b) {
                            Toast.makeText(AddFriendsSubActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            return convertView;
        }
    }


}
