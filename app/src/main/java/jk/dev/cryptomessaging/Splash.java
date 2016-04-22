package jk.dev.cryptomessaging;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import jk.dev.cryptomessaging.Utilities.KeystoreManager;
import jk.dev.cryptomessaging.Utilities.Preferences;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        new Timer().schedule(new TimerTask() {
            public void run() {
                startActivity(new Intent(Splash.this, Connection.class));
            }
        }, 2500);

        KeystoreManager ksm = new KeystoreManager(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();


    }
}
