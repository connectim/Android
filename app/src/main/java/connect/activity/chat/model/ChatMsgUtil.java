package connect.activity.chat.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.StickerCategory;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.home.bean.MsgNoticeBean;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.MessageEntity;
import connect.im.model.FailMsgsManager;
import connect.utils.RegularUtil;
import connect.utils.data.ResourceUtil;


/**
 * Created by gtq on 2016/12/5.
 */
public class ChatMsgUtil {

    private static String Tag = "ChatMsgUtil";

    /**
     * sender, get the message sender direction
     * @param from
     * @return
     */
    public static MsgDirect parseMsgDirect(String from) {
        String mypubkey = MemoryDataManager.getInstance().getPubKey();
        return mypubkey.equals(from) ? MsgDirect.To : MsgDirect.From;
    }

    /**
     * Update message status
     *
     * @param roomkey
     * @param msgid
     * @param state
     */
    public static void updateMsgSendState(String roomkey, String msgid, int state) {
        MessageEntity msgEntity = MessageHelper.getInstance().loadMsgByMsgid(msgid);
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
}
