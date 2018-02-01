package connect.activity.chat.fragment.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/1/31 0031.
 */

public class GroupWithCountEntity implements Serializable{

    private String identifier;

    private String name;
    private String avatar;
    private String count;

    public GroupWithCountEntity() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
