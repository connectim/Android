package connect.activity.wallet.bean;

/**
 * Created by Administrator on 2016/12/21.
 */
public class AddressBean {

    private String tag;
    private String address;

    public AddressBean() {

    }

    public AddressBean(String tag, String address) {
        this.tag = tag;
        this.address = address;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
