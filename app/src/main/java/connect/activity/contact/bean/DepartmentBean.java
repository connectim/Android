package connect.activity.contact.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/1/4 0004.
 */

public class DepartmentBean implements Serializable{

    private String name;
    private String o_u;
    private String uid;
    private String avatar;
    private String pub_key;
    private Boolean registed;
    private Long id;
    private Long count;
    private String empNo;
    private String mobile;
    private Integer gender;
    private String tips;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getO_u() {
        return o_u;
    }

    public void setO_u(String o_u) {
        this.o_u = o_u;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPub_key() {
        return pub_key;
    }

    public void setPub_key(String pub_key) {
        this.pub_key = pub_key;
    }

    public Boolean getRegisted() {
        return registed;
    }

    public void setRegisted(Boolean registed) {
        this.registed = registed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
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
}
