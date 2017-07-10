package connect.database.green.bean;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "PARAM_ENTITY".
 */
@Entity
public class ParamEntity implements java.io.Serializable {

    @Id(autoincrement = true)
    private Long _id;

    @Unique
    private String key;
    private String value;
    private String ext;

    @Generated
    public ParamEntity() {
    }

    public ParamEntity(Long _id) {
        this._id = _id;
    }

    @Generated
    public ParamEntity(Long _id, String key, String value, String ext) {
        this._id = _id;
        this.key = key;
        this.value = value;
        this.ext = ext;
    }

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

}
