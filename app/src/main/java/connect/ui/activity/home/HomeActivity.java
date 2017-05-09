package connect.ui.activity.home;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tencent.bugly.crashreport.CrashReport;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.SharePreferenceUser;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoManager;
import connect.db.green.bean.FriendRequestEntity;
import connect.im.bean.ConnectState;
import connect.im.model.ConnectManager;
import connect.ui.activity.R;
import connect.ui.activity.chat.ChatActivity;
import connect.ui.activity.chat.bean.Talker;
import connect.ui.activity.chat.model.EmoManager;
import connect.ui.activity.contact.bean.ContactNotice;
import connect.ui.activity.contact.bean.MsgSendBean;
import connect.ui.activity.home.bean.HomeAction;
import connect.ui.activity.home.bean.MsgNoticeBean;
import connect.ui.activity.home.fragment.ChatListFragment;
import connect.ui.activity.home.fragment.ContactFragment;
import connect.ui.activity.home.fragment.SetFragment;
import connect.ui.activity.home.fragment.WalletFragment;
import connect.ui.activity.home.view.CheckUpdata;
import connect.ui.activity.login.LoginForPhoneActivity;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.wallet.support.ScanUrlAnalysisUtil;
import connect.ui.base.BaseFragmentActivity;
import connect.ui.service.HttpsService;
import connect.utils.ActivityUtil;
import connect.utils.ConfigUtil;
import connect.utils.FileUtil;
import connect.utils.log.LogManager;
import connect.utils.permission.PermissiomUtilNew;
import connect.utils.ProgressUtil;
import connect.view.MaterialBadgeTextView;

/**
 * Created by gtq on 2016/11/19.
 */
public class HomeActivity extends BaseFragmentActivity {

    @Bind(R.id.msg)
    ImageView msg;
    @Bind(R.id.contact)
    ImageView contact;
    @Bind(R.id.wallet)
    ImageView wallet;
    @Bind(R.id.set)
    ImageView set;
    @Bind(R.id.home_content)
    FrameLayout homeContent;
    @Bind(R.id.msg_rela)
    RelativeLayout msgRela;
    @Bind(R.id.contact_rela)
    RelativeLayout contactRela;
    @Bind(R.id.wallet_rela)
    RelativeLayout walletRela;
    @Bind(R.id.set_rela)
    RelativeLayout setRela;
    @Bind(R.id.badgetv)
    MaterialBadgeTextView badgetv;
    @Bind(R.id.contact_badgetv)
    MaterialBadgeTextView contactBadgetv;

    private String Tag = "HomeActivity";
    private HomeActivity activity;

    private ChatListFragment chatListFragment;
    private ContactFragment contactFragment;
    private SetFragment setFragment;
    private WalletFragment walletFragment;
    private ScanUrlAnalysisUtil analysisUtil;
    private CheckUpdata checkUpdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_home);
        ButterKnife.bind(this);
        initView();
        EventBus.getDefault().register(this);
    }

    public static void startActivity(Activity activity) {
        //String languageCode = SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.APP_LANGUAGE_CODE);
        //SystemDataUtil.setAppLanguage(activity,languageCode);
        ActivityUtil.next(activity, HomeActivity.class);
    }

    @Override
    public void initView() {
        activity = this;
        setDefaultFragment();

        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                SharePreferenceUser.initSharePreferrnce(userBean.getPubKey());
                LogManager.getLogger().d(Tag, "*** userBean.getPubKey() :" + userBean.getPubKey());

                DaoManager.getInstance().closeDataBase();
                DaoManager.getInstance().switchDataBase();
                FileUtil.getExternalStorePath();

                CrashReport.putUserData(activity, "userAddress", SharedPreferenceUtil.getInstance().getAddress());
                CrashReport.setUserSceneTag(activity, Integer.valueOf(ConfigUtil.getInstance().getCrashTags()));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                ConnectState.getInstance().sendEvent(ConnectState.ConnectType.CONNECT);
                EmoManager.getInstance();
                HttpsService.startService(activity);

                requestAppUpdata();
                checkWebOpen();
            }
        }.execute();
    }

    private static final int TIMEOUT_DELAYEXIT = 120;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TIMEOUT_DELAYEXIT:
                    HomeAction.sendTypeMsg(HomeAction.HomeType.EXIT);
                    break;
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onEventMainThread(HomeAction action) {
        Object[] objects = null;
        if (action.getObject() != null) {
            objects = (Object[]) action.getObject();
        }

        switch (action.getType()) {
            case DELAY_EXIT://Timeout logged out
                mHandler.sendEmptyMessageDelayed(TIMEOUT_DELAYEXIT, 5000);
                break;
            case EXIT:
                mHandler.removeMessages(TIMEOUT_DELAYEXIT);
                //Remove the local login information
                SharedPreferenceUtil.getInstance().remove(SharedPreferenceUtil.USER_INFO);
                //close socket
                ConnectManager.getInstance().exitConnect();
                SharePreferenceUser.unLinkSharePreferrnce();
                SharedPreferenceUtil.getInstance().clearMap();
                DaoManager.getInstance().closeDataBase();
                HttpsService.stopServer(activity);

                ProgressUtil.getInstance().dismissProgress();
                ActivityUtil.next(activity, LoginForPhoneActivity.class);
                finish();
                break;
            case TOCHAT:
                ChatActivity.startActivity(activity, (Talker) (objects[0]));
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgNoticeBean notice) {
        Object[] objs = null;
        if (notice.object != null) {
            objs = (Object[]) notice.object;
        }
        if(objs[0] instanceof MsgSendBean){
            analysisUtil.showMsgTip(notice,"web");
        }
    }

    @Subscribe
    public void onEventMainThread(ContactNotice notice) {
        if (notice.getNotice() == ContactNotice.ConNotice.RecAddFriend) {
            updataRequest();
        }
    }

    private void updataRequest(){
        new AsyncTask<Void,Void,Integer>(){
            @Override
            protected Integer doInBackground(Void... params) {
                List<FriendRequestEntity> requestList = ContactHelper.getInstance().loadFriendRequestNew();
                if(requestList == null){
                    return 0;
                }
                return requestList.size();
            }
            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                setFragmentDot(1,integer);
            }
        }.execute();
    }

    @OnClick({R.id.msg_rela, R.id.contact_rela, R.id.wallet_rela, R.id.set_rela})
    public void OnClickListener(View view) {
        initBottomTab();
        switch (view.getId()) {
            case R.id.msg_rela:
                switchFragment(0);
                msg.setSelected(true);
                break;
            case R.id.contact_rela:
                switchFragment(1);
                contact.setSelected(true);
                break;
            case R.id.wallet_rela:
                switchFragment(2);
                wallet.setSelected(true);
                break;
            case R.id.set_rela:
                switchFragment(3);
                set.setSelected(true);
                break;
        }
    }

    private void initBottomTab() {
        msg.setSelected(false);
        contact.setSelected(false);
        wallet.setSelected(false);
        set.setSelected(false);
    }

    public void setDefaultFragment() {
        chatListFragment = ChatListFragment.startFragment();
        contactFragment = ContactFragment.startFragment();
        walletFragment = WalletFragment.startFragment();
        setFragment = SetFragment.startFragment();

        switchFragment(0);
        msg.setSelected(true);
    }

    public void switchFragment(int code) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment.isVisible()) {
                    fragmentTransaction.hide(fragment);
                }
            }
        }

        switch (code) {
            case 0:
                if (!chatListFragment.isAdded()) {
                    fragmentTransaction.add(R.id.home_content, chatListFragment);
                } else {
                    fragmentTransaction.show(chatListFragment);
                }
                break;
            case 1:
                if (!contactFragment.isAdded()) {
                    fragmentTransaction.add(R.id.home_content, contactFragment);
                } else {
                    fragmentTransaction.show(contactFragment);
                }
                break;
            case 2:
                if (!walletFragment.isAdded()) {
                    fragmentTransaction.add(R.id.home_content, walletFragment);
                } else {
                    fragmentTransaction.show(walletFragment);
                }
                break;
            case 3:
                if (!setFragment.isAdded()) {
                    fragmentTransaction.add(R.id.home_content, setFragment);
                } else {
                    fragmentTransaction.show(setFragment);
                }
                break;
        }

        //commit :IllegalStateException: Can not perform this action after onSaveInstanceState
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void setFragmentDot(int pager, int count) {
        switch (pager) {
            case 0:
                if (badgetv != null) {
                    badgetv.setBadgeCount(count);
                }
                break;
            case 1:
                if (contactBadgetv != null) {
                    contactBadgetv.setBadgeCount(count);
                }
                break;
        }
    }

    private void checkWebOpen() {
        analysisUtil = new ScanUrlAnalysisUtil(activity);
        analysisUtil.checkWebOpen();
    }

    private void requestAppUpdata() {
        checkUpdata = new CheckUpdata(activity);
        checkUpdata.check();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        PermissiomUtilNew.getInstance().onRequestPermissionsResult(activity,requestCode,permissions,grantResults,checkUpdata.permissomCallBack);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //ConnectManager.getInstance().exitConnect();//close socket
        //SharePreferenceUser.unLinkSharePreferrnce();
        //SharedPreferenceUtil.getInstance().clearMap();
        EventBus.getDefault().unregister(this);
    }
}
