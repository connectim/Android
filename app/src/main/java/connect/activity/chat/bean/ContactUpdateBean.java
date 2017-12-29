package connect.activity.chat.bean;

import java.io.Serializable;

/**
 * Created by PuJin on 2017/12/29.
 */

public class ContactUpdateBean implements Serializable {

    private String contactUid;
    private String contactName;
    private String contactAvatar;

    public ContactUpdateBean(String contactUid, String contactAvatar) {
        this.contactUid = contactUid;
        this.contactAvatar = contactAvatar;
    }

    public String getContactUid() {
        return contactUid;
    }

    public void setContactUid(String contactUid) {
        this.contactUid = contactUid;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactAvatar() {
        return contactAvatar;
    }

    public void setContactAvatar(String contactAvatar) {
        this.contactAvatar = contactAvatar;
    }
}
