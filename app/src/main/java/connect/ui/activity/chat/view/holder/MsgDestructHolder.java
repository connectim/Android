package connect.ui.activity.chat.view.holder;

import android.view.View;

import connect.ui.activity.chat.bean.MsgEntity;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgDestructHolder extends MsgBaseHolder{

    public MsgDestructHolder(View itemView) {
        super(itemView);
    }
    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, MsgEntity entity) {
        super.buildRowData(msgBaseHolder, entity);
    }
}
