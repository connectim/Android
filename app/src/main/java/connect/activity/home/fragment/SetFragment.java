package connect.activity.home.fragment;

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

import com.google.protobuf.InvalidProtocolBufferException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseFragment;
import connect.activity.home.bean.HomeAction;
import connect.activity.login.bean.UserBean;
import connect.activity.set.AboutActivity;
import connect.activity.set.GeneralActivity;
import connect.activity.set.SupportFeedbackActivity;
import connect.activity.set.UserInfoActivity;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProgressUtil;
import connect.utils.StringUtil;
import connect.utils.UriUtil;
import connect.utils.dialog.DialogUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.ResultCall;
import connect.utils.system.SystemDataUtil;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Set the main interface.
 */
public class SetFragment extends BaseFragment {

    @Bind(R.id.ivAvatar)
    ImageView ivAvatar;
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
    @Bind(R.id.version_text)
    TextView versionText;

    private FragmentActivity mActivity;

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

        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        GlideUtil.loadAvatarRound(ivAvatar, userBean.getAvatar());

        tvName.setText(userBean.getName());
        tvId.setText(userBean.getO_u());
        getVersion();
    }

    @OnClick(R.id.llUserMsg)
    void intoUserInfo(View view) {
        if (mActivity != null && isAdded()) {
            ActivityUtil.next(mActivity, UserInfoActivity.class);
        }
    }

    @OnClick(R.id.llSafety)
    void intoSafety(View view) {
        // ActivityUtil.next(mActivity, SafetyActivity.class);
    }

    @OnClick(R.id.llPrivate)
    void intoPrivate(View view) {
        // ActivityUtil.next(mActivity, PrivateActivity.class);
    }

    @OnClick(R.id.llChatSetting)
    void intoChatSetting(View view) {
        ActivityUtil.next(mActivity, GeneralActivity.class);
    }

    @OnClick(R.id.llProblem)
    void intoProblem(View view) {
        //ActivityUtil.next(mActivity, SupportActivity.class);
        ActivityUtil.next(mActivity, SupportFeedbackActivity.class);
    }

    @OnClick(R.id.llAbout)
    void intoAbout(View view) {
        AboutActivity.startActivity(mActivity);
    }

    @OnClick(R.id.address_scan_img)
    void showScanAddress(View view) {
        //ActivityUtil.next(mActivity, UserConnectIdActivity.class);
    }

    @OnClick(R.id.log_out_tv)
    void logOut(View view) {
        DialogUtil.showAlertTextView(mActivity,
                mActivity.getResources().getString(R.string.Set_tip_title),
                mActivity.getResources().getString(R.string.Set_Logout_delete_login_data_still_log),
                "", "", false, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        ProgressUtil.getInstance().showProgress(mActivity, R.string.Set_Logging_out);
                        HomeAction.getInstance().sendEvent(HomeAction.HomeType.DELAY_EXIT);
                    }

                    @Override
                    public void cancel() {
                    }
                });
    }

    private void getVersion() {
        Connect.VersionRequest versionRequest = Connect.VersionRequest.newBuilder()
                .setCategory(2)
                .setPlatform(2)
                .setProtocolVersion(1)
                .setVersion(SystemDataUtil.getVersionName(mActivity))
                .build();
        HttpRequest.getInstance().post(UriUtil.CONNECT_V1_VERSION, versionRequest, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody().toByteArray());
                    Connect.VersionResponse versionResponse = Connect.VersionResponse.parseFrom(structData.getPlainData());
                    if (!TextUtils.isEmpty(versionResponse.getVersion())) {
                        int compareInt = StringUtil.VersionComparison(versionResponse.getVersion(), SystemDataUtil.getVersionName(mActivity));
                        if(versionText!=null){
                        switch (compareInt) {
                            case 1:
                                versionText.setText(mActivity.getString(R.string.Set_new_version, versionResponse.getVersion()));
                                break;
                            case 0:
                                versionText.setText(R.string.Set_This_is_the_newest_version);
                            case -1:
                                versionText.setText(R.string.Set_This_is_the_newest_version);
                                break;
                        }}
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {

            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
