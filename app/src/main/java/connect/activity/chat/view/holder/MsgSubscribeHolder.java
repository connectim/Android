package connect.activity.chat.view.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.utils.TimeUtil;
import connect.utils.glide.GlideUtil;
import instant.bean.ChatMsgEntity;
import protos.Connect;

/**
 * Created by puin on 17-11-22.
 */

public class MsgSubscribeHolder extends MsgBaseHolder {

    private LinearLayout sysAdLayout;
    private TextView titleTxt;
    private TextView timeTxt;
    private ImageView converImg;
    private TextView contentTxt;

    public MsgSubscribeHolder(View itemView) {
        super(itemView);
        sysAdLayout = (LinearLayout) itemView.findViewById(R.id.linearlayout);
        titleTxt = (TextView) itemView.findViewById(R.id.txt1);
        timeTxt = (TextView) itemView.findViewById(R.id.txt2);
        converImg = (ImageView) itemView.findViewById(R.id.roundimg1);
        contentTxt = (TextView) itemView.findViewById(R.id.txt3);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final ChatMsgEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        final Connect.Announcement announcement = Connect.Announcement.parseFrom(msgExtEntity.getContents());

        titleTxt.setText(String.valueOf(announcement.getTitle()));
        try {
            timeTxt.setText(TimeUtil.getMsgTime(TimeUtil.getCurrentTimeInLong(), (long) (announcement.getCreatedAt() * 1000)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String conversurl = announcement.getCoversUrl();
        if (TextUtils.isEmpty(conversurl)) {
            converImg.setVisibility(View.GONE);
        } else {
            converImg.setVisibility(View.VISIBLE);
            GlideUtil.loadAvatarRound(converImg, conversurl);
        }

        String content = announcement.getContent();
        if (TextUtils.isEmpty(content)) {
            contentTxt.setVisibility(View.GONE);
        } else {
            contentTxt.setVisibility(View.VISIBLE);
            contentTxt.setText(String.valueOf(content));
        }

        sysAdLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }
}