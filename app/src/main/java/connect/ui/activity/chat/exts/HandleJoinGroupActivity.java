package connect.ui.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.ParamManager;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.ChatActivity;
import connect.ui.activity.chat.bean.ApplyGroupBean;
import connect.ui.activity.chat.bean.BaseEntity;
import connect.ui.activity.chat.bean.CardExt1Bean;
import connect.ui.activity.chat.bean.ContainerBean;
import connect.ui.activity.chat.bean.GroupReviewBean;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.Talker;
import connect.ui.activity.contact.FriendInfoActivity;
import connect.ui.activity.contact.StrangerInfoActivity;
import connect.ui.activity.contact.bean.SourceType;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import connect.view.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 * group audit
 */
public class HandleJoinGroupActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg1)
    RoundedImageView roundimg1;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.txt2)
    TextView txt2;
    @Bind(R.id.txt3)
    TextView txt3;
    @Bind(R.id.btn1)
    Button btn1;
    @Bind(R.id.btn2)
    Button btn2;
    @Bind(R.id.roundimg)
    RoundedImageView roundimg;
    @Bind(R.id.linearlayout)
    LinearLayout linearlayout;
    @Bind(R.id.txt4)
    TextView txt4;
    @Bind(R.id.txt5)
    TextView txt5;
    @Bind(R.id.txt6)
    TextView txt6;
    @Bind(R.id.btn3)
    Button btn3;

    private HandleJoinGroupActivity activity;
    private BaseEntity baseEntity;

    private Connect.GroupInfoBase infoBase = null;

    private CardExt1Bean ext1Bean;
    private GroupReviewBean reviewBean;
    private GroupEntity groupEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle_join_group);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, BaseEntity entity) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("entity", entity);
        ActivityUtil.next(activity, HandleJoinGroupActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getString(R.string.Wallet_Detail));
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        baseEntity = (BaseEntity) getIntent().getSerializableExtra("entity");
        MsgDefinBean definBean = baseEntity.getMsgDefinBean();

        ext1Bean = new Gson().fromJson(definBean.getExt1(), CardExt1Bean.class);
        reviewBean = new Gson().fromJson(definBean.getContent(), GroupReviewBean.class);

        groupEntity = ContactHelper.getInstance().loadGroupEntity(reviewBean.getGroupKey());
        if (groupEntity == null) {
            ActivityUtil.goBack(activity);
            return;
        }

        requestInfoByKey(reviewBean.getGroupKey());

        GlideUtil.loadAvater(roundimg1, ext1Bean.getAvatar());
        txt4.setText(ext1Bean.getUsername());
        String tips = TextUtils.isEmpty(reviewBean.getTips()) ? getString(R.string.Link_apply_to_join_group) : reviewBean.getTips();
        txt5.setText(tips);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(getString(R.string.Link_From));
        SpannableString colorStr = new SpannableString(reviewBean.getInvitor().getUsername());
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.color_blue));
        colorStr.setSpan(colorSpan, 0, colorStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(colorStr);
        builder.append(formatSource(reviewBean.getSource()));
        txt6.setText(builder);

        String groupApplyKey = reviewBean.getGroupKey() + ext1Bean.getPub_key();
        ApplyGroupBean applyGroupBean = ParamManager.getInstance().loadGroupApply(groupApplyKey);
        int verifycode = applyGroupBean.getState();
        if (verifycode == 0 || verifycode == -1) {//Audit/new apply
            btn1.setVisibility(View.VISIBLE);
            btn2.setVisibility(View.VISIBLE);
        } else {
            btn3.setVisibility(View.VISIBLE);
        }
    }

    protected String formatSource(int state) {
        String string = "";
        switch (state) {
            case 0:
                string = getString(R.string.Chat_Source_QR);
                break;
            case 2:
                string = getString(R.string.Link_From_group_link);
                break;
            case 1:
                string = getString(R.string.Link_Invite);
                break;
        }
        return string;
    }

    @OnClick({R.id.txt6, R.id.btn1, R.id.btn2, R.id.btn3})
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.txt6:
                String invitor = reviewBean.getInvitor().getAddress();
                ContactEntity friend = ContactHelper.getInstance().loadFriendEntity(invitor);
                if (friend == null) {
                    StrangerInfoActivity.startActivity(activity, invitor, SourceType.GROUP);
                } else {
                    FriendInfoActivity.startActivity(activity, friend.getPub_key());
                }
                break;
            case R.id.btn1:
                handleAgressJoin();
                break;
            case R.id.btn2:
                handleRejectJoin();
                break;
            case R.id.btn3:
                if (groupEntity != null) {
                    ChatActivity.startActivity(activity, new Talker(groupEntity));
                }
                break;
        }
    }

    protected void showGroupInfo() {
        GlideUtil.loadAvater(roundimg, infoBase.getAvatar());
        txt1.setText(infoBase.getName());
        txt2.setText(getString(R.string.Chat_Member_Max, infoBase.getCount(), 200));

        String profile = TextUtils.isEmpty(infoBase.getSummary()) ? getString(R.string.Link_Group_brief) : infoBase.getSummary();
        txt3.setText(profile);
    }

    protected void requestInfoByKey(String groupkey) {
        Connect.GroupId groupId = Connect.GroupId.newBuilder()
                .setIdentifier(groupkey).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_PUBLIC_INFO, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                String prikey = SharedPreferenceUtil.getInstance().getPriKey();
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(prikey, imResponse.getCipherData());
                    infoBase = Connect.GroupInfoBase.parseFrom(structData.getPlainData());
                    showGroupInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(activity,R.string.Link_Group_invitation_is_invalid,ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }

    protected void handleAgressJoin() {
        Connect.CreateGroupMessage createGroupMessage = Connect.CreateGroupMessage.newBuilder()
                .setSecretKey(groupEntity.getEcdh_key()).build();
        byte[] memberecdhkey = SupportKeyUril.rawECDHkey(SharedPreferenceUtil.getInstance().getPriKey(), ext1Bean.getPub_key());
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY, memberecdhkey, createGroupMessage.toByteString());

        String pubkey = SharedPreferenceUtil.getInstance().getPubKey();
        String groupHex = StringUtil.bytesToHexString(gcmData.toByteArray());
        String backup = String.format("%1$s/%2$s", pubkey, groupHex);

        Connect.GroupReviewed reviewed = Connect.GroupReviewed.newBuilder()
                .setIdentifier(reviewBean.getGroupKey())
                .setVerificationCode(reviewBean.getVerificationCode())
                .setAddress(ext1Bean.getAddress())
                .setBackup(backup).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_REVIEWED, reviewed, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                updateGroupJoin(1);
                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(reviewBean.getGroupKey());
                if (groupEntity != null) {
                    //ImageLoaderUtil.removeImageCache(groupEntity.getAvatar());
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                switch (response.getCode()) {
                    case 2432://not group manager
                        ToastEUtil.makeText(activity, getString(R.string.Chat_Not_Group_Master), 2).show();
                        break;
                    case 2433://be in group
                        ToastEUtil.makeText(activity, getString(R.string.Chat_User_already_in_group), 2).show();
                        break;
                    case 3434://Verification code error
                        ToastEUtil.makeText(activity, getString(R.string.Chat_VerifyCode_has_expired), 2).show();
                        break;
                }
            }
        });
    }

    protected void handleRejectJoin() {
        Connect.GroupReviewed reviewed = Connect.GroupReviewed.newBuilder()
                .setIdentifier(reviewBean.getGroupKey())
                .setVerificationCode(reviewBean.getVerificationCode())
                .setAddress(ext1Bean.getAddress()).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_REJECT, reviewed, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                updateGroupJoin(2);
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    protected void updateGroupJoin(int state) {
        String groupApplyKey = reviewBean.getGroupKey() + ext1Bean.getPub_key();
        ParamManager.getInstance().updateGroupApply(groupApplyKey, reviewBean.getTips(), reviewBean.getSource(), state, baseEntity.getMsgDefinBean().getMessage_id());
        ContainerBean.sendRecExtMsg(ContainerBean.ContainerType.ROBOT_HANDLEAPPLY, baseEntity.getMsgDefinBean().getMessage_id(), state);
        ActivityUtil.goBack(activity);
    }
}
