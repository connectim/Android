package connect.activity.chat.view.holder;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.io.File;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.model.EmoManager;
import connect.ui.activity.R;
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
    public void buildRowData(MsgBaseHolder msgBaseHolder, MsgEntity entity) {
        super.buildRowData(msgBaseHolder, entity);
        MsgDefinBean bean = entity.getMsgDefinBean();

        String filepath = bean.getContent();
        filepath = EmoManager.GIF_PATH + File.separator + filepath + ".gif";

        Glide.with(context).load("file:///android_asset/" + filepath)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(emotionimg);
    }
}
