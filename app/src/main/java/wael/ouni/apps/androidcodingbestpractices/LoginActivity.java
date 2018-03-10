package wael.ouni.apps.androidcodingbestpractices;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.linkedin.platform.LISession;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;

import timber.log.Timber;

/**
 * Created by wael on 10/03/18.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LINKEDIN_TOKEN = "linkedin_token";
    private static final String FACEBOOK_TOKEN = "facebook_token";
    CallbackManager mCallbackManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        final ConstraintLayout loginLayout = findViewById(R.id.login_layout);
        setBlurBackground(loginLayout);

        findViewById(R.id.ignore).setOnClickListener(this);

        ImageView loginLinkedinButton = findViewById(R.id.linkedin_login_btn);
        loginLinkedinButton.setOnClickListener(this);

        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.facebook_login_btn);
        loginButton.setReadPermissions("email");

//        // Callback registration
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                GoToHomeScreen(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        boolean loggedIn = AccessToken.getCurrentAccessToken() == null;

        if (loggedIn) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        } else {
            LISessionManager.getInstance(getApplicationContext()).onActivityResult(this,
                    requestCode, resultCode, data);
            Timber.d("onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    // Build the list of member permissions our LinkedIn session requires
    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.W_SHARE);
    }

    private void setBlurBackground(ConstraintLayout loginLayout) {
        RenderScript renderScript = RenderScript.create(this);
        Bitmap blurBackground = new RSBlurProcessor(renderScript).blur(BitmapUtils.drawableToBitmap
                (getResources().getDrawable(R.mipmap.cover_home1)), 15, 1);
        BitmapDrawable bmp = new BitmapDrawable(getResources(), blurBackground);
        loginLayout.setBackground(bmp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linkedin_login_btn:
                LISession session = LISessionManager.getInstance(getApplicationContext()).getSession();
                if (session.isValid()) {
                    GoToHomeScreen(session.getAccessToken());
                } else {
                    LISessionManager.getInstance(getApplicationContext()).init(this, buildScope(), new AuthListener() {
                        @Override
                        public void onAuthSuccess() {
                            // Authentication was successful.  You can now do
                            // other calls with the SDK.

                            GoToHomeScreen(LISessionManager.getInstance(getApplicationContext()).
                                    getSession().getAccessToken());
                        }

                        @Override
                        public void onAuthError(LIAuthError error) {
                            // Handle authentication errors
                        }
                    }, true);
                }
                break;
            case R.id.ignore:
                GoToHomeScreen();
                break;
        }
    }

    private void GoToHomeScreen(AccessToken accessToken) {
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        homeIntent.putExtra(FACEBOOK_TOKEN, accessToken);
        startActivity(homeIntent);
    }

    private void GoToHomeScreen(com.linkedin.platform.AccessToken accessToken) {
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        homeIntent.putExtra(LINKEDIN_TOKEN, accessToken);
        startActivity(homeIntent);
    }
    private void GoToHomeScreen() {
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(homeIntent);
    }


}
