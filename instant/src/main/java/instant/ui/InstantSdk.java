package instant.ui;

import android.content.Context;

import instant.bean.Session;
import instant.bean.UserCookie;
import instant.sender.SenderManager;
import instant.ui.service.RemoteServeice;
import instant.ui.service.SenderService;

/**
 * Created by Administrator on 2017/9/22.
 */
public class InstantSdk {

    public static InstantSdk instantSdk = getInstance();

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

    public void registerUserInfo(Context context, String uid,String publicKey, String privateKey) {
        UserCookie userCookie = new UserCookie();
        userCookie.setUid(uid);
        userCookie.setPubKey(publicKey);
        userCookie.setPriKey(privateKey);
        Session.getInstance().setUserCookie(Session.CONNECT_USER, userCookie);

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
