package connect.activity.set.presenter;

import android.widget.Toast;

import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.activity.set.UserInfoNameActivity;
import connect.activity.set.contract.UserInfoNameContract;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

public class UserInfoNamePresenter implements UserInfoNameContract.Presenter{

    private final String type;
    private UserInfoNameContract.View mView;

    public UserInfoNamePresenter(UserInfoNameContract.View mView, String type) {
        this.mView = mView;
        this.type = type;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        initData();
    }

    @Override
    public void initData() {
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        int titleResId = 0;
        int hintResId = 0;
        String text = "";
        if (type.equals(UserInfoNameActivity.TYPE_NAME)) {
            titleResId = R.string.Set_Name;
            hintResId = R.string.Set_Name;
            text = userBean.getName();
        } else if (type.equals(UserInfoNameActivity.TYPE_NUMBER)) {
            titleResId = R.string.Set_ID;
            hintResId = R.string.Set_ID;
            text = userBean.getConnectId();
        }
        mView.setInitView(titleResId,hintResId,text);
    }

    @Override
    public void requestName(final String value) {
        Connect.SettingUserInfo avatar = Connect.SettingUserInfo.newBuilder()
                .setUsername(value)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_SETTING_USERINFO, avatar, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                userBean.setName(value);
                SharedPreferenceUtil.getInstance().putUser(userBean);
                mView.setFinish();
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                if (response.getCode() == 2102) {
                    Toast.makeText(mView.getActivity(),R.string.Login_username_already_exists,Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public void requestID(final String value) {
        Connect.ConnectId connectId = Connect.ConnectId.newBuilder()
                .setConnectId(value)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_SETTING_CONNECTID, connectId,
                new ResultCall<Connect.HttpNotSignResponse>() {
                    @Override
                    public void onResponse(Connect.HttpNotSignResponse response) {
                        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                        userBean.setConnectId(value);
                        SharedPreferenceUtil.getInstance().putUser(userBean);
                        mView.setFinish();
                    }

                    @Override
                    public void onError(Connect.HttpNotSignResponse response) {
                        response.getCode();
                    }
                });
    }

}