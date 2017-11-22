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
import instant.sender.model.SubscriberChat;

/**
 * Created by Administrator on 2017/11/22.
 */

public class CSubscriberChat extends SubscriberChat implements ConversationListener {

    public static CSubscriberChat cSubscriberChat = getInstance();

    private synchronized static CSubscriberChat getInstance() {
        if (cSubscriberChat == null) {
            cSubscriberChat = new CSubscriberChat();
        }
        return cSubscriberChat;
    }

    public void updateRoomMsg(String draft, String showText, long msgtime) {
        updateRoomMsg(draft, showText, msgtime, -1);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at) {
        updateRoomMsg(draft, showText, msgtime, at, 0);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg) {
        updateRoomMsg(draft, showText, msgtime, at, newmsg, true);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg, boolean broad) {
        if (TextUtils.isEmpty(chatKey())) {
            return;
        }

        Context context = BaseApplication.getInstance().getBaseContext();
        List<RoomAttrBean> roomEntities = ConversionHelper.getInstance().loadRoomEntities(chatKey());
        if (roomEntities == null || roomEntities.size() == 0) {
            ConversionEntity conversionEntity = new ConversionEntity();
            conversionEntity.setIdentifier(context.getString(R.string.app_name));
            conversionEntity.setName(nickName());
            conversionEntity.setAvatar(headImg());
            conversionEntity.setType(chatType());
            conversionEntity.setContent(TextUtils.isEmpty(showText) ? "" : showText);
            conversionEntity.setStranger(isStranger ? 1 : 0);
            conversionEntity.setUnread_count(newmsg == 0 ? 0 : 1);
            conversionEntity.setDraft(TextUtils.isEmpty(draft) ? "" : draft);
            conversionEntity.setIsAt(at);
            ConversionHelper.getInstance().insertRoomEntity(conversionEntity);
        } else {
            for (RoomAttrBean attrBean : roomEntities) {
                ConversionHelper.getInstance().updateRoomEntity(
                        context.getString(R.string.app_name),
                        TextUtils.isEmpty(draft) ? "" : draft,
                        TextUtils.isEmpty(showText) ? "" : showText,
                        (newmsg == 0 ? 0 : 1 + attrBean.getUnread()),
                        at,
                        (isStranger ? 1 : 0),
                        (msgtime > 0 ? 0 : msgtime)
                );
            }
        }
        if (broad) {
            ConversationAction.conversationAction.sendEvent(ConversationAction.ConverType.LOAD_MESSAGE);
        }
    }
}
