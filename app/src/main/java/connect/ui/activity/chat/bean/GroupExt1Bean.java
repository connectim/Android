package connect.ui.activity.chat.bean;

import java.io.Serializable;

/**
 * Created by pujin on 2017/1/21.
 */

public class GroupExt1Bean implements Serializable{

    private String avatar;
    private String groupname;
    private String groupidentifier;
    private String inviteToken;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getGroupidentifier() {
        return groupidentifier;
    }

    public void setGroupidentifier(String groupidentifier) {
        this.groupidentifier = groupidentifier;
    }

    public void setInviteToken(String inviteToken) {
        this.inviteToken = inviteToken;
    }

    public String getInviteToken() {
        return inviteToken;
    }
}
