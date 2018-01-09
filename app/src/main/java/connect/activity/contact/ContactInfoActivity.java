package connect.activity.contact;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.Talker;
import connect.activity.company.adapter.DepartmentBean;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.permission.PermissionUtil;
import connect.widget.DepartmentAvatar;
import connect.widget.TopToolBar;
import protos.Connect;


public class ContactInfoActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.name_text)
    TextView nameText;
    @Bind(R.id.sign_tv)
    TextView signTv;
    @Bind(R.id.number_tv)
    TextView numberTv;
    @Bind(R.id.department_tv)
    TextView departmentTv;
    @Bind(R.id.phone_tv)
    TextView phoneTv;
    @Bind(R.id.cell_image)
    ImageView cellImage;
    @Bind(R.id.chat_btn)
    Button chatBtn;
    @Bind(R.id.avatar_image)
    DepartmentAvatar avatarImage;
    @Bind(R.id.gender_image)
    ImageView genderImage;

    private ContactInfoActivity mActivity;
    private DepartmentBean departmentBean;

    public static void lunchActivity(Activity activity, DepartmentBean departmentBean, String department) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", departmentBean);
        bundle.putString("department", department);
        ActivityUtil.next(activity, ContactInfoActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Chat_Contact_details);

        departmentBean = (DepartmentBean) getIntent().getExtras().getSerializable("bean");
        departmentBean.setO_u(getIntent().getExtras().getString("department"));
        avatarImage.setAvatarName(departmentBean.getName(), false, departmentBean.getGender());
        nameText.setText(departmentBean.getName());
        if(departmentBean.getGender() == 1){
            genderImage.setImageResource(R.mipmap.man);
        }else{
            genderImage.setImageResource(R.mipmap.woman);
        }
        signTv.setText(departmentBean.getTips());
        numberTv.setText(departmentBean.getEmpNo());
        departmentTv.setText(departmentBean.getO_u());
        phoneTv.setText(departmentBean.getMobile());
        if (!departmentBean.getRegisted()) {
            chatBtn.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.cell_image)
    void call(View view) {
        PermissionUtil.getInstance().requestPermission(mActivity, new String[]{PermissionUtil.PERMISSION_PHONE}, permissionCallBack);
    }

    @OnClick(R.id.chat_btn)
    void chat(View view) {
        Talker talker = new Talker(Connect.ChatType.PRIVATE, departmentBean.getUid());
        talker.setAvatar(departmentBean.getAvatar());
        talker.setNickName(departmentBean.getName());
        talker.setFriendPublicKey(departmentBean.getPub_key());
        ChatActivity.startActivity(mActivity, talker);
    }

    private PermissionUtil.ResultCallBack permissionCallBack = new PermissionUtil.ResultCallBack() {
        @Override
        public void granted(String[] permissions) {
            if (permissions != null && permissions.length > 0) {
                if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + departmentBean.getMobile()));
                mActivity.startActivity(intent);
            }
        }

        @Override
        public void deny(String[] permissions) {}
    };

}
