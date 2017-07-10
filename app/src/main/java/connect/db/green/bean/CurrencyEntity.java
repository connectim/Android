package connect.db.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Administrator on 2017/7/10.
 */
@Entity
public class CurrencyEntity {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    private Integer currency_code;
    private String address;
    private String address_index;
    private Integer addresss_status;
    private Long address_balance;
    @Generated(hash = 1989409973)
    public CurrencyEntity(Long _id, Integer currency_code, String address,
            String address_index, Integer addresss_status, Long address_balance) {
        this._id = _id;
        this.currency_code = currency_code;
        this.address = address;
        this.address_index = address_index;
        this.addresss_status = addresss_status;
        this.address_balance = address_balance;
    }
    @Generated(hash = 228156879)
    public CurrencyEntity() {
    }
    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public Integer getCurrency_code() {
        return this.currency_code;
    }
    public void setCurrency_code(Integer currency_code) {
        this.currency_code = currency_code;
    }
    public String getAddress() {
        return this.address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getAddress_index() {
        return this.address_index;
    }
    public void setAddress_index(String address_index) {
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
