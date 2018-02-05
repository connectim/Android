package connect.instant.model;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.home.bean.ConversationAction;
import connect.activity.home.bean.RoomAttrBean;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.bean.ConversionEntity;
import connect.instant.inter.ConversationListener;
import connect.ui.activity.R;
import instant.sender.model.RobotChat;

/**
 * Created by Administrator on 2017/10/19.
 */

public class CRobotChat extends RobotChat implements ConversationListener{

    private static CRobotChat robotChat;

    public CRobotChat() {
    }

    public synchronized static CRobotChat getInstance() {
        if (robotChat == null) {
            robotChat = new CRobotChat();
        }
        return robotChat;
    }

    @Override
    public String headImg() {
        super.headImg();
        Context context= BaseApplication.getInstance().getBaseContext();
        return context.getString(R.string.app_name);
    }

    @Override
    public String nickName() {
        super.nickName();
        Context context= BaseApplication.getInstance().getBaseContext();
        return context.getString(R.string.app_name);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime) {
        updateRoomMsg(draft, showText, msgtime, 0);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at) {
        updateRoomMsg(draft, showText, msgtime, at, 0);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg) {
        updateRoomMsg(draft,showText,msgtime,at,newmsg,true);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg, boolean broad) {
        updateRoomMsg(draft,showText,msgtime,at,newmsg,true,0);
    }

    @Override
    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg, boolean broad, int attention) {
        if (TextUtils.isEmpty(chatKey())) {
            return;
        }

        Context context= BaseApplication.getInstance().getBaseContext();
        List<RoomAttrBean> roomEntities = ConversionHelper.getInstance().loadRoomEntities(chatKey());
        if (roomEntities == null || roomEntities.size() == 0) {
            ConversionEntity conversionEntity = new ConversionEntity();
            conversionEntity.setIdentifier(context.getString(R.string.app_name));
            conversionEntity.setName(nickName());
            conversionEntity.setAvatar(headImg());
            conversionEntity.setType(chatType());
            conversionEntity.setContent(TextUtils.isEmpty(showText) ? "" : showText);
            conversionEntity.setUnread_count(newmsg == 0 ? 0 : 1);
            conversionEntity.setDraft(TextUtils.isEmpty(draft) ? "" : draft);
            conversionEntity.setLast_time((msgtime < 0 ? 0 : msgtime));
            ConversionHelper.getInstance().insertRoomEntity(conversionEntity);
        } else {
            for (RoomAttrBean attrBean : roomEntities) {
                ConversionHelper.getInstance().updateRoomEntity(
                        context.getString(R.string.app_name),
                        TextUtils.isEmpty(draft) ? "" : draft,
                        TextUtils.isEmpty(showText) ? "" : showText,
                        (newmsg == 0 ? 0 : 1 + attrBean.getUnread()),
                        0,
                        (msgtime < 0 ? 0 : msgtime)
                );
            }
        }
        if (broad) {
            ConversationAction.conversationAction.sendEvent(ConversationAction.ConverType.LOAD_MESSAGE);
        }
    }
}
