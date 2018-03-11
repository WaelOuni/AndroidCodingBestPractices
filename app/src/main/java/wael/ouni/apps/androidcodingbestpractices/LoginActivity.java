package wael.ouni.apps.androidcodingbestpractices;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;

/**
 * Login interface
 * Sign in facebook
 * Sign in linkedin
 * sign in GooglePlus
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LINKEDIN_TOKEN = "linkedin_token";
    private static final String FACEBOOK_TOKEN = "facebook_token";
    private static final String GOOGLE_ACCOUNT = "google_account";
    private static final int RC_SIGN_IN = 11;
    CallbackManager mCallbackManager;
    private ConstraintLayout mLoginLayout;
    private GoogleSignInClient mGoogleSignInClient;
    @SuppressWarnings("FieldCanBeLocal")
    private SignInButton mLoginGoogleButton;
    private LoginButton mLoginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        setBlurBackground(mLoginLayout);
        mCallbackManager = CallbackManager.Factory.create();
        signInFacebook();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        if (intent != null) {
            boolean booleanExtra = intent.getBooleanExtra(HomeActivity.SIGN_OUT_ARG, false);
            if (booleanExtra) {


                // Check for existing Google Sign In account, if the user is already signed in
                // the GoogleSignInAccount will be non-null.
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if (account != null) {

                    mGoogleSignInClient.signOut()
                            .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                } else if (AccessToken.getCurrentAccessToken() != null) {
                    //disconnect facebook
                    LoginManager.getInstance().logOut();
                } else if (LISessionManager.getInstance(getApplicationContext()).getSession().isValid()) {
                    //disconnect linkedin
                    LISessionManager.getInstance(getApplicationContext()).clearSession();
                } else throw new RuntimeException("Any account signed to the app");
            }
        }
        CheckValidAccountIsConnected();
    }

    /**
     * get ui components and assign listeners to them
     */
    private void initViews() {
        mLoginLayout = findViewById(R.id.login_layout);
        mLoginButton = findViewById(R.id.facebook_login_btn);
        mLoginButton.setReadPermissions("email");
        ImageView loginLinkedinButton = findViewById(R.id.linkedin_login_btn);
        loginLinkedinButton.setOnClickListener(this);

        mLoginGoogleButton = findViewById(R.id.google_login_btn);
        mLoginGoogleButton.setSize(SignInButton.SIZE_STANDARD);
        mLoginGoogleButton.setOnClickListener(this);
        findViewById(R.id.ignore).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CheckValidAccountIsConnected();
        if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        } else {
            LISessionManager.getInstance(getApplicationContext()).onActivityResult(this,
                    requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * add blur effect to the background of login interface
     *
     * @param loginLayout
     */
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
                signInLinkedin();
                break;
            case R.id.ignore:
                GoToHomeScreen();
                break;
            case R.id.google_login_btn:
                signInGoogle();
                break;
        }
    }

    /**
     * Check if the user already sign with a valid account (facebook, linkedin, google+)
     */
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

    /**
     * Sign in with facebook account
     */
    private void signInFacebook() {
        // Callback registration
        mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                GoToHomeScreen(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
            }
        });
    }

    /**
     * Sign in with linkedin account
     */
    private void signInLinkedin() {
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

    /**
     * Sign in with googlePlus account
     */
    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Build the list of member permissions our LinkedIn session requires
    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.W_SHARE);
    }

    /**
     * Redirect to Home screen with facebook access token
     *
     * @param accessToken
     */
    private void GoToHomeScreen(AccessToken accessToken) {
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        homeIntent.putExtra(FACEBOOK_TOKEN, accessToken);
        startActivity(homeIntent);
        finish();
    }

    /**
     * Redirect to Home screen with linkedin access token
     *
     * @param accessToken
     */
    private void GoToHomeScreen(com.linkedin.platform.AccessToken accessToken) {
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        homeIntent.putExtra(LINKEDIN_TOKEN, accessToken);
        startActivity(homeIntent);
        finish();
    }

    /**
     * Redirect to Home screen with googlePlus account
     *
     * @param account
     */
    private void GoToHomeScreen(GoogleSignInAccount account) {
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        homeIntent.putExtra(GOOGLE_ACCOUNT, account);
        startActivity(homeIntent);
        finish();
    }

    /**
     * Redirect to Home screen
     */
    private void GoToHomeScreen() {
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(homeIntent);
        finish();
    }

}
