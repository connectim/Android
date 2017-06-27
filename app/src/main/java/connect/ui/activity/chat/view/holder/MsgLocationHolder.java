package connect.ui.activity.chat.view.holder;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.view.BubbleImg;
import connect.ui.activity.chat.exts.GoogleMapActivity;
import connect.ui.activity.chat.bean.GeoAddressBean;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgLocationHolder extends MsgChatHolder {
    private BubbleImg imgmsg;
    private TextView textView;

    public MsgLocationHolder(View itemView) {
        super(itemView);
        imgmsg = (BubbleImg) itemView.findViewById(R.id.imgmsg);
        textView = (TextView) itemView.findViewById(R.id.txt);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgEntity entity) {
        super.buildRowData(msgBaseHolder, entity);
        MsgDefinBean bean = entity.getMsgDefinBean();
        final GeoAddressBean geoAddres = bean.getLocationExt();
        String url = TextUtils.isEmpty(bean.getContent()) ? bean.getUrl() : bean.getContent();

        textView.setText(geoAddres.getAddress());
        imgmsg.loadUri(direct, entity.getPubkey(), bean.getMessage_id(), url,definBean.getImageOriginWidth(),definBean.getImageOriginHeight());
        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleMapActivity.startActivity((Activity) context,geoAddres.getLocationLatitude(), geoAddres.getLocationLongitude());
            }
        });
    }
}
