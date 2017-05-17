package connect.ui.activity.chat.view.holder;

import android.view.View;

import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.model.EmoManager;
import connect.view.GifView;

import java.io.File;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgEmotionHolder extends MsgChatHolder {
    private GifView emotionimg;

    public MsgEmotionHolder(View itemView) {
        super(itemView);
        emotionimg = (GifView) itemView.findViewById(R.id.emotionmsg);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, MsgEntity entity) {
        super.buildRowData(msgBaseHolder, entity);
        MsgDefinBean bean= entity.getMsgDefinBean();

        String filepath = bean.getContent();
        filepath = EmoManager.GIF_PATH + File.separator + filepath + ".gif";
        emotionimg.setGifResource(filepath);
        emotionimg.play();
    }
}
