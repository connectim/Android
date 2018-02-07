package connect.activity.chat.view.holder;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import connect.activity.workbench.VisitorsActivity;
import connect.activity.workbench.WarehouseDetailActivity;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import instant.bean.ChatMsgEntity;
import instant.utils.StringUtil;
import protos.Connect;

/**
 * Created by PuJin on 2018/2/7.
 */

public class MsgWarehouseHolder extends MsgBaseHolder {

    private LinearLayout wareHouseLayout;
    private TextView titleTxt;
    private TextView timeTxt;
    private TextView contentTxt;

    private int wareType ;
    private Connect.UnRegisterNotify unRegisterNotify = null;
    private Connect.VisitorNotify visitorNotify = null;

    private Context context;
    private String title;
    private String time;
    private String content;

    public MsgWarehouseHolder(View itemView) {
        super(itemView);
        wareHouseLayout = (LinearLayout) itemView.findViewById(R.id.linearlayout);
        titleTxt = (TextView) itemView.findViewById(R.id.txt1);
        timeTxt = (TextView) itemView.findViewById(R.id.txt2);
        contentTxt = (TextView) itemView.findViewById(R.id.txt3);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final ChatMsgEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        context = msgBaseHolder.context;
        Connect.NotifyMessage notifyMessage = Connect.NotifyMessage.parseFrom(msgExtEntity.getContents());
        wareType  = notifyMessage.getNotifyType();

        switch (wareType) {
            case 20:
                visitorNotify = Connect.VisitorNotify.parseFrom(StringUtil.hexStringToBytes(notifyMessage.getContent()));
                title = context.getString(R.string.Robot_New_Visitor_Apply);
                titleTxt.setText(title);
                try {
                    timeTxt.setText(TimeUtil.getRobotContentMsgTime(TimeUtil.getCurrentTimeInLong(), (long) (visitorNotify.getTime() * 1000)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                content = context.getString(R.string.Robot_Visiting_Purpose, visitorNotify.getReason());
                contentTxt.setText(content);
                break;
            case 21:
                unRegisterNotify = Connect.UnRegisterNotify.parseFrom(StringUtil.hexStringToBytes(notifyMessage.getContent()));
                title = context.getString(R.string.Robot_Abnormal_Remind);
                titleTxt.setText(title);
                try {
                    timeTxt.setText(TimeUtil.getRobotContentMsgTime(TimeUtil.getCurrentTimeInLong(), (long) (unRegisterNotify.getTime() * 1000)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                content = context.getString(R.string.Robot_WareHouse_Warn, unRegisterNotify.getLocation());
                contentTxt.setText(content);
                break;
        }

        wareHouseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) context;
                switch (wareType) {
                    case 20:
                        VisitorsActivity.lunchActivity(activity);
                        break;
                    case 21:
                        long id = unRegisterNotify.getId();
                        WarehouseDetailActivity.lunchActivity(activity, id);
                        break;
                }
            }
        });
    }
}