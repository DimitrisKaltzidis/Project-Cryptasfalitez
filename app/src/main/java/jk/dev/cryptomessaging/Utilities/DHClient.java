package jk.dev.cryptomessaging.Utilities;


import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;

/**
 * This program executes the Diffie-Hellman key agreement protocol
 * between 2 parties: Alice and Bob.
 * <p/>
 * By default, preconfigured parameters (1024-bit prime modulus and base
 * generator used by SKIP) are used.
 * If this program is called with the "-gen" option, a new set of
 * parameters is created.
 */

public class DHClient {

    //Bob
    final String bob = "DHClient";
    InputStream inputStream;
    OutputStream outputStream;
    RSAPublicKey serverPublicKey;

    public DHClient(InputStream inputStream, OutputStream outputStream, String strServerPublicKey) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.serverPublicKey = KeystoreManager.base64ToPublicKey(strServerPublicKey);
    }

    public void run(String mode) throws Exception {

        DHParameterSpec dhSkipParamSpec;

        if (mode.equals("GENERATE_DH_PARAMS")) {
            // Some central authority creates new DH parameters
            System.out.println
                    ("Creating Diffie-Hellman parameters (takes VERY long) ...");
            AlgorithmParameterGenerator paramGen
                    = AlgorithmParameterGenerator.getInstance("DH");
            paramGen.init(512);
            AlgorithmParameters params = paramGen.generateParameters();
            dhSkipParamSpec = (DHParameterSpec) params.getParameterSpec
                    (DHParameterSpec.class);
        } else {
            // use some pre-generated, default DH parameters
            System.out.println("Using SKIP Diffie-Hellman parameters");
            dhSkipParamSpec = new DHParameterSpec(skip1024Modulus,
                    skip1024Base);
        }
        /*****
         * BLUETOOTH GET
         */
        //Bob get Alice Key
        byte[] encryptedAlicePubKey = new byte[512]; //encrypted with rsa 4096
        inputStream.read(encryptedAlicePubKey);
        //decrypt with Bob's RSA private key
        byte[] alicePubKeyEnc = KeystoreManager.decryptByteArray(encryptedAlicePubKey);
        //decode alice key
        KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec
                (alicePubKeyEnc);
        PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);
        Log.d(bob,"Bob sees Alice's DH key as:" + new String(alicePubKey.getEncoded()));
        /*****
         * Bob gets the DH parameters associated with Alice's public key.
         * He must use the same parameters when he generates his own key
         * pair.
         */
        DHParameterSpec dhParamSpec = ((DHPublicKey) alicePubKey).getParams();

        // Bob creates his own DH key pair
        System.out.println("BOB: Generate DH keypair ...");
        KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
        bobKpairGen.initialize(dhParamSpec);
        KeyPair bobKpair = bobKpairGen.generateKeyPair();

        // Bob creates and initializes his DH KeyAgreement object
        System.out.println("BOB: Initialization ...");
        KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
        bobKeyAgree.init(bobKpair.getPrivate());

        /*****
         * BLUETOOTH SEND
         */
        // Bob encodes his public key, and sends it over to Alice.
        byte[] bobPubKeyEnc = bobKpair.getPublic().getEncoded();
        Log.d(bob,"bob DH key: " + new String(bobPubKeyEnc));
        // encrypt Bob DH key with Alice RSA public key
        byte[] encryptedBobPubKey = KeystoreManager.encryptByteArray(serverPublicKey,bobPubKeyEnc);
        outputStream.write(encryptedBobPubKey);

        /*
         * Bob uses Alice's public key for the first (and only) phase
         * of his version of the DH
         * protocol.
         */
        System.out.println("BOB: Execute PHASE1 ...");
        bobKeyAgree.doPhase(alicePubKey, true);

        byte[] bobSharedSecret = bobKeyAgree.generateSecret();

        System.out.println("Bob secret: " +
                toHexString(bobSharedSecret));

    }

    /*
     * Converts a byte to hex digit and writes to the supplied buffer
     */
    private void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /*
     * Converts a byte array to hex string
     */
    private String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();

        int len = block.length;

        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len - 1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }

    /*
     * Prints the usage of this test.
     */
    private void usage() {
        System.err.print("DHKeyAgreement usage: ");
        System.err.println("[-gen]");
    }

    // The 1024 bit Diffie-Hellman modulus values used by SKIP
    private static final byte skip1024ModulusBytes[] = {
            (byte) 0xF4, (byte) 0x88, (byte) 0xFD, (byte) 0x58,
            (byte) 0x4E, (byte) 0x49, (byte) 0xDB, (byte) 0xCD,
            (byte) 0x20, (byte) 0xB4, (byte) 0x9D, (byte) 0xE4,
            (byte) 0x91, (byte) 0x07, (byte) 0x36, (byte) 0x6B,
            (byte) 0x33, (byte) 0x6C, (byte) 0x38, (byte) 0x0D,
            (byte) 0x45, (byte) 0x1D, (byte) 0x0F, (byte) 0x7C,
            (byte) 0x88, (byte) 0xB3, (byte) 0x1C, (byte) 0x7C,
            (byte) 0x5B, (byte) 0x2D, (byte) 0x8E, (byte) 0xF6,
            (byte) 0xF3, (byte) 0xC9, (byte) 0x23, (byte) 0xC0,
            (byte) 0x43, (byte) 0xF0, (byte) 0xA5, (byte) 0x5B,
            (byte) 0x18, (byte) 0x8D, (byte) 0x8E, (byte) 0xBB,
            (byte) 0x55, (byte) 0x8C, (byte) 0xB8, (byte) 0x5D,
            (byte) 0x38, (byte) 0xD3, (byte) 0x34, (byte) 0xFD,
            (byte) 0x7C, (byte) 0x17, (byte) 0x57, (byte) 0x43,
            (byte) 0xA3, (byte) 0x1D, (byte) 0x18, (byte) 0x6C,
            (byte) 0xDE, (byte) 0x33, (byte) 0x21, (byte) 0x2C,
            (byte) 0xB5, (byte) 0x2A, (byte) 0xFF, (byte) 0x3C,
            (byte) 0xE1, (byte) 0xB1, (byte) 0x29, (byte) 0x40,
            (byte) 0x18, (byte) 0x11, (byte) 0x8D, (byte) 0x7C,
            (byte) 0x84, (byte) 0xA7, (byte) 0x0A, (byte) 0x72,
            (byte) 0xD6, (byte) 0x86, (byte) 0xC4, (byte) 0x03,
            (byte) 0x19, (byte) 0xC8, (byte) 0x07, (byte) 0x29,
            (byte) 0x7A, (byte) 0xCA, (byte) 0x95, (byte) 0x0C,
            (byte) 0xD9, (byte) 0x96, (byte) 0x9F, (byte) 0xAB,
            (byte) 0xD0, (byte) 0x0A, (byte) 0x50, (byte) 0x9B,
            (byte) 0x02, (byte) 0x46, (byte) 0xD3, (byte) 0x08,
            (byte) 0x3D, (byte) 0x66, (byte) 0xA4, (byte) 0x5D,
            (byte) 0x41, (byte) 0x9F, (byte) 0x9C, (byte) 0x7C,
            (byte) 0xBD, (byte) 0x89, (byte) 0x4B, (byte) 0x22,
            (byte) 0x19, (byte) 0x26, (byte) 0xBA, (byte) 0xAB,
            (byte) 0xA2, (byte) 0x5E, (byte) 0xC3, (byte) 0x55,
            (byte) 0xE9, (byte) 0x2F, (byte) 0x78, (byte) 0xC7
    };

    // The SKIP 1024 bit modulus
    private static final BigInteger skip1024Modulus
            = new BigInteger(1, skip1024ModulusBytes);

    // The base used with the SKIP 1024 bit modulus
    private static final BigInteger skip1024Base = BigInteger.valueOf(2);
}