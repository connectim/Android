package connect.wallet.jni;

/**

 */
public class GCMModel {
    public byte[] encrypt;
    public byte[] tag;

    public GCMModel(byte[] encrypt, byte[] tag) {
        super();
        this.tag = tag;
        this.encrypt = encrypt;
    }

    public GCMModel() {
        super();
    }




}
