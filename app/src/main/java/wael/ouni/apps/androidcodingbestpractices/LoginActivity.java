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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.linkedin.platform.LISession;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;

/**
 * Created by wael on 10/03/18.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LINKEDIN_TOKEN = "linkedin_token";
    private static final String FACEBOOK_TOKEN = "facebook_token";
    private static final int RC_SIGN_IN = 11;
    private static final String GOOGLE_ACCOUNT = "google_account";
    CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;

    private SignInButton mLoginGoogleButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginGoogleButton = findViewById(R.id.google_login_btn);
        mLoginGoogleButton.setSize(SignInButton.SIZE_STANDARD);
        mLoginGoogleButton.setOnClickListener(this);
        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

// Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

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

    }


    @Override
    protected void onStart() {
        super.onStart();
        CheckValidAccountIsConnected();

    }

    private void CheckValidAccountIsConnected() {
        // Check for existing Google Sign In account, if the user is already signed in
// the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        boolean loggedIn = AccessToken.getCurrentAccessToken() != null || account != null ||
                LISessionManager.getInstance(getApplicationContext()).getSession().isValid();
        if (loggedIn) {
            GoToHomeScreen();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CheckValidAccountIsConnected();
        if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        } else // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                // The Task returned from this call is always completed, no need to attach
                // a listener.

//                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//                handleSignInResult(task);
            } else {
                LISessionManager.getInstance(getApplicationContext()).onActivityResult(this,
                        requestCode, resultCode, data);
            }

        super.onActivityResult(requestCode, resultCode, data);
    }
//
//    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
//        try {
//            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
//            // Signed in successfully, show authenticated UI.
//            GoToHomeScreen(account);
//        } catch (ApiException e) {
//            // The ApiException status code indicates the detailed failure reason.
//            // Please refer to the GoogleSignInStatusCodes class reference for more information.
////            updateUI(null);
//        }
//    }


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

            case R.id.google_login_btn:
                signIn();
                break;
        }
    }

    private void GoToHomeScreen(AccessToken accessToken) {
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        homeIntent.putExtra(FACEBOOK_TOKEN, accessToken);
        startActivity(homeIntent);
        finish();
    }

    private void GoToHomeScreen(com.linkedin.platform.AccessToken accessToken) {
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        homeIntent.putExtra(LINKEDIN_TOKEN, accessToken);
        startActivity(homeIntent);
        finish();
    }

    private void GoToHomeScreen(GoogleSignInAccount account) {
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        homeIntent.putExtra(GOOGLE_ACCOUNT, account);
        startActivity(homeIntent);
        finish();
    }

    private void GoToHomeScreen() {
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(homeIntent);
        finish();
    }


}
