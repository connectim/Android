package connect.ui.activity.contact.bean;

/**
 * Mobile address book contacts
 */
public class PhoneContactBean{
    private String id;
    private String phone;
    private String nickName;
    private String avater;
    private String name;
    private String address;
    private String pubKey;
    private int status;//1：Did not add  2：Have been added 3：In the validation

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
