package com.tapglue.exampleapp_v2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.tapglue.Tapglue;
import com.tapglue.model.TGUser;
import com.tapglue.model.TGUsersList;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends ListActivity {

    @Nullable
    @Bind(R.id.friends_list)
    ListView mFriendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentActivity(R.id.menu_friends);
        setContentView(R.layout.activity_friends);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        ButterKnife.bind(this);
        loadEvents();
    }

    @Override
    public void loadEvents() {
        Tapglue.user().retrieveFriendsForCurrentUser(new TGRequestCallback<TGUsersList>() {
            @Override
            public boolean callbackIsEnabled() {
                return true;
            }

            @Override
            public void onRequestError(TGRequestErrorType tgRequestErrorType) {

            }

            @Override
            public void onRequestFinished(TGUsersList usersList, boolean b) {
                List<TGUser> friends;
                if (usersList == null) {
                    friends = new ArrayList<>();
                } else {
                    friends = usersList.getUsers();
                }
                FriendsArrayAdapter adapter = new FriendsArrayAdapter(FriendsActivity.this, R.layout.friend, friends);

                if (mFriendsList != null) {
                    mFriendsList.setAdapter(adapter);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friends, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.add_friend: {
                startActivity(new Intent(this, AddFriendsSubActivity.class));
                return true;
            }
            case R.id.request: {
                startActivity(new Intent(this, RequestSubActivity.class));
                return true;
            }
        }
        return false;
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        super.onSupportActionModeFinished(mode);
    }

    protected class FriendsArrayAdapter extends EventsArrayAdapter<TGUser> {

        private List<TGUser> friends;

        public FriendsArrayAdapter(Context context, int layoutResourceId, List<TGUser> friends) {
            super(context, layoutResourceId, friends);
            this.friends = friends;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = bind(this, convertView, parent);
            TGUser friend = friends.get(position);
            TextView friendName = (TextView) convertView.findViewById(R.id.friend_name);
            ImageView userPic = (ImageView) convertView.findViewById(R.id.user_pic) ;
            userPic.setImageResource(R.drawable.userpic);
            setUserPic(friend, userPic);
            friendName.setText(friend.getFirstName() + " " + friend.getLastName());
            return convertView;
        }
    }
}
