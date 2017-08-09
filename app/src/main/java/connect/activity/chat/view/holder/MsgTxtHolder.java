package connect.activity.chat.view.holder;

import android.app.Activity;
import android.text.method.LinkMovementMethod;
import android.view.View;

import connect.activity.home.bean.HomeAction;
import connect.ui.activity.R;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.view.EmoTextView;
import connect.activity.common.selefriend.ConversationActivity;
import connect.activity.common.bean.ConverType;
import connect.utils.ActivityUtil;

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
    public void buildRowData(MsgBaseHolder msgBaseHolder, MsgEntity entity) {
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
