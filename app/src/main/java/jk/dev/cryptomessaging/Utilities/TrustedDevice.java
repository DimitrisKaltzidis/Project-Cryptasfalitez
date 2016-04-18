package jk.dev.cryptomessaging.Utilities;

/**
 * Created by Jim on 19/4/2016.
 */
public class TrustedDevice {
    private String macAddress;
    private String name;
    private String publicKey;

    public TrustedDevice(String macAddress, String name, String publicKey) {
        this.macAddress = macAddress;
        this.name = name;
        this.publicKey = publicKey;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
