package com.tapglue.exampleapp_v2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.tapglue.Tapglue;
import com.tapglue.model.TGUser;
import com.tapglue.networking.requests.TGRequestCallback;
import com.tapglue.networking.requests.TGRequestErrorType;

public class LoginActivity extends Activity {

    boolean callbacksEnabled = true;

    @Bind(R.id.login)
    EditText mLogin;

    @Bind(R.id.password)
    EditText mPassword;

    @Bind(R.id.createnew_cb)
    CheckBox mCreateNewCB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    public void onLoginClick(View view) {

        Tapglue.TGConfiguration config = new Tapglue.TGConfiguration()
                .setToken("78a62cac15972206e2b2da4f5a42c5c4")
                .setDebugMode(true);

        Tapglue.initialize(this, config);

        if (!mCreateNewCB.isChecked()) {
            //Registered user
            Tapglue.user().login(mLogin.getText().toString(),
                    mPassword.getText().toString(),
                    new TGRequestCallback<Boolean>() {
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
                            startActivity(new Intent(LoginActivity.this, FeedActivity.class));
                            finish();
                        }
                    });
        } else {
            //New user
            TGUser user = new TGUser()
                    .setUserName(mLogin.getText().toString())
                    .setPassword(mPassword.getText().toString())
                    .setEmail(mLogin.getText().toString())
                    .setFirstName(mLogin.getText().toString());

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
                    startActivity(new Intent(LoginActivity.this, FeedActivity.class));
                    finish();
                }
            });
        }
    }
}
