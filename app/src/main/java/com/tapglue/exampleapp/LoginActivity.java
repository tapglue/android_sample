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
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.tapglue.Tapglue;
import com.tapglue.model.TGUser;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    boolean callbacksEnabled = false;
    @Bind(R.id.password)
    EditText mPassword;
    @Bind(R.id.login)
    Button mProceed;
    @Bind(R.id.register)
    CheckBox mRegister;
    @Bind(R.id.username)
    EditText mUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Tapglue.user().getCurrentUser() != null) {
            startActivity(PeopleActivity.createIntent(LoginActivity.this, PeopleActivity.ActivityMode.FRIENDS_LIST));
            finish();
        }
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRegister.isChecked()) {
                    // registration
                    TGUser user = new TGUser()
                            .setUserName(mUserName.getText().toString())
                            .setPassword(mPassword.getText().toString())
                            .setEmail(mUserName.getText().toString() + "@email.com")
                            .setFirstName(mUserName.getText().toString());

                    Tapglue.user().createAndLoginUser(user, new TGRequestCallback<Boolean>() {
                        @Override
                        public boolean callbackIsEnabled() {
                            return callbacksEnabled;
                        }

                        @Override
                        public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                            if (tgRequestErrorType.getType().equals(TGRequestErrorType.ErrorType.OTHER)) {
                                Toast.makeText(LoginActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.toast_networkerror, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onRequestFinished(Boolean success, boolean liveChange) {
                            startActivity(PeopleActivity.createIntent(LoginActivity.this, PeopleActivity.ActivityMode.FRIENDS_LIST));
                            finish();
                        }
                    });
                } else {
                    // Try to login
                    Tapglue.user().login(mUserName.getText().toString(), mPassword.getText().toString(), new TGRequestCallback<Boolean>() {
                        @Override
                        public boolean callbackIsEnabled() {
                            return callbacksEnabled;
                        }

                        @Override
                        public void onRequestError(TGRequestErrorType tgRequestErrorType) {
                            if (tgRequestErrorType.getType().equals(TGRequestErrorType.ErrorType.OTHER)) {
                                Toast.makeText(LoginActivity.this, R.string.toast_networkerror, Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(LoginActivity.this, tgRequestErrorType.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRequestFinished(Boolean aBoolean, boolean b) {
                            startActivity(PeopleActivity.createIntent(LoginActivity.this, PeopleActivity.ActivityMode.FRIENDS_LIST));
                            finish();
                        }
                    });
                }
            }
        });
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
    }
}
