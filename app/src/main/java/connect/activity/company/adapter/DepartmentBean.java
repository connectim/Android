package connect.activity.company.adapter;

/**
 * Created by Administrator on 2018/1/4 0004.
 */

public class DepartmentBean {

    private String name;
    private String o_u;
    private String uid;
    private String avatar;
    private String pub_key;
    private Boolean registed;
    private Long id;

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
}
