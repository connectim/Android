package connect.activity.chat.model;

import android.app.Activity;

import connect.activity.chat.ChatActivity;
import connect.database.green.DaoHelper.ContactHelper;
import connect.utils.ActivityUtil;

/**
 * Created by Administrator on 2017/11/17.
 */

public class ChatUtils {

    public static void exceptionGroup(Activity activity,String groupIdentify){
        ContactHelper.getInstance().removeGroupEntity(groupIdentify);
        ActivityUtil.backActivityWithClearTop(activity, ChatActivity.class);
    }
}
