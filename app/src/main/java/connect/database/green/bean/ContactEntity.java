package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;

@Entity
public class ContactEntity implements Serializable {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    @NotNull
    @Unique
    private String uid;
    private String name;
    private String avatar;
    private String ou;
    private String publicKey;
    private String empNo;
    private String mobile;
    private Integer gender;
    private String tips;
    private Boolean registed;

    @Generated(hash = 393979869)
    public ContactEntity() {
    }

    @Generated(hash = 338746229)
    public ContactEntity(Long _id, @NotNull String uid, String name, String avatar,
            String ou, String publicKey, String empNo, String mobile,
            Integer gender, String tips, Boolean registed) {
        this._id = _id;
        this.uid = uid;
        this.name = name;
        this.avatar = avatar;
        this.ou = ou;
        this.publicKey = publicKey;
        this.empNo = empNo;
        this.mobile = mobile;
        this.gender = gender;
        this.tips = tips;
        this.registed = registed;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getOu() {
        return ou;
    }

    public void setOu(String ou) {
        this.ou = ou;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getEmpNo() {
        return empNo;
    }

    public void setEmpNo(String empNo) {
        this.empNo = empNo;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public Boolean getRegisted() {
        return registed;
    }

    public void setRegisted(Boolean registed) {
        this.registed = registed;
    }
}
