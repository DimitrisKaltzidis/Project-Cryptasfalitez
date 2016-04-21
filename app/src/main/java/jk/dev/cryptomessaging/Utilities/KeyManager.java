//package jk.dev.cryptomessaging.Utilities;
//
//import android.content.Context;
//import android.util.Log;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.security.KeyFactory;
//import java.security.KeyPair;
//import java.security.KeyPairGenerator;
//import java.security.KeyStore;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.PrivateKey;
//import java.security.PublicKey;
//import java.security.SecureRandom;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateException;
//import java.security.cert.CertificateFactory;
//import java.security.cert.X509Certificate;
//import java.security.spec.InvalidKeySpecException;
//import java.security.spec.X509EncodedKeySpec;
//
///**
// * Created by aris on 4/20/16.
// */
//public class KeyManager {
//
//    final String tag = "KeyManager_";
//    Context context;
//    PublicKey publicKey;
//    PrivateKey privateKey;
//    X509Certificate cert;
//    KeyStore keyStore;
//    File keystoreFile;
//    String keyStorePassword;
//    InputStream in;
//    OutputStream out;
//
//    public KeyManager() {
//        //init KeyStore
//        try {
//            keyStore = KeyStore.getInstance("BKS"); // Bouncy Castle Keystore (android istories)
//            keyStorePassword = "password";
//            keystoreFile = new File(context.getFilesDir(), "user.keystore");
//            in = new FileInputStream(keystoreFile);
//        } catch (KeyStoreException | FileNotFoundException e) {
//            Log.e(tag+"Constructor",e.getMessage());
//        }
//
//    }
//
//    private void createKeyPair() throws NoSuchAlgorithmException {
//        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
//        SecureRandom random = new SecureRandom();
//        keyGen.initialize(1024, random);
//        KeyPair keyPair = keyGen.generateKeyPair();
//        publicKey = keyPair.getPublic();
//        privateKey = keyPair.getPrivate();
//    }
//
//    private void initKeystore() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
//
//        if (keystoreFile.exists()) {
//            try {
//                keyStore.load(in, keyStorePassword.toCharArray());
//            } finally {
//                in.close();
//            }
//        }else{
//            //den iparxi keystore
//            //dimiurgia neu
//            keyStore.load(null, keyStorePassword.toCharArray());
//            createKeyPair();
//            //create certificate
//            createCert();
//            //save entry to keystore
//            keyStore.setKeyEntry("user",publicKey,null,new Certificate[]{cert});
//        }
//
//
//        //create KeyPair
//
//        PublicKey publicKey;
//        PrivateKey privateKey;
//
//        byte[] pk;
//
//
//
//        pk = keyPair.getPublic().getEncoded();
//        Log.d("PUBLIC_KEY", publicKey);
//        Log.d("encoded_PUBLIC_KEY", String.valueOf(pk));
//        KeyFactory bobKeyFac = KeyFactory.getInstance("RSA");
//        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec
//                (pk);
//        PublicKey dpk = null;
//        try {
//            dpk = bobKeyFac.generatePublic(x509KeySpec);
//        } catch (InvalidKeySpecException e) {
//            e.printStackTrace();
//        }
//        Log.d("decoded_PUBLIC_KEY", String.valueOf(dpk));
//        privateKey = keyPair.getPrivate();
//        //replace to to_public_pou eftiaxes kai to to_private_pou eftiaxes me to public kai to private antoistixa
////                Preferences.savePrefsString("PUBLIC_KEY", publicKey,getApplicationContext());
////                Preferences.savePrefsString("PRIVATE_KEY",privateKey,getApplicationContext());
//
////        }else{
//        //show keys in log
////            Log.d("PUBLIC_KEY", publicKey);
////            Log.d("PRIVATE_KEY", privateKey);
////        }
//    }
//
//    private void createCert() throws IOException, CertificateException {
//        InputStream inStream = null;
//        try {
//            inStream = new FileInputStream("user-cert");
//            CertificateFactory cf = CertificateFactory.getInstance("X.509");
//            X509Certificate cert = (X509Certificate)cf.generateCertificate(inStream);
//        } finally {
//            if (inStream != null) {
//                inStream.close();
//            }
//        }
//    }
//}
