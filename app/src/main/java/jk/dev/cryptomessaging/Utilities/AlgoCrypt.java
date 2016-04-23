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
            Log.d("DH","SECRET KEY MUST BE THE SAME: "+ keystr);
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

    int getEncryptedMessageSize(int msgSize){
        try {
            //padding 000
            byte[] msg = new byte[msgSize];
            byte[] cleartext = Arrays.copyOf(msg,msgSize);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] ciphertext = cipher.doFinal(cleartext);
//            new String();
            return ciphertext.length;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return 0;
    }

    byte[] encrypt(byte[] input){
        try {
            //padding 000
            byte[] cleartext = Arrays.copyOf(input,100);
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
            // trim zeros at the end
            byte[] trimmed = null;
            for (int i=0;i<cleartext.length;i++){
                if (cleartext[i]==0){
                    trimmed = Arrays.copyOf(cleartext,i);
                    break;
                }
            }
            return trimmed;
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
