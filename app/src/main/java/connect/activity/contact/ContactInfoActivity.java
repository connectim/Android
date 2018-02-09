package connect.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.GroupMemberUtil;
import connect.activity.contact.bean.ContactNotice;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.utils.system.SystemUtil;
import connect.widget.DepartmentAvatar;
import connect.widget.TopToolBar;
import instant.utils.SharedUtil;
import protos.Connect;


public class ContactInfoActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.name_text)
    TextView nameText;
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
    @Bind(R.id.number_text)
    TextView numberText;

    private ContactInfoActivity mActivity;
    private ContactEntity contactEntity;
    private String uid;

    public static void lunchActivity(Activity activity, String uid) {
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        ActivityUtil.next(activity, ContactInfoActivity.class, bundle);
    }

    public static void lunchActivity(Activity activity, ContactEntity contactEntity, String uid) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", contactEntity);
        bundle.putString("uid", uid);
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

        uid = getIntent().getExtras().getString("uid");
        contactEntity = (ContactEntity) getIntent().getExtras().getSerializable("bean");
        if (contactEntity != null) {
            uid = contactEntity.getUid();
            showView();
        }
        numberText.setText(mActivity.getString(R.string.Link_Employee_number) + ":");
        searchUser(uid);
    }

    private void showView() {
        nameText.setText(contactEntity.getName());
        if (contactEntity.getGender() == 1) {
            genderImage.setImageResource(R.mipmap.man);
        } else {
            genderImage.setImageResource(R.mipmap.woman);
        }
        numberTv.setText(contactEntity.getEmpNo());
        departmentTv.setText(contactEntity.getOu());
        phoneTv.setText(contactEntity.getMobile());
        if (contactEntity.getRegisted()) {
            if (ContactHelper.getInstance().loadFriendByUid(contactEntity.getUid()) == null) {
                if (contactEntity.getGender() == 1) {
                    toolbar.setRightText(R.string.Work_Pay_attention_to_him);
                } else {
                    toolbar.setRightText(R.string.Work_Pay_attention_to_her);
                }
            } else {
                toolbar.setRightText(R.string.Work_Cancel_the_attention);
            }
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
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void attention(View view) {
        if (ContactHelper.getInstance().loadFriendByUid(contactEntity.getUid()) == null) {
            addFollow(true);
        } else {
            addFollow(false);
        }
    }

    @OnClick(R.id.cell_image)
    void call(View view) {
        SystemUtil.callPhone(mActivity, contactEntity.getMobile());
    }

    @OnClick(R.id.chat_btn)
    void chat(View view) {
        if (contactEntity != null && !TextUtils.isEmpty(contactEntity.getUid())) {
            ChatActivity.startActivity(mActivity, Connect.ChatType.PRIVATE, contactEntity.getUid());
        }
    }

    private void searchUser(final String uid) {
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
                    if (contactEntity == null) {
                        contactEntity = new ContactEntity();
                        contactEntity.setName(workmate.getName());
                        contactEntity.setAvatar(workmate.getAvatar());
                        contactEntity.setPublicKey(workmate.getPubKey());
                        contactEntity.setEmpNo(workmate.getEmpNo());
                        contactEntity.setMobile(workmate.getMobile());
                        contactEntity.setGender(workmate.getGender());
                        contactEntity.setTips(workmate.getTips());
                        contactEntity.setRegisted(workmate.getRegisted());
                        contactEntity.setUid(workmate.getUid());
                        contactEntity.setOu(workmate.getOU());
                        showView();
                    } else {
                        final ContactEntity contactEntityLocal = ContactHelper.getInstance().loadFriendByUid(contactEntity.getUid());
                        if (contactEntityLocal != null) {
                            if (!workmate.getAvatar().equals(contactEntityLocal.getAvatar())) {
                                contactEntity.setAvatar(workmate.getAvatar());
                                showView();
                                ContactHelper.getInstance().insertContact(contactEntity);
                                ContactNotice.receiverContact();
                            }
                        }
                    }

                    ConversionEntity conversionEntity = ConversionHelper.getInstance().loadRoomEnitity(uid);
                    if (conversionEntity != null) {
                        conversionEntity.setAvatar(workmate.getAvatar());
                        conversionEntity.setName(workmate.getName());
                        ConversionHelper.getInstance().insertRoomEntity(conversionEntity);
                    }

                    List<GroupMemberEntity> memberEntities = ContactHelper.getInstance().loadGroupMembersByUid(uid);
                    if (memberEntities != null && memberEntities.size() > 0) {
                        GroupMemberUtil.getIntance().clearMembersMap();
                        for (GroupMemberEntity entity : memberEntities) {
                            entity.setUsername(workmate.getName());
                            entity.setAvatar(workmate.getAvatar());
                        }
                        ContactHelper.getInstance().inserGroupMemEntity(memberEntities);
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
                    ToastEUtil.makeText(mActivity, R.string.Link_Focus_successful).show();
                    ContactNotice.receiverContact();
                    toolbar.setRightText(R.string.Work_Cancel_the_attention);
                } else {
                    ContactHelper.getInstance().deleteEntity(contactEntity.getUid());
                    ToastEUtil.makeText(mActivity, R.string.Link_Focus_cancle_successful).show();
                    ContactNotice.receiverContact();
                    if (contactEntity.getGender() == 1) {
                        toolbar.setRightText(R.string.Work_Pay_attention_to_him);
                    } else {
                        toolbar.setRightText(R.string.Work_Pay_attention_to_her);
                    }
                }

            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
            }
        });
    }

}
