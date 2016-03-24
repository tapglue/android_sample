package com.tapglue.exampleapp_v2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import com.tapglue.Tapglue;
import com.tapglue.model.TGImage;
import com.tapglue.model.TGUser;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class EditProfileActivity extends WithMenuActivity {

    @Bind(R.id.userphoto)
    ImageView mUserPhoto;

    @Bind(R.id.username_edit)
    EditText mUsername;

    @Bind(R.id.firstname_edit)
    EditText mFirstName;

    @Bind(R.id.lastname_edit)
    EditText mLastName;

    @Bind(R.id.email_edit)
    EditText mEmail;

    @Bind(R.id.edit_button)
    Button mEdit;

    @Bind(R.id.friends_count)
    TextView mFriendsCount;

    @BindString(R.string.edit)
    String mEditText;

    @BindString(R.string.save)
    String mSaveText;

    private TGUser currentUser;

    private boolean editing = false;
    private String userPicUrl;


    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentActivity(R.id.menu_profile);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        currentUser = Tapglue.user().getCurrentUser();
        if (currentUser != null) {
            fillProfile();
            setUserPic(currentUser, mUserPhoto);
        }
        toggleEditListener();
        toggleUserPicListener();
    }

    private void toggleUserPicListener() {
        if (editing) {
            mUserPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userPicUrl = "";
                    LayoutInflater inflater = EditProfileActivity.this.getLayoutInflater();
                    View selectPicView = inflater.inflate(R.layout.popup_selectimage, null);
                    final EditText urlEdit = (EditText) selectPicView.findViewById(R.id.url_edit);
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                    builder.setTitle(R.string.enter_image_url)
                            .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    URL url = null;
                                    try {
                                        String urlText = urlEdit.getText().toString();
                                        url = new URL(urlText);
                                    } catch (MalformedURLException e) {
                                        Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    if (url != null) {
                                        userPicUrl = url.toString();
                                        downloadPicture(url.toString(), mUserPhoto, R.drawable.userpic);
                                    }
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .setView(selectPicView);
                    builder.create().show();
                }
            });
        } else {
            mUserPhoto.setOnClickListener(null);
        }
    }

    private void fillProfile() {
        mUsername.setText(currentUser.getUserName());
        mFirstName.setText(currentUser.getFirstName());
        mLastName.setText(currentUser.getLastName());
        mEmail.setText(currentUser.getEmail());
        mFriendsCount.setText(currentUser.getFriendCount() + " " + getResources().getString(R.string.friends));
    }

    private void toggleState() {
        editing = !editing;
        mUsername.setEnabled(editing);
        mFirstName.setEnabled(editing);
        mLastName.setEnabled(editing);
        mEmail.setEnabled(editing);
        toggleEditListener();
        toggleUserPicListener();
    }

    private void toggleEditListener() {
        if (editing) {
            mEdit.setText(mSaveText);
            mEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentUser.setUserName(mUsername.getText().toString());
                    currentUser.setFirstName(mFirstName.getText().toString());
                    currentUser.setLastName(mLastName.getText().toString());
                    currentUser.setEmail(mEmail.getText().toString());
                    if ((userPicUrl != null) && !userPicUrl.isEmpty()) {
                        TGImage userPic = new TGImage();
                        userPic.setURL(userPicUrl);
                        HashMap<String, TGImage> images = currentUser.getImages();
                        if (images == null) {
                            images = new HashMap<>();
                            currentUser.setImages(images);
                        }
                        images.put("UserPic", userPic);
                    }
                    Tapglue.user().saveChangesToCurrentUser(currentUser, new TGRequestCallback<Boolean>() {
                        @Override
                        public boolean callbackIsEnabled() {
                            return true;
                        }

                        @Override
                        public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                            Toast.makeText(EditProfileActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRequestFinished(Boolean object, boolean b) {
                            toggleState();
                        }
                    });
                }
            });
        } else {
            mEdit.setText(mEditText);
            mEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleState();
                }
            });
        }
    }
}
