package connect.ui.activity.home.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.bean.ContactEntity;
import connect.im.model.FailMsgsManager;
import connect.im.msgdeal.SendMsgUtil;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgChatReceiver;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.MsgSender;
import connect.ui.activity.chat.bean.Talker;
import connect.ui.activity.chat.model.ChatMsgUtil;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.NormalChat;
import connect.ui.activity.home.bean.HomeAction;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.set.AboutActivity;
import connect.ui.activity.set.AddressActivity;
import connect.ui.activity.set.GeneralActivity;
import connect.ui.activity.set.PrivateActivity;
import connect.ui.activity.set.SafetyActivity;
import connect.ui.activity.set.SupportActivity;
import connect.ui.activity.set.ModifyInfoActivity;
import connect.ui.base.BaseApplication;
import connect.ui.base.BaseFragment;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ExCountDownTimer;
import connect.utils.ProgressUtil;
import connect.utils.TimeUtil;
import connect.utils.glide.GlideUtil;
import connect.view.TopToolBar;
import connect.view.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 * setting
 * Created by john on 2016/11/28.
 */
public class SetFragment extends BaseFragment {

    @Bind(R.id.ivAvatar)
    RoundedImageView ivAvatar;
    @Bind(R.id.tvName)
    TextView tvName;
    @Bind(R.id.tvId)
    TextView tvId;
    @Bind(R.id.llUserMsg)
    RelativeLayout llUserMsg;
    @Bind(R.id.llSafety)
    LinearLayout llSafety;
    @Bind(R.id.llPrivate)
    LinearLayout llPrivate;
    @Bind(R.id.llChatSetting)
    LinearLayout llChatSetting;
    @Bind(R.id.llProblem)
    LinearLayout llProblem;
    @Bind(R.id.llAbout)
    LinearLayout llAbout;
    @Bind(R.id.log_out_tv)
    TextView logOutTv;
    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.address_scan_img)
    ImageView addressScanImg;

    private FragmentActivity mActivity;
    private UserBean userBean;

    public static SetFragment startFragment() {
        SetFragment setFragment = new SetFragment();
        return setFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
    }

    private void initView() {
        toolbarTop.setBlackStyle();
        toolbarTop.setTitle(null, R.string.Set_Setting);

        userBean = SharedPreferenceUtil.getInstance().getUser();
        GlideUtil.loadAvater(ivAvatar, userBean.getAvatar());
        tvName.setText(userBean.getName());

        if(TextUtils.isEmpty(userBean.getConnectId())){
            tvId.setText(userBean.getAddress());
        }else{
            tvId.setText(userBean.getConnectId());
        }
    }

    @OnClick(R.id.llUserMsg)
    void intoUserInfo(View view) {
        if(mActivity != null && isAdded()){
            ActivityUtil.next(mActivity, ModifyInfoActivity.class);
        }
    }

    @OnClick(R.id.llSafety)
    void intoSafety(View view) {
        ActivityUtil.next(mActivity, SafetyActivity.class);
    }

    @OnClick(R.id.llPrivate)
    void intoPrivate(View view) {
        ActivityUtil.next(mActivity, PrivateActivity.class);
    }

    @OnClick(R.id.llChatSetting)
    void intoChatSetting(View view) {
        ActivityUtil.next(mActivity, GeneralActivity.class);
    }

    @OnClick(R.id.llProblem)
    void intoProblem(View view) {
        ActivityUtil.next(mActivity, SupportActivity.class);
    }

    @OnClick(R.id.llAbout)
    void intoAbout(View view) {
        AboutActivity.startActivity(mActivity);
    }

    @OnClick(R.id.log_out_tv)
    void logOut(View view) {
        DialogUtil.showAlertTextView(mActivity,
                mActivity.getResources().getString(R.string.Set_tip_title),
                mActivity.getResources().getString(R.string.Set_Logout_delete_login_data_still_log),
                "", "", false, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        ProgressUtil.getInstance().showProgress(mActivity,R.string.Set_Logging_out);
                        SendMsgUtil.connectLogout();
                        HomeAction.sendTypeMsg(HomeAction.HomeType.DELAY_EXIT);
                    }

                    @Override
                    public void cancel() {

                    }
                });
    }

    @OnClick(R.id.address_scan_img)
    void showScanAddress(View view) {
        ActivityUtil.next(mActivity, AddressActivity.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
