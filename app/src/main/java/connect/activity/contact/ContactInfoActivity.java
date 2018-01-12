package connect.activity.contact;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.Talker;
import connect.activity.contact.bean.ContactNotice;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.utils.permission.PermissionUtil;
import connect.widget.DepartmentAvatar;
import connect.widget.TopToolBar;
import instant.utils.SharedUtil;
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
    @Bind(R.id.avatar_imageview)
    ImageView avatarImageview;

    private ContactInfoActivity mActivity;
    private ContactEntity contactEntity;

    public static void lunchActivity(Activity activity, ContactEntity contactEntity) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", contactEntity);
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

        contactEntity = (ContactEntity) getIntent().getExtras().getSerializable("bean");
        nameText.setText(contactEntity.getName());
        if (contactEntity.getGender() == 1) {
            genderImage.setImageResource(R.mipmap.man);
        } else {
            genderImage.setImageResource(R.mipmap.woman);
        }
        signTv.setText(contactEntity.getTips());
        numberTv.setText(contactEntity.getEmpNo());
        departmentTv.setText(contactEntity.getOu());
        phoneTv.setText(contactEntity.getMobile());
        if (contactEntity.getRegisted()) {
            toolbar.setRightImg(R.mipmap.menu_white);
            toolbar.setRightTextEnable(true);

            avatarImageview.setVisibility(View.VISIBLE);
            avatarImage.setVisibility(View.GONE);
            GlideUtil.loadAvatarRound(avatarImageview, contactEntity.getAvatar(), 8);
        } else {
            chatBtn.setVisibility(View.GONE);
            toolbar.setRightTextEnable(false);

            avatarImageview.setVisibility(View.GONE);
            avatarImage.setVisibility(View.VISIBLE);
            avatarImage.setAvatarName(contactEntity.getName(), true, contactEntity.getGender());
        }
        searchUser(contactEntity.getUid());
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void showDialog(View view) {
        final ContactEntity contactEntity1 = ContactHelper.getInstance().loadFriendByUid(contactEntity.getUid());
        String message;
        if (contactEntity1 == null) {
            message = mActivity.getResources().getString(R.string.Link_Add_focus);
        } else {
            message = mActivity.getResources().getString(R.string.Link_Cancle_focus);
        }
        ArrayList<String> list = new ArrayList<>();
        list.add(message);
        DialogUtil.showBottomView(mActivity, list, new DialogUtil.DialogListItemClickListener() {
            @Override
            public void confirm(int position) {
                switch (position) {
                    case 0:
                        if (contactEntity1 == null) {
                            addFollow(true);
                        } else {
                            addFollow(false);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @OnClick(R.id.cell_image)
    void call(View view) {
        PermissionUtil.getInstance().requestPermission(mActivity, new String[]{PermissionUtil.PERMISSION_PHONE}, permissionCallBack);
    }

    @OnClick(R.id.chat_btn)
    void chat(View view) {
        Talker talker = new Talker(Connect.ChatType.PRIVATE, contactEntity.getUid());
        talker.setAvatar(contactEntity.getAvatar());
        talker.setNickName(contactEntity.getName());
        talker.setFriendPublicKey(contactEntity.getPublicKey());
        ChatActivity.startActivity(mActivity, talker);
    }

    private PermissionUtil.ResultCallBack permissionCallBack = new PermissionUtil.ResultCallBack() {
        @Override
        public void granted(String[] permissions) {
            if (permissions != null && permissions.length > 0) {
                if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contactEntity.getMobile()));
                mActivity.startActivity(intent);
            }
        }

        @Override
        public void deny(String[] permissions) {
        }
    };

    private void searchUser(String uid) {
        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(uid)
                .setTyp(2)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_WORKMATE_SEARCH, searchUser, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.Workmates workmates = Connect.Workmates.parseFrom(structData.getPlainData());
                    Connect.Workmate workmate = workmates.getList(0);
                    if (!workmate.getAvatar().equals(contactEntity.getAvatar())) {
                        contactEntity.setAvatar(workmate.getAvatar());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
            }
        });
    }

    private void addFollow(final boolean isAdd) {
        Connect.UserFollow userFollow = Connect.UserFollow.newBuilder()
                .setFollow(isAdd)
                .setUid(contactEntity.getUid())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_USERS_FOLLOW, userFollow, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.WorkmateVersion workmateVersion = Connect.WorkmateVersion.parseFrom(structData.getPlainData());
                    SharedUtil.getInstance().putValue(SharedUtil.CONTACTS_VERSION, workmateVersion.getVersion());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (isAdd) {
                    contactEntity.setRegisted(true);
                    ContactHelper.getInstance().insertContact(contactEntity);
                    ToastEUtil.makeText(mActivity, R.string.Login_Save_successful).show();
                    ContactNotice.receiverContact();
                } else {
                    ContactHelper.getInstance().deleteEntity(contactEntity.getUid());
                    ToastEUtil.makeText(mActivity, R.string.Link_Delete_Successful).show();
                    ContactNotice.receiverContact();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
            }
        });
    }

}
