package jk.dev.cryptomessaging.Utilities;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by aris on 4/23/16.
 */
public class AlgoCrypt {
    String TAG = "AlgoCrypt";
    String algorithm;
    String transformation;
    SecretKey secretKey;
    Cipher cipher;
    byte[] iv;

    AlgoCrypt(KeyAgreement keyAgreement, PublicKey othersDHPK, String algorithm, String transformation){
        this.algorithm = algorithm;
        this.transformation = transformation;
        try {
            //begin new phase from DH KeyAgreement
            keyAgreement.doPhase(othersDHPK,true);
            // secret key of algorithm of choice
            this.secretKey = keyAgreement.generateSecret(this.algorithm);
            String keystr = Base64.encodeToString(secretKey.getEncoded(),Base64.DEFAULT);
            Log.d("DH",algorithm + " SECRET KEY: "+ keystr);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Log.d(TAG,Log.getStackTraceString(e));
        }
    }

    byte[] encrypt(byte[] cleartext){
        try {
            cipher = Cipher.getInstance(transformation,"BC");
            Log.d(TAG,"using " + cipher.getAlgorithm() + "/" + cipher.getBlockSize()+ "/" + cipher.getProvider() + " to encrypt");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//            AlgorithmParameters params = cipher.getParameters();
//            iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            iv = cipher.getParameters().getEncoded();

            byte[] ciphertext = cipher.doFinal(cleartext);
            return ciphertext;
        } catch (IllegalBlockSizeException | BadPaddingException | IOException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | NoSuchProviderException e) {
            Log.d(TAG,Log.getStackTraceString(e));
        }
        return null;
    }

    byte[] decrypt(byte[] ciphertext, byte[] iv){
        try {
//            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            AlgorithmParameters params = AlgorithmParameters.getInstance(algorithm);
            params.init(iv);
            cipher = Cipher.getInstance(transformation,"BC");
            Log.d(TAG,"using " + cipher.getAlgorithm() + "/" + cipher.getBlockSize()+ "/" + cipher.getProvider() + " to decrypt");
            cipher.init(Cipher.DECRYPT_MODE,secretKey,params);
            byte[] cleartext = cipher.doFinal(ciphertext);
            return cleartext;
        }  catch (IllegalBlockSizeException | BadPaddingException | IOException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            Log.d(TAG,Log.getStackTraceString(e));
        }
        return null;
    }

    public boolean checkHMAC(byte[] messageHMAC, byte[] message) {
        if (Arrays.equals(messageHMAC,calculateHMAC(message))){
            return true;
        }
        return false;
    }

    public byte[] calculateHMAC(byte[] message) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getEncoded(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return sha256_HMAC.doFinal(message);
        } catch (Exception e) {
            System.out.println("Error calculating HMAC");
        }
        return null;
    }

}
