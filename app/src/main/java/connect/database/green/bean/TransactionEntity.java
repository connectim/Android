package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class TransactionEntity implements Serializable {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;
    @NotNull
    @Unique
    private String message_id;
    @NotNull
    @Unique
    private String hashid;

    private Integer status;
    private Integer pay_count;
    private Integer crowd_count;

    @Generated(hash = 950792307)
    public TransactionEntity(Long _id, @NotNull String message_id,
            @NotNull String hashid, Integer status, Integer pay_count,
            Integer crowd_count) {
        this._id = _id;
        this.message_id = message_id;
        this.hashid = hashid;
        this.status = status;
        this.pay_count = pay_count;
        this.crowd_count = crowd_count;
    }
    @Generated(hash = 1319631883)
    public TransactionEntity() {
    }
    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public String getMessage_id() {
        return this.message_id;
    }
    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }
    public String getHashid() {
        return this.hashid;
    }
    public void setHashid(String hashid) {
        this.hashid = hashid;
    }
    public Integer getStatus() {
        return this.status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public Integer getPay_count() {
        return this.pay_count;
    }
    public void setPay_count(Integer pay_count) {
        this.pay_count = pay_count;
    }
    public Integer getCrowd_count() {
        return this.crowd_count;
    }
    public void setCrowd_count(Integer crowd_count) {
        this.crowd_count = crowd_count;
    }

}
