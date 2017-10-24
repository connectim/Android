package connect.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.contact.adapter.AddPhoneAdapter;
import connect.activity.contact.bean.PhoneContactBean;
import connect.activity.contact.bean.SourceType;
import connect.activity.contact.contract.AddFriendPhoneContract;
import connect.activity.contact.presenter.AddFriendPhonePresenter;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.permission.PermissionUtil;
import connect.utils.system.SystemUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;

/**
 * Add a phone book friends
 */
public class AddFriendPhoneActivity extends BaseActivity implements AddFriendPhoneContract.View {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.siderbar)
    SideBar siderbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private LinearLayoutManager linearLayoutManager;
    private AddFriendPhoneActivity mActivity;
    private AddFriendPhoneContract.Presenter presenter;
    private AddPhoneAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_addfriend_phone);
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
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_Contacts);
        toolbar.setRightText(R.string.Link_Invite);
        toolbar.setRightTextColor(R.color.color_00c400);
        toolbar.setRightTextEnable(false);
        new AddFriendPhonePresenter(this).start();

        linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        adapter = new AddPhoneAdapter(mActivity);
        adapter.setOnSelectListener(onSelectListener);
        recyclerview.setAdapter(adapter);
        siderbar.setOnTouchingLetterChangedListener(letterChangedListener);
        PermissionUtil.getInstance().requestPermission(mActivity, new String[]{PermissionUtil.PERMISSION_CONTACTS}, permissomCallBack);
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_text)
    void goinvite(View view) {
        PermissionUtil.getInstance().requestPermission(mActivity, new String[]{PermissionUtil.PERMISSION_SMS}, permissomCallBack);
    }

    SideBar.OnTouchingLetterChangedListener letterChangedListener = new SideBar.OnTouchingLetterChangedListener() {
        @Override
        public void onTouchingLetterChanged(String s) {
            int position = adapter.getPositionForSection(s.charAt(0));
            if (position >= 0) {
                linearLayoutManager.scrollToPosition(position);
            }
        }
    };

    private PermissionUtil.ResultCallBack permissomCallBack = new PermissionUtil.ResultCallBack() {
        @Override
        public void granted(String[] permissions) {
            if (permissions != null && permissions.length > 0) {
                if (permissions[0].equals(PermissionUtil.PERMISSION_CONTACTS)) {
                    presenter.requestContact();
                } else if (permissions[0].equals(PermissionUtil.PERMISSION_SMS)) {
                    String numbers = "";
                    List<PhoneContactBean> seleList = adapter.getSeleList();
                    for (PhoneContactBean contactBean : seleList) {
                        numbers = numbers + contactBean.getPhone() + ";";
                    }
                    SystemUtil.sendPhoneSMS(mActivity, numbers, getString(R.string.Link_invite_encrypted_chat_with_APP_Download,
                            SharedPreferenceUtil.getInstance().getUser().getName()));
                }
            }
        }

        @Override
        public void deny(String[] permissions) {}
    };

    private AddPhoneAdapter.OnSelectListener onSelectListener = new AddPhoneAdapter.OnSelectListener() {
        @Override
        public void selectFriend(List<PhoneContactBean> list) {
            if (list.size() == 0) {
                toolbar.setRightText(R.string.Link_Invite);
                toolbar.setRightTextEnable(false);
            } else {
                toolbar.setRightText(getString(R.string.Link_Invite_Count, list.size()));
                toolbar.setRightTextEnable(true);
            }
        }

        @Override
        public void addFriend(int position, PhoneContactBean contactBean) {
            StrangerInfoActivity.startActivity(mActivity, contactBean.getAddress(), SourceType.CONTACT);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(mActivity, requestCode, permissions, grantResults, permissomCallBack);
    }

    @Override
    public void setPresenter(AddFriendPhoneContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void updateView(int size, List<PhoneContactBean> list) {
        adapter.setServerSize(size);
        adapter.setDataNotify(list);
    }

}
