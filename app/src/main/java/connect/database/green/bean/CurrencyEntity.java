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
public class CurrencyEntity implements Serializable{

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    @Unique
    @NotNull
    private Integer currency;
    private Integer category;
    @Unique
    @NotNull
    private String salt;
    private String masterAddress;
    private String defaultAddress;
    private Integer status;
    private Long amount;
    private Long balance;
    private String payload;
    @Generated(hash = 1237195953)
    public CurrencyEntity(Long _id, @NotNull Integer currency, Integer category,
            @NotNull String salt, String masterAddress, String defaultAddress,
            Integer status, Long amount, Long balance, String payload) {
        this._id = _id;
        this.currency = currency;
        this.category = category;
        this.salt = salt;
        this.masterAddress = masterAddress;
        this.defaultAddress = defaultAddress;
        this.status = status;
        this.amount = amount;
        this.balance = balance;
        this.payload = payload;
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
    public Integer getCurrency() {
        return this.currency;
    }
    public void setCurrency(Integer currency) {
        this.currency = currency;
    }
    public Integer getCategory() {
        return this.category;
    }
    public void setCategory(Integer category) {
        this.category = category;
    }
    public String getSalt() {
        return this.salt;
    }
    public void setSalt(String salt) {
        this.salt = salt;
    }
    public String getMasterAddress() {
        return this.masterAddress;
    }
    public void setMasterAddress(String masterAddress) {
        this.masterAddress = masterAddress;
    }
    public String getDefaultAddress() {
        return this.defaultAddress;
    }
    public void setDefaultAddress(String defaultAddress) {
        this.defaultAddress = defaultAddress;
    }
    public Integer getStatus() {
        return this.status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public Long getBalance() {
        return this.balance;
    }
    public void setBalance(Long balance) {
        this.balance = balance;
    }
    public String getPayload() {
        return this.payload;
    }
    public void setPayload(String payload) {
        this.payload = payload;
    }
    public Long getAmount() {
        return this.amount;
    }
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
}
