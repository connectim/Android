package connect.activity.chat.view.holder;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import connect.ui.activity.R;
import connect.activity.chat.bean.AdBean;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.exts.OuterWebsiteActivity;
import connect.activity.set.AboutActivity;
import connect.utils.TimeUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.roundedimageview.RoundedImageView;

/**
 * Created by pujin on 2017/3/24.
 */

public class MsgSysAdHolder extends MsgBaseHolder {

    private LinearLayout sysAdLayout;
    private TextView titleTxt;
    private TextView timeTxt;
    private RoundedImageView converImg;
    private TextView contentTxt;

    private AdBean adBean = null;

    public MsgSysAdHolder(View itemView) {
        super(itemView);
        sysAdLayout = (LinearLayout) itemView.findViewById(R.id.linearlayout);
        titleTxt = (TextView) itemView.findViewById(R.id.txt1);
        timeTxt = (TextView) itemView.findViewById(R.id.txt2);
        converImg = (RoundedImageView) itemView.findViewById(R.id.roundimg1);
        contentTxt = (TextView) itemView.findViewById(R.id.txt3);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgEntity entity) {
        super.buildRowData(msgBaseHolder, entity);
        adBean = new Gson().fromJson(entity.getMsgDefinBean().getContent(), AdBean.class);

        titleTxt.setText(String.valueOf(adBean.getTitle()));
        try {
            timeTxt.setText(TimeUtil.getMsgTime(TimeUtil.getCurrentTimeInLong(), adBean.getCreateTime() * 1000));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(adBean.getConversUrl())) {
            converImg.setVisibility(View.GONE);
        } else {
            converImg.setVisibility(View.VISIBLE);
            GlideUtil.loadImage(converImg, adBean.getConversUrl());
        }
        if (TextUtils.isEmpty(adBean.getContent())) {
            contentTxt.setVisibility(View.GONE);
        } else {
            contentTxt.setVisibility(View.VISIBLE);
            contentTxt.setText(String.valueOf(adBean.getContent()));
        }

        sysAdLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (adBean.getCategory()) {
                    case 0://link
                        String url=adBean.getUrl();
                        if (!TextUtils.isEmpty(url)) {
                            OuterWebsiteActivity.startActivity((Activity) context, adBean.getUrl());
                        }
                        break;
                    case 1://update
                        AboutActivity.startActivity((Activity) context);
                        break;
                }
            }
        });
    }
}
