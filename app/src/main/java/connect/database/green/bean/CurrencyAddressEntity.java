package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Administrator on 2017/7/10.
 */
@Entity
public class CurrencyAddressEntity implements Serializable{

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    private String currency_code;
    private String address;
    private Long address_index;
    private Integer addresss_status;
    private Long address_balance;
    @Generated(hash = 1314138908)
    public CurrencyAddressEntity(Long _id, String currency_code, String address,
            Long address_index, Integer addresss_status, Long address_balance) {
        this._id = _id;
        this.currency_code = currency_code;
        this.address = address;
        this.address_index = address_index;
        this.addresss_status = addresss_status;
        this.address_balance = address_balance;
    }
    @Generated(hash = 1937805610)
    public CurrencyAddressEntity() {
    }
    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public String getCurrency_code() {
        return this.currency_code;
    }
    public void setCurrency_code(String currency_code) {
        this.currency_code = currency_code;
    }
    public String getAddress() {
        return this.address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public Long getAddress_index() {
        return this.address_index;
    }
    public void setAddress_index(Long address_index) {
        this.address_index = address_index;
    }
    public Integer getAddresss_status() {
        return this.addresss_status;
    }
    public void setAddresss_status(Integer addresss_status) {
        this.addresss_status = addresss_status;
    }
    public Long getAddress_balance() {
        return this.address_balance;
    }
    public void setAddress_balance(Long address_balance) {
        this.address_balance = address_balance;
    }

}
