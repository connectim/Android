package connect.activity.set;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.activity.set.bean.PrivateSetBean;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.permission.PermissionUtil;
import connect.utils.StringUtil;
import connect.utils.system.SystemDataUtil;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.activity.contact.bean.PhoneContactBean;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * User privacy Settings.
 */
public class PrivateActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.find_phone_tb)
    View findPhoneTb;
    @Bind(R.id.find_address_tb)
    View findAddressTb;
    @Bind(R.id.black_list_ll)
    LinearLayout blackListLl;
    @Bind(R.id.contacts_time_tv)
    TextView contactsTimeTv;
    @Bind(R.id.contacts_update_img)
    ImageView contactsUpdateImg;
    @Bind(R.id.find_recommend_tb)
    View findRecommendTb;

    private PrivateActivity mActivity;
    private PrivateSetBean privateSetBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_private);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Privacy);

        privateSetBean = ParamManager.getInstance().getPrivateSet();
        if (privateSetBean != null) {
            findPhoneTb.setSelected(privateSetBean.getPhoneFind());
            findAddressTb.setSelected(privateSetBean.getAddressFind());
            if (!TextUtils.isEmpty(privateSetBean.getUpdateTime())) {
                contactsTimeTv.setText(mActivity.getString(R.string.Set_Updated_time,privateSetBean.getUpdateTime()));
            }
            findRecommendTb.setSelected(privateSetBean.getRecommend());
        }
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.contacts_update_img)
    void updateContact(View view) {
        PermissionUtil.getInstance().requestPermissom(mActivity,new String[]{PermissionUtil.PERMISSIM_CONTACTS},permissionCallBack);
    }

    @OnClick(R.id.black_list_ll)
    void goBlackList(View view) {
        ActivityUtil.next(mActivity, PrivateBlackActivity.class);
    }

    @OnClick(R.id.find_phone_tb)
    void switchFriendPhone(View view) {
        boolean isSelect = findPhoneTb.isSelected();
        findPhoneTb.setSelected(!isSelect);
        privateSetBean.setPhoneFind(!isSelect);
        requestPrivate();
    }

    @OnClick(R.id.find_address_tb)
    void switchFriendAddress(View view) {
        boolean isSelect = findAddressTb.isSelected();
        findAddressTb.setSelected(!isSelect);
        privateSetBean.setAddressFind(!isSelect);
        requestPrivate();
    }

    @OnClick(R.id.find_recommend_tb)
    void switchRecommend(View view) {
        boolean isSelect = findRecommendTb.isSelected();
        findRecommendTb.setSelected(!isSelect);
        privateSetBean.setRecommend(!isSelect);
        requestPrivate();
    }

    private PermissionUtil.ResultCallBack permissionCallBack = new PermissionUtil.ResultCallBack() {
        @Override
        public void granted(String[] permissions) {
            requestContact();
        }

        @Override
        public void deny(String[] permissions) {}
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(mActivity,requestCode,permissions,grantResults,permissionCallBack);
    }

    /**
     * Synchronize the local contacts
     */
    private void requestContact() {
        new AsyncTask<Void, Void, Connect.PhoneBook>() {
            @Override
            protected Connect.PhoneBook doInBackground(Void... params) {
                return getPhoneHmacBook();
            }

            @Override
            protected void onPostExecute(Connect.PhoneBook phoneBook) {
                super.onPostExecute(phoneBook);
                if (phoneBook == null) {
                    ToastEUtil.makeText(mActivity,R.string.Link_contact_loading_failed_check_the_contact_pression,
                            ToastEUtil.TOAST_STATUS_FAILE).show();
                    return;
                }
                // Display synchronous animation
                RotateAnimation animation = new RotateAnimation(0f,360f * 3, Animation.RELATIVE_TO_SELF,
                        0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                animation.setDuration(1000);
                animation.setFillAfter(false);
                contactsUpdateImg.startAnimation(animation);

                syncPhone(phoneBook);
            }
        }.execute();
    }

    /**
     * Synchronize the encrypted phone number
     *
     * @param phoneBook phone data
     */
    private void syncPhone(Connect.PhoneBook phoneBook){
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_PHONE_SYNC, phoneBook, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                privateSetBean.setUpdateTime(TimeUtil.getCurrentTimeInString(TimeUtil.DEFAULT_DATE_FORMAT));
                ParamManager.getInstance().putPrivateSet(privateSetBean);
                contactsTimeTv.setText(mActivity.getString(R.string.Set_Updated_time,privateSetBean.getUpdateTime()));
                ToastEUtil.makeText(mActivity,R.string.Login_Update_successful,ToastEUtil.TOAST_STATUS_SUCCESS).show();
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(mActivity,R.string.Login_Updated_failed,ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }

    /**
     * Update the new privacy Settings
     */
    private void requestPrivate() {
        Connect.Privacy privacy = Connect.Privacy.newBuilder()
                .setPhoneNum(privateSetBean.getPhoneFind())
                .setAddress(privateSetBean.getAddressFind())
                .setRecommend(privateSetBean.getRecommend())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_PRIVACY, privacy, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                ParamManager.getInstance().putPrivateSet(privateSetBean);
            }

            @Override
            public void onError(Connect.HttpResponse response) {}
        });
    }

    /**
     * Access to local contacts, and to do Hmac phone number
     *
     * @return Phone number after I finish Hmac the PhoneBook
     */
    private Connect.PhoneBook getPhoneHmacBook(){
        // For local contacts
        List<PhoneContactBean> list = SystemDataUtil.getLoadAddresSbook(mActivity);
        if (null == list || list.size() == 0) {
            return null;
        }
        // Do the Hmac to telephone number
        Connect.PhoneBook.Builder builder = Connect.PhoneBook.newBuilder();
        for (int i = 0; i < list.size(); i++) {
            String phone = StringUtil.filterNumber(list.get(i).getPhone());
            String phoneHmac = SupportKeyUril.hmacSHA512(phone, SupportKeyUril.HmacSalt);
            Connect.PhoneInfo phoneInfo = Connect.PhoneInfo.newBuilder()
                    .setMobile(phoneHmac)
                    .build();
            builder.addMobiles(phoneInfo);
        }
        Connect.PhoneBook phoneBook = builder.build();
        return phoneBook;
    }

}
