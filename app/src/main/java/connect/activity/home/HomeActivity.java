package connect.activity.home;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseApplication;
import connect.activity.base.BaseFragmentActivity;
import connect.activity.chat.set.BaseGroupSelectActivity;
import connect.activity.contact.ScanAddFriendActivity;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.home.bean.HomeAction;
import connect.activity.home.bean.MsgNoticeBean;
import connect.activity.home.fragment.ContactFragment;
import connect.activity.home.fragment.ConversationFragment;
import connect.activity.home.fragment.SetFragment;
import connect.activity.home.fragment.WorkbenchFragment;
import connect.activity.home.view.CheckUpdate;
import connect.activity.login.LoginUserActivity;
import connect.activity.set.SupportFeedbackActivity;
import connect.activity.set.bean.SystemSetBean;
import connect.database.green.DaoHelper.ParamManager;
import connect.instant.bean.ConnectState;
import connect.service.GroupService;
import connect.service.UpdateInfoService;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.dialog.DialogUtil;
import connect.utils.log.LogManager;
import connect.utils.permission.PermissionUtil;
import connect.utils.scan.ResolveUrlUtil;
import connect.widget.MaterialBadgeTextView;
import instant.bean.UserOrderBean;
import instant.utils.manager.FailMsgsManager;

/**
 * Created by gtq on 2016/11/19.
 */
public class HomeActivity extends BaseFragmentActivity {

    @Bind(R.id.msg)
    ImageView msg;
    @Bind(R.id.contact)
    ImageView contact;
    @Bind(R.id.set)
    ImageView set;
    @Bind(R.id.workbench)
    ImageView workbench;
    @Bind(R.id.msg_rela)
    RelativeLayout msgRela;
    @Bind(R.id.contact_rela)
    RelativeLayout contactRela;
    @Bind(R.id.set_rela)
    RelativeLayout setRela;
    @Bind(R.id.workbench_rela)
    RelativeLayout workbenchRela;
    @Bind(R.id.badgetv)
    MaterialBadgeTextView badgetv;
    @Bind(R.id.contact_badgetv)
    MaterialBadgeTextView contactBadgetv;

    private static String TAG = "Tag_HomeActivity";
    @Bind(R.id.msg_text)
    TextView msgText;
    @Bind(R.id.contact_text)
    TextView contactText;
    @Bind(R.id.workbench_text)
    TextView workbenchText;
    @Bind(R.id.set_text)
    TextView setText;

    private HomeActivity activity;

    private ConversationFragment chatListFragment;
    private ContactFragment contactFragment;
    private WorkbenchFragment workbenchFragment;
    private SetFragment setFragment;
    private ResolveUrlUtil resolveUrlUtil;
    private CheckUpdate checkUpdata;

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, HomeActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_home);
        ButterKnife.bind(this);
        initView();
        EventBus.getDefault().register(this);
    }

    @Override
    public void initView() {
        activity = this;
        setDefaultFragment();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                BaseApplication.getInstance().initRegisterAccount();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                LogManager.getLogger().d(TAG, "onPostExecute");
                UpdateInfoService.startService(activity);
                GroupService.startService(activity);

                ConnectState.getInstance().sendEvent(ConnectState.ConnectType.CONNECT);
                requestAppUpdata();
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
                    HomeAction.getInstance().sendEvent(HomeAction.HomeType.EXIT);
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
                FailMsgsManager.getInstance().removeAllFailMsg();
                UserOrderBean userOrderBean = new UserOrderBean();
                userOrderBean.connectLogout();

                mHandler.sendEmptyMessageDelayed(TIMEOUT_DELAYEXIT, 1000);
                break;
            case EXIT:
                UserOrderBean orderBean = new UserOrderBean();
                orderBean.connectLogout();

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(1001);

                mHandler.removeMessages(TIMEOUT_DELAYEXIT);
                BaseApplication.getInstance().exitRegisterAccount();
                List<Activity> list = BaseApplication.getInstance().getActivityList();
                for (Activity activity1 : list) {
                    if (!activity1.getClass().getName().equals(activity.getClass().getName())) {
                        activity1.finish();
                    }
                }
                Intent intent = new Intent(activity, LoginUserActivity.class);
                activity.startActivity(intent);
                finish();
                break;
            case SWITCHFRAGMENT:
                int fragmentCode = (Integer) objects[0];
                switchFragment(fragmentCode);
                break;
            case GROUP_NEWCHAT:
                int position = (int) (objects[0]);
                if(position == 1){
                    BaseGroupSelectActivity.startActivity(activity, true, "");
                }else if(position == 2){
                    ActivityUtil.next(activity, ScanAddFriendActivity.class);
                }else if(position == 3){
                    SystemSetBean systemSetBean = ParamManager.getInstance().getSystemSet();
                    boolean isVibrate = systemSetBean.isVibrate();
                    boolean isRing = systemSetBean.isRing();
                    if (isVibrate && isRing) {
                        systemSetBean.setVibrate(false);
                        systemSetBean.setRing(false);
                    } else {
                        systemSetBean.setVibrate(true);
                        systemSetBean.setRing(true);
                    }
                    ParamManager.getInstance().putSystemSet(systemSetBean);
                }else if(position == 4){
                    ActivityUtil.next(activity, SupportFeedbackActivity.class);
                }
                break;
            case REMOTE_LOGIN:
                String deviceName = (String) objects[0];
                orderBean = new UserOrderBean();
                orderBean.connectLogout();

                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(1001);

                mHandler.removeMessages(TIMEOUT_DELAYEXIT);
                BaseApplication.getInstance().exitRegisterAccount();
                list = BaseApplication.getInstance().getActivityList();
                for (Activity activity1 : list) {
                    if (!activity1.getClass().getName().equals(activity.getClass().getName())) {
                        activity1.finish();
                    }
                }
                intent = new Intent(activity, LoginUserActivity.class);
                intent.putExtra("VALUE",deviceName);
                activity.startActivity(intent);
                finish();
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgNoticeBean notice) {
        Object[] objs = null;
        if (notice.object != null) {
            objs = (Object[]) notice.object;
        }
        if (objs[0] instanceof MsgSendBean) {
            resolveUrlUtil.showMsgTip(notice, ResolveUrlUtil.TYPE_OPEN_WEB, false);
        }
    }

    @OnClick({R.id.msg_rela, R.id.contact_rela, R.id.workbench_rela, R.id.set_rela})
    public void OnClickListener(View view) {
        initBottomTab();
        switch (view.getId()) {
            case R.id.msg_rela:
                switchFragment(0);
                msg.setSelected(true);
                msgText.setSelected(true);
                break;
            case R.id.contact_rela:
                switchFragment(1);
                contact.setSelected(true);
                contactText.setSelected(true);
                break;
            case R.id.workbench_rela:
                switchFragment(2);
                workbench.setSelected(true);
                workbenchText.setSelected(true);
                break;
            case R.id.set_rela:
                switchFragment(3);
                set.setSelected(true);
                setText.setSelected(true);
                break;
        }
    }

    private void initBottomTab() {
        msg.setSelected(false);
        contact.setSelected(false);
        workbench.setSelected(false);
        set.setSelected(false);

        msgText.setSelected(false);
        contactText.setSelected(false);
        workbenchText.setSelected(false);
        setText.setSelected(false);
    }

    public void setDefaultFragment() {
        chatListFragment = ConversationFragment.startFragment();
        contactFragment = ContactFragment.startFragment();
        workbenchFragment = WorkbenchFragment.startFragment();
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
                if (!workbenchFragment.isAdded()) {
                    fragmentTransaction.add(R.id.home_content, workbenchFragment);
                } else {
                    fragmentTransaction.show(workbenchFragment);
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

    private void requestAppUpdata() {
        checkUpdata = new CheckUpdate(activity);
        checkUpdata.check();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(activity, requestCode, permissions, grantResults, checkUpdata.permissomCallBack);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
