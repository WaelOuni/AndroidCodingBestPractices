package wael.ouni.apps.androidcodingbestpractices;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.bumptech.glide.Glide;

import timber.log.Timber;

/**
 * Created by wael on 10/03/18.
 */

public class HomeActivity extends AppCompatActivity {

    public static final String SIGN_OUT_ARG = "sign_out";

    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private NavigationView mNavigationDrawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Set a Toolbar to replace the ActionBar.
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionbar = getSupportActionBar();

        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content_home,
                new FirstFragment()).commit();

        // Find our drawer view
        mDrawerLayout = findViewById(R.id.drawer_layout);

        // Find our drawer view
        mNavigationDrawer = findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(mNavigationDrawer);

    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
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
        mDrawerLayout.closeDrawers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void signOut() {
        Intent signOutIntent = new Intent(this, LoginActivity.class);
        signOutIntent.putExtra(SIGN_OUT_ARG, true);
        startActivity(signOutIntent);
    }
}
