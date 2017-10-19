package instant.utils.cryption;

/**
 * Created by Administrator on 2017/9/22.
 */

public class SupportKey {

    private String privateKey;
    private String saltToken;

    private static SupportKey supportKey = getInstance();

    public static SupportKey getInstance() {
        if (supportKey == null) {
            supportKey = new SupportKey();
        }
        return supportKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getSaltToken() {
        return saltToken;
    }

    public void setSaltToken(String saltToken) {
        this.saltToken = saltToken;
    }
}
