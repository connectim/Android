package connect.activity.chat.view.holder;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import connect.activity.chat.bean.MsgExtEntity;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.CardExt1Bean;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.contact.FriendInfoActivity;
import connect.activity.contact.StrangerInfoActivity;
import connect.activity.contact.bean.SourceType;
import connect.utils.glide.GlideUtil;
import connect.widget.roundedimageview.RoundedImageView;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgCardHolder extends MsgChatHolder {

    private RoundedImageView cardHead;
    private TextView cardName;

    private CardExt1Bean cardExt1Bean;

    public MsgCardHolder(View itemView) {
        super(itemView);
        cardHead = (RoundedImageView) itemView.findViewById(R.id.roundimg_head_small);
        cardName = (TextView) itemView.findViewById(R.id.tvName);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgExtEntity baseEntity) {
        super.buildRowData(msgBaseHolder, baseEntity);
        MsgDefinBean definBean = baseEntity.getMsgDefinBean();
        cardExt1Bean = new Gson().fromJson(definBean.getExt1(), CardExt1Bean.class);

        GlideUtil.loadAvater(cardHead, cardExt1Bean.getAvatar());
        cardName.setText(cardExt1Bean.getUsername());
        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactEntity entity = ContactHelper.getInstance().loadFriendEntity(cardExt1Bean.getPub_key());
                if (entity == null) {
                    StrangerInfoActivity.startActivity((Activity) context, cardExt1Bean.getAddress(), SourceType.CARD);
                } else {
                    FriendInfoActivity.startActivity((Activity) context, cardExt1Bean.getPub_key());
                }
            }
        });
    }
}
