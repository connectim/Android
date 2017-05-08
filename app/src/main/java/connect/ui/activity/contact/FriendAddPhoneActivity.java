package connect.ui.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.contact.adapter.AddPhoneAdapter;
import connect.ui.activity.contact.bean.SourceType;
import connect.ui.activity.contact.contract.FriendAddContract;
import connect.ui.activity.contact.presenter.FriendAddPresenter;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.permission.PermissiomUtilNew;
import connect.utils.system.SystemUtil;
import connect.ui.activity.contact.bean.PhoneContactBean;
import connect.view.SideBar;
import connect.view.TopToolBar;

/**
 * Add a phone book friends
 * Created by Administrator on 2016/12/29.
 */
public class FriendAddPhoneActivity extends BaseActivity implements FriendAddContract.View {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.list_view)
    ListView listView;
    @Bind(R.id.siderbar)
    SideBar siderbar;

    private FriendAddPhoneActivity mActivity;
    private FriendAddContract.Presenter presenter;
    private UserBean userBean;
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
        setPresenter(new FriendAddPresenter(this));

        userBean = SharedPreferenceUtil.getInstance().getUser();
        adapter = new AddPhoneAdapter();
        adapter.setOnSeleListence(onSeleListence);
        listView.setAdapter(adapter);
        siderbar.setOnTouchingLetterChangedListener(letterChangedListener);
        PermissiomUtilNew.getInstance().requestPermissom(mActivity,new String[]{PermissiomUtilNew.PERMISSIM_CONTACTS},permissomCallBack);
    }

    @Override
    public void setPresenter(FriendAddContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_text)
    void goinvite(View view) {
        PermissiomUtilNew.getInstance().requestPermissom(mActivity,new String[]{PermissiomUtilNew.PERMISSIM_SMS},permissomCallBack);
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void updataView(int size, List<PhoneContactBean> list) {
        adapter.setServerSize(size);
        adapter.setDataNotify(list);
    }

    SideBar.OnTouchingLetterChangedListener letterChangedListener = new SideBar.OnTouchingLetterChangedListener() {
        @Override
        public void onTouchingLetterChanged(String s) {
            int position = adapter.getPositionForSection(s.charAt(0));
            if(position >= 0){
                listView.setSelection(position);
            }
        }
    };

    private PermissiomUtilNew.ResultCallBack permissomCallBack = new PermissiomUtilNew.ResultCallBack(){
        @Override
        public void granted(String[] permissions) {
            if(permissions != null && permissions.length > 0){
                if(permissions[0].equals(PermissiomUtilNew.PERMISSIM_CONTACTS)){
                    presenter.requestContact();
                }else if(permissions[0].equals(PermissiomUtilNew.PERMISSIM_SMS)){
                    String numbers = "";
                    List<PhoneContactBean> seleList = adapter.getSeleList();
                    for(PhoneContactBean contactBean : seleList){
                        numbers = numbers + contactBean.getPhone() + ";";
                    }
                    SystemUtil.sendPhoneSMS(mActivity, numbers, getString(R.string.Link_invite_encrypted_chat_with_APP_Download,userBean.getName()));
                }
            }
        }

        @Override
        public void deny(String[] permissions) {
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        PermissiomUtilNew.getInstance().onRequestPermissionsResult(mActivity,requestCode,permissions,grantResults,permissomCallBack);
    }

    private AddPhoneAdapter.OnSeleListence onSeleListence = new AddPhoneAdapter.OnSeleListence() {
        @Override
        public void seleFriend(List<PhoneContactBean> list) {
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

}
