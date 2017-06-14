package connect.im.bean;

/**
 * Created by gtq on 2016/11/30.
 */
public enum SocketACK {
    VERSION_CHANGE(new byte[]{0x00, (byte) 0x90}),//Version changes

    HAND_SHAKE_FIRST(new byte[]{0x01, 0x01}),//The first handshake messages
    HAND_SHAKE_SECOND(new byte[]{0x01, 0x02}),//The second handshake messages

    HEART_BREAK(new byte[]{0x02, 0x00}),//The heartbeat

    ACK_BACK_ONLINE(new byte[]{0x03, 0x01}),//Online message receipt
    ACK_BACK_OFFLINE(new byte[]{0x03, 0x02}),//Offline message receipt

    CONTACT_SYNC(new byte[]{0x04, 0x01}),//Sync contacts
    PULL_OFFLINE(new byte[]{0x04, 0x04}),//Pull the offline messages
    CONTACT_LOGIN(new byte[]{0x04, 0x06}),//The login
    CONTACT_LOGOUT(new byte[]{0x04, 0x07}),//exit
    ADD_FRIEND(new byte[]{0x04, 0x08}),//Add buddy
    AGREE_FRIEND(new byte[]{0x04, 0x09}),//Agree to add buddy
    REMOVE_FRIEND(new byte[]{0x04, 0x0a}),//Remove buddy
    SET_FRIEND(new byte[]{0x04, 11}),//Modify the friends remark and common friends
    NO_INTERESTED(new byte[]{0x04, 0x15}),//Not interested in
    OUTER_TRANSFER(new byte[]{0x04, 0x11}),//External transfer
    OUTER_REDPACKET(new byte[]{0x04, 0x12}),//Outside a red envelope
    UPLOAD_APPINFO(new byte[]{0x04, 0x16}),//Report the device version information
    UPLOAD_CHATCOOKIE(new byte[]{0x04, 0x17}),//Upload session Cookie
    DOWNLOAD_FRIENDCOOKIE(new byte[]{0x04, 0x18}),//Get friends cookies
    DIFFERENT_DEVICE(new byte[]{0x04,0x19}),//different devive login in

    ROBOT_CHAT(new byte[]{0x05, 0x00}),//Robot news
    SINGLE_CHAT(new byte[]{0x05, 0x01}),//The private chat
    GROUP_INVITE(new byte[]{0x05, 0x03}),//Invited into the group of
    GROUP_CHAT(new byte[]{0x05, 0x04}),//Group chat
    MSG_UNTOUCH(new byte[]{0x05, 0x05}),//Message inaccessible
    CHAT_NOTICE(new byte[]{0x05, 0x09});//notice

    byte[] order;

    SocketACK(byte[] order) {
        this.order = order;
    }

    public byte[] getOrder() {
        return order;
    }
}
