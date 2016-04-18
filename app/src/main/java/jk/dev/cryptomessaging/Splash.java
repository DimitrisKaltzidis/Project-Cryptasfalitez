package jk.dev.cryptomessaging;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

import jk.dev.cryptomessaging.Utilities.Preferences;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();


        if(Preferences.loadPrefsString("FIRST_TIME","YES",getApplicationContext()).equals("YES")){
            /// create public private key for the first time and save it to prefs
            // xrisimopoiese tis methodous
            //replace to to_public_pou eftiaxes kai to to_private_pou eftiaxes me to public kai to private antoistixa
            Preferences.savePrefsString("PUBLIC_KEY","to_public_pou eftiaxes",getApplicationContext());
            Preferences.savePrefsString("PRIVATE_KEY","to_private_pou eftiaxes",getApplicationContext());


        }

        new Timer().schedule(new TimerTask() {
            public void run() {
                startActivity(new Intent(Splash.this, Connection.class));
            }
        }, 2500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();


    }
}
