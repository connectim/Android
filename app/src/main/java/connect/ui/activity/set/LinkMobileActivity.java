package connect.ui.activity.set;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProgressUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import protos.Connect;

/**
 * Unbundling or binding mobile phone
 * Created by Administrator on 2016/12/2.
 */
public class LinkMobileActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.hint_tv)
    TextView hintTv;
    @Bind(R.id.mobile_tv)
    TextView mobileTv;
    @Bind(R.id.link_btn)
    Button linkBtn;

    private LinkMobileActivity mActivity;
    private UserBean userBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_link);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Link_Mobile);

        userBean = SharedPreferenceUtil.getInstance().getUser();
        if(TextUtils.isEmpty(userBean.getPhone())){//link
            hintTv.setText(R.string.Set_Not_connected_to_mobile_network);
            linkBtn.setText(R.string.Set_Add_mobile);
            mobileTv.setText("");
        }else{//linked
            hintTv.setText(R.string.Set_Your_cell_phone_number);
            linkBtn.setText(R.string.Set_Change_Mobile);
            toolbarTop.setRightImg(R.mipmap.menu_white);
            mobileTv.setText("+" + userBean.getPhone());
        }
    }

    @OnClick(R.id.left_img)
    void goback(View view){
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void rightMore(View view){
        ArrayList list = new ArrayList<>();
        list.add(mActivity.getResources().getString(R.string.Set_Unlink));
        DialogUtil.showBottomListView(mActivity,list,new DialogUtil.DialogListItemClickListener(){
            @Override
            public void confirm(AdapterView<?> parent, View view, int position) {
                switch (position){
                    case 0:
                        unLinkPhone();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @OnClick(R.id.link_btn)
    void linkBtn(View view){
        LinkChangePhoneActivity.startActivity(mActivity,LinkChangePhoneActivity.LINK_TYPE);
    }

    private void unLinkPhone(){
        DialogUtil.showAlertTextView(mActivity,
                mActivity.getResources().getString(R.string.Set_Unlink_your_mobile_phone),
                mActivity.getResources().getString(R.string.Set_unlink_Connect_not_find_friend_your_backup_deleted),
                "", "", false, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        requestBindMobile();
                    }

                    @Override
                    public void cancel() {

                    }
                });
    }

    private void requestBindMobile(){
        ProgressUtil.getInstance().showProgress(mActivity);
        String[] phoneArray = userBean.getPhone().split("-");
        Connect.MobileVerify mobileVerify = Connect.MobileVerify.newBuilder()
                .setCountryCode(Integer.valueOf(phoneArray[0]))
                .setNumber(phoneArray[1])
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_UNBIND_MOBILE, mobileVerify, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                UserBean userBean = new Gson().fromJson(SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.USER_INFO), UserBean.class);
                userBean.setPhone("");
                SharedPreferenceUtil.getInstance().updataUser(userBean);
                ProgressUtil.getInstance().dismissProgress();
                initView();
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                ProgressUtil.getInstance().dismissProgress();
            }
        });
    }

}
