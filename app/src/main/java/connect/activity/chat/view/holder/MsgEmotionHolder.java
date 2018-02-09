package connect.activity.chat.view.holder;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

import connect.ui.activity.R;
import connect.widget.bottominput.EmoManager;
import instant.bean.ChatMsgEntity;
import protos.Connect;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgEmotionHolder extends MsgChatHolder {
    private ImageView emotionimg;

    public MsgEmotionHolder(View itemView) {
        super(itemView);
        emotionimg = (ImageView) itemView.findViewById(R.id.emotionmsg);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final ChatMsgEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        Connect.EmotionMessage emotionMessage = Connect.EmotionMessage.parseFrom(msgExtEntity.getContents());

        String filepath = emotionMessage.getContent();
        filepath = EmoManager.EMOJI_PATH + File.separator + "png" + File.separator + filepath + ".png";
        Glide.with(context)
                .load("file:///android_asset/" + filepath)
                .into(emotionimg);
    }
}
