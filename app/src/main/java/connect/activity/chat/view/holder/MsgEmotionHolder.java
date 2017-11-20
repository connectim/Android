package connect.activity.chat.view.holder;

import android.view.View;

import java.io.File;

import connect.activity.chat.bean.RoomSession;
import connect.widget.bottominput.EmoManager;
import connect.ui.activity.R;
import connect.widget.GifView;
import instant.bean.ChatMsgEntity;
import protos.Connect;

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
    public void buildRowData(MsgBaseHolder msgBaseHolder, ChatMsgEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        Connect.EmotionMessage emotionMessage = Connect.EmotionMessage.parseFrom(msgExtEntity.getContents());
        RoomSession.getInstance().checkBurnTime(emotionMessage.getSnapTime());

        String filepath = emotionMessage.getContent();
        filepath = EmoManager.GIF_PATH + File.separator + filepath + ".gif";
        /*Glide.with(context).load("file:///android_asset/" + filepath)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(emotionimg);*/

        emotionimg.setGifResource(filepath);
        emotionimg.play();
    }
}
