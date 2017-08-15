package connect.activity.chat.view.holder;

import android.view.View;

import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.MsgExtEntity;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgDestructHolder extends MsgBaseHolder{

    public MsgDestructHolder(View itemView) {
        super(itemView);
    }
    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, MsgExtEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
    }
}
