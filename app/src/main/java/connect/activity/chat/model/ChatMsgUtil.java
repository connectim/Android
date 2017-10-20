package connect.activity.chat.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import connect.activity.base.BaseListener;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupMemberEntity;
import connect.utils.ProtoBufUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.StickerCategory;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.home.bean.MsgNoticeBean;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.MessageEntity;
import connect.utils.RegularUtil;
import connect.utils.data.ResourceUtil;
import instant.utils.manager.FailMsgsManager;
import protos.Connect;


/**
 * Created by gtq on 2016/12/5.
 */
public class ChatMsgUtil {

    private static String Tag = "ChatMsgUtil";

    public static ChatMsgUtil chatMsgUtil = getIntance();

    private synchronized static ChatMsgUtil getIntance() {
        if (chatMsgUtil == null) {
            chatMsgUtil = new ChatMsgUtil();
        }
        return chatMsgUtil;
    }

    /**
     * Update message status
     *
     * @param roomkey
     * @param msgid
     * @param state
     */
    public static void updateMsgSendState(String roomkey, String msgid, int state) {
        MessageEntity msgEntity = MessageEntity.chatMsgToMessageEntity(MessageHelper.getInstance().loadMsgByMsgid(msgid));
        if (msgEntity != null) {
            msgEntity.setSend_status(state);
            MessageHelper.getInstance().updateMsg(msgEntity);
        }

        if (TextUtils.isEmpty(roomkey)) {
            Map<String, Object> failMap = FailMsgsManager.getInstance().getFailMap(msgid);
            if (failMap != null) {
                Object object = failMap.get("EXT");
                if (object instanceof MsgSendBean) {
                    MsgNoticeBean.sendMsgNotice(MsgNoticeBean.NtEnum.MSG_SEND_SUCCESS, failMap.get("EXT"));
                }
            }
        } else {
            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MSGSTATE, roomkey, msgid, state);
        }
    }

    /**
     * expression to text
     *
     * @param content
     * @return
     */
    public static SpannableStringBuilder txtTransEmotion(final Context context, final String content) {
        SpannableStringBuilder mSpannableString = new SpannableStringBuilder(content);
        Matcher emjMatcher = Pattern.compile(RegularUtil.VERIFYCATION_EMJ).matcher(content);
        while (emjMatcher.find()) {
            int start = emjMatcher.start();
            int end = emjMatcher.end();
            String emot = content.substring(start, end);
            emot = emot.substring(1, emot.length() - 1) + ".png";
            String key = StickerCategory.emojiMaps.get(emot);
            if (!TextUtils.isEmpty(key)) {
                emot = key;
                Drawable d = ResourceUtil.getEmotDrawable(emot);
                if (d != null) {
                    ImageSpan span = new ImageSpan(d);
                    mSpannableString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return mSpannableString;
    }

    private Map<String, GroupMemberEntity> memEntityMap = null;

    public void loadGroupMembersMap(String groupKey) {
        if (memEntityMap == null) {
            memEntityMap = new HashMap<>();
        }
        List<GroupMemberEntity> groupMemEntities = ContactHelper.getInstance().loadGroupMemEntities(groupKey);
        for (GroupMemberEntity memEntity : groupMemEntities) {
            memEntityMap.put(memEntity.getUid(), memEntity);
        }
    }

    public void loadGroupMember(String groupKey, String memberkey, BaseListener<GroupMemberEntity> baseListener) {
        if (memEntityMap == null) {
            loadGroupMembersMap(groupKey);
        }

        GroupMemberEntity memberEntity = memEntityMap.get(memberkey);
        if (memberEntity == null) {
            requestGroupMemberDetailInfo(memberkey, baseListener);
        } else {
            baseListener.Success(memberEntity);
        }
    }

    public void requestGroupMemberDetailInfo(String publickey, final BaseListener<GroupMemberEntity> baseListener) {
        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(publickey)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNEXT_V1_USERS_SEARCHBYPUBKEY, searchUser, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.UserInfo userInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(userInfo)) {
                        GroupMemberEntity memberEntity = new GroupMemberEntity();
                        memberEntity.setAvatar(userInfo.getAvatar());
                        memberEntity.setUsername(userInfo.getUsername());
                        memEntityMap.put(userInfo.getPubKey(), memberEntity);
                        baseListener.Success(memberEntity);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                baseListener.fail("");
            }
        });
    }
}
