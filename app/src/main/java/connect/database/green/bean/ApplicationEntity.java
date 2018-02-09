package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ApplicationEntity implements Serializable{

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    @NotNull
    @Unique
    private String code;
    @NotNull
    private int category;

    @Generated(hash = 55228676)
    public ApplicationEntity(Long _id, @NotNull String code, int category) {
        this._id = _id;
        this.code = code;
        this.category = category;
    }

    @Generated(hash = 1177651541)
    public ApplicationEntity() {
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
}
