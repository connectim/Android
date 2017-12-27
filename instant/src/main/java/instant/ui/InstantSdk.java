package instant.ui;

import android.content.Context;

import instant.bean.Session;
import instant.bean.UserCookie;
import instant.sender.SenderManager;
import instant.ui.service.RemoteServeice;
import instant.ui.service.SenderService;
import instant.utils.log.LogManager;

/**
 * Created by Administrator on 2017/9/22.
 */
public class InstantSdk {

    public static InstantSdk instantSdk = getInstance();

    private static final String TAG = "_InstantSdk";
    private Context context;

    private synchronized static InstantSdk getInstance() {
        if (instantSdk == null) {
            instantSdk = new InstantSdk();
        }
        return instantSdk;
    }

    public void initSdk(Context context) {
        this.context = context;
    }

    public void registerUserInfo(Context context, String uid,String token) {
        UserCookie userCookie = new UserCookie();
        LogManager.getLogger().d(TAG, "uid :" + uid + "   token : " + token);
        userCookie.setUid(uid);
        userCookie.setToken(token);
        Session.getInstance().setConnectCookie(userCookie);

        SenderService.startService(context);
        RemoteServeice.startService(context);
    }

    public Context getBaseContext() {
        return context;
    }

    public void stopInstant(){
        SenderManager.getInstance().exitAccount();
    }
}
