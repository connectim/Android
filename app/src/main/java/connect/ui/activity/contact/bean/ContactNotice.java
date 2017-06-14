package connect.ui.activity.contact.bean;

import org.greenrobot.eventbus.EventBus;

/**
 * Contact notice
 * Created by gtq on 2016/12/28.
 */
public class ContactNotice {

    public enum ConNotice{
        RecContact,
        RecAddFriend,
        RecGroup,
        RecFriend,
    }

    private ConNotice notice;

    public ContactNotice() {
    }

    public ContactNotice(ConNotice notice) {
        this.notice = notice;
    }

    public ConNotice getNotice(){
        return this.notice;
    }
    
    public static void receiverAddFriend() {
        EventBus.getDefault().post(new ContactNotice(ConNotice.RecAddFriend));
    }

    public static void receiverGroup() {
        EventBus.getDefault().post(new ContactNotice(ConNotice.RecGroup));
    }

    public static void receiverContact() {
        EventBus.getDefault().post(new ContactNotice(ConNotice.RecContact));
    }

    public static void receiverFriend() {
        EventBus.getDefault().post(new ContactNotice(ConNotice.RecFriend));
    }
}
