package connect.activity.chat.view.holder;

import android.app.Activity;
import android.text.method.LinkMovementMethod;
import android.view.View;

import com.google.protobuf.InvalidProtocolBufferException;

import connect.activity.chat.bean.RoomSession;
import connect.activity.chat.view.EmoTextView;
import connect.ui.activity.R;
import connect.widget.selefriend.SelectRecentlyChatActivity;
import instant.bean.ChatMsgEntity;
import protos.Connect;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgTxtHolder extends MsgChatHolder {

    private EmoTextView txtmsg;

    public MsgTxtHolder(View itemView) {
        super(itemView);
        txtmsg = (EmoTextView) itemView.findViewById(R.id.txtmsg);
        txtmsg.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, ChatMsgEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(msgExtEntity.getContents());
        RoomSession.getInstance().checkBurnTime(textMessage.getSnapTime());

        String content = textMessage.getContent();
        txtmsg.setText(content);
    }

    @Override
    public String[] longPressPrompt() {
        return context.getResources().getStringArray(R.array.prompt_txt);
    }

    @Override
    public View longClickView() {
        return txtmsg;
    }

    @Override
    public void transPondTo() {
        ChatMsgEntity msgExtEntity = getMsgExtEntity();
        try {
            Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(msgExtEntity.getContents());
            SelectRecentlyChatActivity.startActivity((Activity) context, SelectRecentlyChatActivity.TRANSPOND, String.valueOf(msgExtEntity.getMessageType()), textMessage.getContent());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getCopyTxt() {
        return txtmsg.getText().toString();
    }
}
