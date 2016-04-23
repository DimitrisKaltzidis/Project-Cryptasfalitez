package jk.dev.cryptomessaging.Utilities;

/**
 * Created by aris on 4/17/16.
 */

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;
//import com.sun.crypto.provider.SunJCE;

/**
 * This program executes the Diffie-Hellman key agreement protocol
 * between 2 parties: Alice and Bob.
 * <p/>
 * By default, preconfigured parameters (1024-bit prime modulus and base
 * generator used by SKIP) are used.
 * If this program is called with the "-gen" option, a new set of
 * parameters is created.
 */

public class DHServer {

    //Alice
    final String alice = "DHServer";
    InputStream inputStream;
    OutputStream outputStream;
    AlgoCrypt algo;
    RSAPublicKey clientPublicKey;

    public DHServer(InputStream inputStream, OutputStream outputStream, String strclientRSAPublicKey) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.clientPublicKey = KeystoreManager.base64ToPublicKey(strclientRSAPublicKey);
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
            Log.d(alice,"Using SKIP Diffie-Hellman parameters");
            dhSkipParamSpec = new DHParameterSpec(skip1024Modulus,
                    skip1024Base);
        }

        /*
         * Alice creates her own DH key pair, using the DH parameters from
         * above
         */
        Log.d(alice,"ALICE: Generate DH keypair ...");
        KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
        aliceKpairGen.initialize(dhSkipParamSpec);
        KeyPair aliceKpair = aliceKpairGen.generateKeyPair();

        // Alice creates and initializes her DH KeyAgreement object
        Log.d(alice,"ALICE: Initialization ...");
        KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
        aliceKeyAgree.init(aliceKpair.getPrivate());

        /*****
         * BLUETOOTH SEND
         */

        // Alice encodes her public key
        byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
        Log.d(alice,"Alice DH key:" + new String(alicePubKeyEnc.toString()));
        // Alice kani encrypt to public key tis (periexi ta DH values) me to RSA public key tu bob
        byte[] encryptedAlicePubKey = KeystoreManager.encryptByteArray(clientPublicKey,alicePubKeyEnc);
        Log.d(alice,"Alice Encrypted DH key:" + new String(encryptedAlicePubKey));
        // and sends it over to Bob.
        outputStream.write(encryptedAlicePubKey);

        //Alice get bob key
        byte [] encryptedBobPubKey = new byte[512];
        inputStream.read(encryptedBobPubKey);
        // decrypt bob key
        byte[] bobPubKeyEnc = KeystoreManager.decryptByteArray(encryptedBobPubKey);
        // decode bob key
        KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
        PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
        Log.d(alice,"Alice sees Bob's DH key as:" + new String(bobPubKey.getEncoded()));

        System.out.println("ALICE: Execute PHASE1 ...");
        aliceKeyAgree.doPhase(bobPubKey, true);

        /*
         * At this stage, both Alice and Bob have completed the DH key
         * agreement protocol.
         * Both generate the (same) shared secret.
         */
        byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();
        int aliceLen = aliceSharedSecret.length;

        Log.d(alice,"Alice secret: " +
                toHexString(aliceSharedSecret));

        // Algorithm of choice implementation
        algo = new AlgoCrypt(aliceKeyAgree,bobPubKey,"AES","AES/ECB/PKCS5Padding");
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