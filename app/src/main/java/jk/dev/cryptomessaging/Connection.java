package jk.dev.cryptomessaging;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Connection extends AppCompatActivity {

    int backPressedCounter = 0;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);


    }


    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        if (backPressedCounter == 0) {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Press back again to exit", Snackbar.LENGTH_LONG);
            snackbar.show();
        }else {
            super.onBackPressed();
        }
        backPressedCounter++;
    }
}
