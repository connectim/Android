package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by Administrator on 2017/7/10.
 */
@Entity
public class CurrencyAddressEntity implements Serializable{

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    @NotNull
    private Integer currency;
    @Unique
    @NotNull
    private String address;
    private Integer index;
    private Long balance;
    private Integer status;
    private String label;
    @Generated(hash = 383368957)
    public CurrencyAddressEntity(Long _id, @NotNull Integer currency,
            @NotNull String address, Integer index, Long balance, Integer status,
            String label) {
        this._id = _id;
        this.currency = currency;
        this.address = address;
        this.index = index;
        this.balance = balance;
        this.status = status;
        this.label = label;
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
    public Integer getCurrency() {
        return this.currency;
    }
    public void setCurrency(Integer currency) {
        this.currency = currency;
    }
    public String getAddress() {
        return this.address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public Integer getIndex() {
        return this.index;
    }
    public void setIndex(Integer index) {
        this.index = index;
    }
    public Long getBalance() {
        return this.balance;
    }
    public void setBalance(Long balance) {
        this.balance = balance;
    }
    public Integer getStatus() {
        return this.status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public String getLabel() {
        return this.label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

}
