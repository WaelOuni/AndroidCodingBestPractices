package wael.ouni.apps.androidcodingbestpractices;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by wael on 10/03/18.
 */

public class HomeActivity extends AppCompatActivity {

    public static final String SIGN_OUT_ARG = "sign_out";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void signOut(View view) {
        Intent signOutIntent = new Intent(this, LoginActivity.class);
        signOutIntent.putExtra(SIGN_OUT_ARG, true);
        startActivity(signOutIntent);
    }
}
