package wael.ouni.apps.androidcodingbestpractices;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;


/**
 * Login interface
 * Sign in facebook
 * Sign in linkedin
 * sign in GooglePlus
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private ConstraintLayout mLoginLayout;
    private Credentials credentials;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();

        setBlurBackground(mLoginLayout);
        login();
    }

    private void login() {
        Auth0 auth0 = new Auth0(this);
        auth0.setOIDCConformant(true);
        WebAuthProvider.init(auth0)
                .withScheme("demo")
                .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
                .start(LoginActivity.this, new AuthCallback() {
                    @Override
                    public void onFailure(@NonNull Dialog dialog) {
                        // Show error Dialog to user
                    }

                    @Override
                    public void onFailure(AuthenticationException exception) {
                        // Show error to user
                    }

                    @Override
                    public void onSuccess(@NonNull Credentials credentials) {
                        // Store credentials
                        // Navigate to your main activity

                    }
                });
    }

    /**
     * get ui components and assign listeners to them
     */
    private void initViews() {
        mLoginLayout = findViewById(R.id.login_layout);
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
        }
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
