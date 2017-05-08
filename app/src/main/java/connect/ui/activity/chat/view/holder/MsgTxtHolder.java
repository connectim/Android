package connect.ui.activity.chat.view.holder;

import android.app.Activity;
import android.text.method.LinkMovementMethod;
import android.view.View;

import connect.ui.activity.R;
import connect.ui.activity.chat.bean.BaseEntity;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.view.EmoTextView;
import connect.ui.activity.common.ConversationActivity;
import connect.ui.activity.common.bean.ConverType;

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
    public void buildRowData(MsgBaseHolder msgBaseHolder, BaseEntity entity) {
        super.buildRowData(msgBaseHolder, entity);
        MsgDefinBean definBean = entity.getMsgDefinBean();
        String content = definBean.getContent();
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
        ConversationActivity.startActivity((Activity) context, ConverType.TRANSPOND, String.valueOf(definBean.getType()), definBean.getContent());
    }
}
