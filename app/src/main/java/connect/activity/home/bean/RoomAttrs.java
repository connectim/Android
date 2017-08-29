package connect.activity.home.bean;

import android.text.TextUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import connect.activity.chat.model.content.FriendChat;
import connect.activity.chat.model.content.GroupChat;
import connect.activity.chat.model.content.NormalChat;
import connect.activity.chat.model.content.RobotChat;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.utils.TimeUtil;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/29.
 */

public class RoomAttrs {

    public static RoomAttrs roomAttrs = getInstance();

    private static RoomAttrs getInstance() {
        if (roomAttrs == null) {
            synchronized (RoomAttrs.class) {
                roomAttrs = new RoomAttrs();
            }
        }
        return roomAttrs;
    }

    private List<RoomAttrBean> roomAttrBeanList = null;
    private RoomAttrCompara attrCompara = new RoomAttrCompara();

    public List<RoomAttrBean> getAttrBeanList() {
        if (roomAttrBeanList == null) {
            roomAttrBeanList = new LinkedList<>();
            roomAttrBeanList = ConversionHelper.getInstance().loadRoomEntites();
        }

        Collections.sort(roomAttrBeanList, attrCompara);
        return roomAttrBeanList;
    }

    public List<RoomAttrBean> insertAttrContent(ConversationType conversationType, Object... objects) {
        switch (conversationType) {
            case CLEAR:
                roomAttrBeanList.clear();
                break;
            case REMOVE:
                String publicKey = (String) objects[0];
                removeRoomAttr(publicKey);
                break;
            default:
                updateRoomAttr(objects);
                break;
        }
        return getAttrBeanList();
    }

    private void removeRoomAttr(String publicKey){
        Iterator<RoomAttrBean> iterator = roomAttrBeanList.iterator();
        while (iterator.hasNext()) {
            RoomAttrBean attrBean = iterator.next();
            if (attrBean.getRoomid().equals(publicKey)) {
                iterator.remove();
                break;
            }
        }
    }

    public void updateRoomAttr(Object... objects) {
        boolean isExist = false;
        Iterator<RoomAttrBean> iterator = roomAttrBeanList.iterator();

        String publicKey = (String) objects[0];
        Integer chatType = (Integer) objects[1];
        while (iterator.hasNext()) {
            RoomAttrBean tempBean = iterator.next();
            if (tempBean.getRoomid().equals(publicKey)) {
                isExist = true;

                tempBean = updateRoomAttr(tempBean, objects);
                roomAttrBeanList.add(tempBean);
                break;
            }
        }

        if (!isExist) {
            RoomAttrBean tempBean = insertNewRoomAttr(Connect.ChatType.forNumber(chatType), publicKey);
            tempBean = updateRoomAttr(tempBean, objects);
            roomAttrBeanList.add(tempBean);
        }
    }

    private RoomAttrBean insertNewRoomAttr(Connect.ChatType chatType, String publicKey) {
        List<RoomAttrBean> attrBeanList = ConversionHelper.getInstance().loadRoomEntities(publicKey);
        RoomAttrBean attrBean;

        if (attrBeanList.size() <= 0) {
            attrBean = new RoomAttrBean();
            NormalChat normalChat = null;
            switch (chatType) {
                case CONNECT_SYSTEM:
                    normalChat = RobotChat.getInstance();
                    break;
                case PRIVATE:
                    ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(publicKey);
                    if (friendEntity != null) {
                        normalChat = new FriendChat(friendEntity);
                    }
                    break;
                case GROUPCHAT:
                    GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(publicKey);
                    if (groupEntity != null) {
                        normalChat = new GroupChat(groupEntity);
                    }
                    break;
            }

            String roomid = "";
            String nickName = "";
            String headImg = "";
            if (normalChat != null) {
                roomid = normalChat.identify();
                nickName = normalChat.nickName();
                headImg = normalChat.headImg();
            }

            attrBean.setRoomid(roomid);
            attrBean.setName(nickName);
            attrBean.setAvatar(headImg);
            attrBean.setTop(0);
            attrBean.setUnread(0);
            attrBean.setTimestamp(TimeUtil.getCurrentTimeInLong());
        } else {
            attrBean = attrBeanList.get(0);
        }
        return attrBean;
    }

    public RoomAttrBean updateRoomAttr(RoomAttrBean tempBean, Object... objects) {
        String nickName = (String) objects[2];
        String avatar = (String) objects[3];
        Long timeStamp = (Long) objects[4];
        Integer unRead = (Integer) objects[5];
        String content = (String) objects[6];
        String draft = (String) objects[7];
        Integer disturb = (Integer) objects[8];
        Integer at = (Integer) objects[9];
        Integer stranger = (Integer) objects[9];

        if (!TextUtils.isEmpty(nickName)) {
            tempBean.setName(nickName);
        }

        if (!TextUtils.isEmpty(avatar)) {
            tempBean.setAvatar(avatar);
        }

        tempBean.setTimestamp(timeStamp);
        tempBean.setContent(content);
        tempBean.setDraft(draft);
        tempBean.setDisturb(disturb);
        tempBean.setAt(at);
        tempBean.setStranger(stranger);


        if (unRead == 0) {
            tempBean.setUnread(0);
        } else if (unRead > 0) {
            tempBean.setUnread(1 + tempBean.getUnread());
        }
        return tempBean;
    }

    class RoomAttrCompara implements Comparator<RoomAttrBean> {

        @Override
        public int compare(RoomAttrBean lhs, RoomAttrBean rhs) {
            int compare = 0;

            int lhsTop = lhs.getTop();
            int rhsTop = rhs.getTop();

            if (lhsTop == rhsTop) {
                long lhsTime = lhs.getTimestamp();
                long rhsTime = rhs.getTimestamp();
                compare = lhsTime <= rhsTime ? -1 : 1;
            } else {
                compare = lhsTop <= rhsTop ? -1 : 1;
            }
            return compare;
        }
    }
}
