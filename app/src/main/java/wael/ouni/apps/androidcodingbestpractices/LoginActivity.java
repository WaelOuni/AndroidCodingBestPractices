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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
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
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISession;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

/**
 * Login interface
 * Sign in facebook
 * Sign in linkedin
 * sign in GooglePlus
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String LINKEDIN_TOKEN = "linkedin_token";
    public static final String FACEBOOK_TOKEN = "facebook_token";
    public static final String GOOGLE_ACCOUNT = "google_account";
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
                                    //Update ui after sign out google
                                }
                            });
                } else if (AccessToken.getCurrentAccessToken() != null) {
                    //disconnect facebook
                    LoginManager.getInstance().logOut();
                } else if (LISessionManager.getInstance(getApplicationContext()).getSession().isValid()) {
                    //disconnect linkedin
                    LISessionManager.getInstance(getApplicationContext()).clearSession();
                }
                //else throw new RuntimeException("Any account signed to the app");
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
        mLoginButton.setReadPermissions("public_profile");
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
            case R.id.google_login_btn:
                signInGoogle();
                break;
            case R.id.ignore:
                GoToHomeScreen();
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
        AccessToken currentFacebookAccessToken = AccessToken.getCurrentAccessToken();
        LISession session = LISessionManager.getInstance(getApplicationContext()).getSession();
        if (currentFacebookAccessToken != null) {
            GoToHomeScreen(currentFacebookAccessToken);
        } else if (account != null) {
            GoToHomeScreen(account);
        } else if (session.isValid()) {
            GoToHomeScreen(session.getAccessToken());
        } else {
//            throw new RuntimeException("Any account supported");
        }
    }

    /**
     * Sign in with facebook account
     */
    private void signInFacebook() {
        // Callback registration
        mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                // App code
                GraphRequest graphRequest = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    // Application code
                                    String id = object.getString("id");
                                    String email = object.getString("email");
                                    String name = object.getString("name");
                                    URL imageURL;

                                    String token = loginResult.getAccessToken().getToken();
                                    Timber.v("Login:accessToken %s", token);
                                    Timber.v("Login:id %s", id);
                                    Timber.v("Login:email %s", email);
                                    Timber.v("Login:name %s", name);

                                    try {
                                        imageURL = new URL("https://graph.facebook.com/me/picture?access_token=" + token);
                                        Timber.v("Login:picture %s", imageURL.toString());
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }

                                    GoToHomeScreen(loginResult.getAccessToken());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
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
        LISessionManager.getInstance(getApplicationContext()).init(this, buildScope(),
                new AuthListener() {
                    @Override
                    public void onAuthSuccess() {
                        final String host = "api.linkedin.com";
                        String url ="https://" + host+ "/v1/people/~:(email-address,formatted-name,phone-numbers,picture-urls::(original))";
                        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
                        apiHelper.getRequest(LoginActivity.this, url, new ApiListener() {
                            @Override
                            public void onApiSuccess(ApiResponse apiResponse) {
                                // Success
                                JSONObject responseDataAsJson = apiResponse.getResponseDataAsJson();
                                String response = apiResponse.getResponseDataAsString();
//                                try {
//                                    String id = responseDataAsJson.getString("id");
////                                    String email = responseDataAsJson.getString("email-address");
//                                    String name = responseDataAsJson.getString("first-name")
//                                            + " " + responseDataAsJson.getString("last-name");
//                                    String picUrl = responseDataAsJson.getString("picture-urls::(original)");
//                                    URL imageURL;
//
//                                    Timber.v("Login:id %s", id);
////                                    Timber.v("Login:email %s", email);
//                                    Timber.v("Login:name %s", name);
//                                    try {
//                                        imageURL = new URL(picUrl);
//                                        Timber.v("Login:picture %s", imageURL.toString());
//                                    } catch (MalformedURLException e) {
//                                        e.printStackTrace();
//                                    }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
                            }

                            @Override
                            public void onApiError(LIApiError liApiError) {
                                // Error making GET request!
                            }
                        });


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
        return Scope.build(Scope.R_BASICPROFILE, Scope.R_EMAILADDRESS);
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
