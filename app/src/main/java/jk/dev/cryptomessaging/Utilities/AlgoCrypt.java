package jk.dev.cryptomessaging.Utilities;

import android.util.Base64;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * Created by aris on 4/23/16.
 */
public class AlgoCrypt {
    String algorithm;
    String transformation;
    SecretKey secretKey;
    Cipher cipher;

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
            // init cipher (padding, kulupu)
            this.cipher = Cipher.getInstance(transformation,"BC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

//    byte[] getEncryptedMessageSize(byte[] cleartext){
//        try {
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//            byte[] ciphertext = cipher.doFinal(cleartext);
////            new String();
//            return ciphertext;
//        } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
//        } catch (BadPaddingException e) {
//            e.printStackTrace();
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    byte[] encrypt(byte[] cleartext){
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] ciphertext = cipher.doFinal(cleartext);
//            new String();
            return ciphertext;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    byte[] decrypt(byte[] ciphertext){
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] cleartext = cipher.doFinal(ciphertext);
            return cleartext;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

}
