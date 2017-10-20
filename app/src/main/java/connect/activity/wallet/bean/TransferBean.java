package connect.activity.wallet.bean;

/**
 * Created by Administrator on 2017/4/19 0019.
 */

public class TransferBean {

    //1. outer lucky packet 2. outer transfer 3. address transfer 4. transfer to friend 5. friend lucky packet
    private int type;
    private String avater;
    private String name;
    private String uid;

    public TransferBean(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public TransferBean(int type, String avater, String name, String uid) {
        this.type = type;
        this.avater = avater;
        this.name = name;
        this.uid = uid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAvater() {
        return avater;
    }

    public void setAvater(String avater) {
        this.avater = avater;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
