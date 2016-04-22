package jk.dev.cryptomessaging.Utilities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.security.KeyChain;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.security.auth.x500.X500Principal;

/**
 * Created by aris on 4/21/16.
 */
public class KeystoreManager {
    Context context;
    static final String TAG = "KeystoreManager";
    static final String CIPHER_TYPE = "RSA/ECB/PKCS1Padding";
    static final String CIPHER_PROVIDER = "AndroidOpenSSL";

    List<String> keyAliases;

    public static KeyStore keyStore;

    public KeystoreManager(Context context){
        this.context = context;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            createNewKeys();
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException e) {
            Log.e("CreatingKeys",Log.getStackTraceString(e));
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    private void refreshKeys() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        keyAliases = new ArrayList<>();
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                keyAliases.add(aliases.nextElement());
            }
        }
        catch(Exception e) {}

        for (String a:keyAliases){
            Log.d("zzz",a);
            Log.d("gg", String.valueOf(keyStore.getKey(a,"".toCharArray())));
        }

//        if(listAdapter != null)
//            listAdapter.notifyDataSetChanged();
    }

    public static String publicKeyToBase64(){
        String alias = "user";
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = null;
            privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey();
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
            return Base64.encodeToString(x509EncodedKeySpec.getEncoded(),Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PublicKey base64ToPublicKey(String base64){
        try {
            byte[] data = Base64.decode(base64,Base64.DEFAULT);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createNewKeys() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException {
        String alias = "user";
        try {
            // Create new key if needed
            if (!keyStore.containsAlias(alias)) {
                Toast.makeText(context,"Generating RSA 4096 bit public key for the first time",Toast.LENGTH_LONG).show();
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(alias)
                        .setKeySize(4096)
                        .setSubject(new X500Principal("CN="+alias))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA","AndroidKeyStore");
                generator.initialize(spec);

                KeyPair keyPair = generator.generateKeyPair();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Exception " + e.getMessage() + " occured", Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(e));
        }
        refreshKeys();
    }

    public void deleteKey(final String alias) {
        AlertDialog alertDialog =new AlertDialog.Builder(context)
                .setTitle("Delete Key")
                .setMessage("Do you want to delete the key \"" + alias + "\" from the keystore?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            keyStore.deleteEntry(alias);
                            refreshKeys();
                        } catch (KeyStoreException e) {
                            Toast.makeText(context,
                                    "Exception " + e.getMessage() + " occured",
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, Log.getStackTraceString(e));
                        } catch (UnrecoverableKeyException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    public static PublicKey getMyPublicKey(){
        KeyStore.PrivateKeyEntry privateKeyEntry = null;
        try {
            privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry("user", null);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return privateKeyEntry.getCertificate().getPublicKey();
    }

    public static byte[] encryptByteArray(PublicKey publicKey, byte[] initialText) {
        try {
            Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, inCipher);
            cipherOutputStream.write(initialText);
            cipherOutputStream.close();

            byte [] vals = outputStream.toByteArray();
            return vals;
        } catch (Exception e) {
//            Toast.makeText(context, "Exception " + e.getMessage() + " occured", Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    public static byte[] decryptByteArray(byte[] cipherText) {
        String alias = "user";
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            PrivateKey privateKey = privateKeyEntry.getPrivateKey();

            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            output.init(Cipher.DECRYPT_MODE, privateKey);

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(cipherText), output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte)nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for(int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }

            return bytes;

        } catch (Exception e) {
//            Toast.makeText(context, "Exception " + e.getMessage() + " occured", Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }
}
