package wael.ouni.apps.androidcodingbestpractices;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;

import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by wael on 10/03/18.
 */

public class HomeActivity extends AppCompatActivity {

    public static final String SIGN_OUT_ARG = "sign_out";

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getTokenAccount();
        linkededinApiHelper();

        // Set a Toolbar to replace the ActionBar.
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();

        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content_home,
                new FirstFragment()).commit();

        // Find our drawer view
        mDrawer = findViewById(R.id.drawer_layout);

        // Find our drawer view
        nvDrawer = findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);
    }


    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass = FirstFragment.class;
        switch (menuItem.getItemId()) {
            case R.id.nav_first_fragment:
                fragmentClass = FirstFragment.class;
                break;
            case R.id.nav_second_fragment:
                fragmentClass = SecondFragment.class;
                break;
            case R.id.nav_third_fragment:
                fragmentClass = ThirdFragment.class;
                break;
            case R.id.nav_log_out:
                signOut();
                break;
            default:
                fragmentClass = FirstFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_home, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    public void signOut() {
        Intent signOutIntent = new Intent(this, LoginActivity.class);
        signOutIntent.putExtra(SIGN_OUT_ARG, true);
        startActivity(signOutIntent);
    }


    private static final String host = "api.linkedin.com";
    private static final String url = "https://" + host
            + "/v1/people/~:" +
            "(email-address,formatted-name,phone-numbers,picture-urls::(original))";

    public void linkededinApiHelper() {
        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(HomeActivity.this, url, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse result) {
                try {
                    showResult(result.getResponseDataAsJson());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onApiError(LIApiError error) {

            }
        });
    }

    public void showResult(JSONObject response) {

        try {

            Timber.v("Login:%s", response.get("emailAddress").toString());
            Timber.v("Login:%s", response.get("formattedName").toString());
            Timber.v("Login:%s", response.getString("pictureUrl"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
