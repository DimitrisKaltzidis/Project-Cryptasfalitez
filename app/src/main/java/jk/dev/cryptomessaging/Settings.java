package jk.dev.cryptomessaging;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import jk.dev.cryptomessaging.Utilities.Preferences;

public class Settings extends AppCompatActivity {

    private RadioGroup rgAlgorithms;
    private RadioButton rbAES, rbDES;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        rgAlgorithms = (RadioGroup) findViewById(R.id.rgAlgorithms);
        rbAES = (RadioButton) findViewById(R.id.rbAes);
        rbDES = (RadioButton) findViewById(R.id.rbDes);

        String cryptoAlgo = Preferences.loadPrefsString("CRYPTO_ALGO", "AES", getApplicationContext());

        if (cryptoAlgo.equals("AES"))
            rbAES.setChecked(true);
        else
            rbDES.setChecked(true);


        rgAlgorithms.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbAes) {
                    Preferences.savePrefsString("CRYPTO_ALGO", "AES", getApplicationContext());
                } else {
                    Preferences.savePrefsString("CRYPTO_ALGO", "DES", getApplicationContext());
                }
            }
        });
    }
}
