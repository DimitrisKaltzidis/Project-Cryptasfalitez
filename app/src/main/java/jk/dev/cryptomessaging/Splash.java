package jk.dev.cryptomessaging;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();


        new Timer().schedule(new TimerTask() {
            public void run() {
                startActivity(new Intent(Splash.this, Discovery.class));
            }
        }, 2500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();


    }
}
