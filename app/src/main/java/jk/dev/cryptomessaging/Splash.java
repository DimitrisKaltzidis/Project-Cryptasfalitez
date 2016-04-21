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
        }, 500);

        KeystoreManager ksm = new KeystoreManager(getApplicationContext());
        try {
            ksm.createNewKeys("you");
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    private KeyPair getKeys() throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
        PublicKey publicKey = null;
        PrivateKey privateKey = null;

//        if(Preferences.loadPrefsString("FIRST_TIME","YES",getApplicationContext()).equals("YES")){
        /// create public private key for the first time and save it to prefs
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = new SecureRandom();
            keyGen.initialize(1024, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
            //encoding and saving keys
            byte[] publicKeyEnc = publicKey.getEncoded();
            byte[] privateKeyEnc = privateKey.getEncoded();
            Preferences.savePrefsString("PUBLIC_KEY", String.valueOf(publicKeyEnc),getApplicationContext());
            Preferences.savePrefsString("PRIVATE_KEY", String.valueOf(privateKeyEnc),getApplicationContext());
        Log.d("PUBLIC_KEY saved", String.valueOf(publicKeyEnc));
        Log.d("PRIVATE_KEY saved", String.valueOf(privateKeyEnc));
//            return keyPair;
//        }else{
            String strPublicKeyEnc = Preferences.loadPrefsString("PUBLIC_KEY","not_found",getApplicationContext());
            String strPrivateKeyEnc = Preferences.loadPrefsString("PRIVATE_KEY","not_found",getApplicationContext());
        Log.d("PUBLIC_KEY loaded", String.valueOf(strPublicKeyEnc));
        Log.d("PRIVATE_KEY loaded", String.valueOf(strPrivateKeyEnc));
            //show keys in log
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] publicKeyEnc2 = strPublicKeyEnc.getBytes();
            byte[] privateKeyEnc2 = strPrivateKeyEnc.getBytes();
        Log.d("PUBLIC_KEY str to byte", String.valueOf(publicKeyEnc2));
        Log.d("PRIVATE_KEY str to byte", String.valueOf(privateKeyEnc2));
            X509EncodedKeySpec puk = new X509EncodedKeySpec(publicKeyEnc2);
            X509EncodedKeySpec prk = new X509EncodedKeySpec(privateKeyEnc2);

            PublicKey publicKey2 = keyFactory.generatePublic(puk);
            PrivateKey privateKey2 = keyFactory.generatePrivate(prk);
            Log.d("PUBLIC_KEY", String.valueOf(publicKey2));
            Log.d("PRIVATE_KEY", String.valueOf(privateKey2));
            return new KeyPair(publicKey2,privateKey2);

//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();


    }
}
