package wael.ouni.apps.androidcodingbestpractices;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import timber.log.Timber;

/**
 * Created by wael on 10/03/18.
 */

public class HomeActivity extends AppCompatActivity {

    public static final String SIGN_OUT_ARG = "sign_out";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getTokenAccount();
    }

    /**
     * get token id of the account which the user sign in with
     * if he connect with google account so get the account object
     */
    private void getTokenAccount() {
        com.linkedin.platform.AccessToken linkedinToken = (com.linkedin.platform.AccessToken)
                getIntent().getSerializableExtra(LoginActivity.LINKEDIN_TOKEN);
        if (linkedinToken != null) {
            Timber.i("onCreate:linkedinToken %s", linkedinToken.getValue());
        }
        AccessToken facebookToken = getIntent().getParcelableExtra(LoginActivity.FACEBOOK_TOKEN);
        if (facebookToken != null) {
            Timber.i("onCreate:facebookToken %s", facebookToken.getToken());
        }
        GoogleSignInAccount googleAccount = getIntent().getParcelableExtra(LoginActivity.GOOGLE_ACCOUNT);
        if (googleAccount != null) {
            Timber.i("onCreate:googleAccount %s", googleAccount.getId());
        }
    }

    public void signOut(View view) {
        Intent signOutIntent = new Intent(this, LoginActivity.class);
        signOutIntent.putExtra(SIGN_OUT_ARG, true);
        startActivity(signOutIntent);
    }
}
