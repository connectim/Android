package connect.ui.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.ChatActivity;
import connect.ui.activity.chat.bean.Talker;
import connect.ui.activity.home.bean.HttpRecBean;
import connect.ui.base.BaseActivity;
import connect.ui.service.HttpsService;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import connect.view.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 * apply to join in group
 */
public class ApplyJoinGroupActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg)
    RoundedImageView roundimg;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.txt2)
    TextView txt2;
    @Bind(R.id.txt3)
    TextView txt3;
    @Bind(R.id.linearlayout)
    LinearLayout linearlayout;
    @Bind(R.id.txt4)
    TextView txt4;
    @Bind(R.id.btn)
    Button btn;

    private String Tag = "ApplyJoinGroupActivity";
    private ApplyJoinGroupActivity activity;

    public enum EApplyGroup {
        QRSCAN,
        GROUPKEY,//group PubKey
        TOKEN
,    }

    private static String APPLYTYPE = "APPLYTYPE";
    private static String GROUPKEY = "GROUPKEY";

    private EApplyGroup applyType;
    private String[] groupKey;
    private Connect.GroupInfoBase infoBase = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_join_group);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity context, EApplyGroup apply, String... groupkey) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(APPLYTYPE, apply);
        bundle.putStringArray(GROUPKEY, groupkey);
        ActivityUtil.next(context, ApplyJoinGroupActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getString(R.string.Link_Group));
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        applyType = (EApplyGroup) getIntent().getSerializableExtra(APPLYTYPE);
        groupKey = getIntent().getStringArrayExtra(GROUPKEY);

        switch (applyType) {
            case TOKEN:
                requestInfoByToken();
                break;
            case GROUPKEY:
                requestInfoByKey();
                break;
            case QRSCAN:
                requestInfoByScan();
                break;
        }
    }

    @OnClick(R.id.btn)
    public void onClickListener(View view) {
        switch (view.getId()) {
            case R.id.btn:
                applyJoinDialog();
                break;
        }
    }

    /**
     * query group public information
     */
    protected void requestInfoByKey() {
        Connect.GroupId groupId = Connect.GroupId.newBuilder()
                .setIdentifier(groupKey[0]).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_PUBLIC_INFO, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupInfoBase groupInfoBase = Connect.GroupInfoBase.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(groupInfoBase)){
                        infoBase = groupInfoBase;
                        requestBaseInfoSucces();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                requestBaseInfoFail();
            }
        });
    }

    /**
     * query group public information
     */
    protected void requestInfoByToken() {
        Connect.GroupToken token = Connect.GroupToken.newBuilder()
                .setToken(groupKey[0]).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_GROUP_INFOTOKEN, token, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupInfoBaseShare baseShare = Connect.GroupInfoBaseShare.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(baseShare)){
                        groupKey[0] = baseShare.getIdentifier();
                        infoBase = Connect.GroupInfoBase.newBuilder()
                                .setAvatar(baseShare.getAvatar())
                                .setHash(baseShare.getHash())
                                .setCount(baseShare.getCount())
                                .setName(baseShare.getName())
                                .setPublic(baseShare.getPublic())
                                .setJoined(baseShare.getJoined())
                                .setSummary(baseShare.getSummary()).build();

                        requestBaseInfoSucces();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                requestBaseInfoFail();
            }
        });
    }

    /**
     * query group public information
     */
    protected void requestInfoByScan() {
        String hash = groupKey[1].split("/")[1];
        Connect.GroupScan groupId = Connect.GroupScan.newBuilder()
                .setIdentifier(groupKey[0])
                .setHash(hash).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_PUBLIC_INFO, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupInfoBase groupInfoBase = Connect.GroupInfoBase.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(groupInfoBase)){
                        infoBase = groupInfoBase;
                        requestBaseInfoSucces();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                requestBaseInfoFail();
            }
        });
    }

    protected void requestBaseInfoSucces() {
        if (infoBase.getPublic()) {
            btn.setVisibility(View.VISIBLE);
            txt4.setVisibility(View.GONE);
            GlideUtil.loadAvater(roundimg, infoBase.getAvatar());
            txt1.setText(infoBase.getName());
            txt2.setText(getString(R.string.Chat_Member_Max, infoBase.getCount(), 200));

            String profile = TextUtils.isEmpty(infoBase.getSummary()) ? getString(R.string.Link_Group_brief) : infoBase.getSummary();
            txt3.setText(profile);

            boolean isJoin = infoBase.getJoined();
            if (isJoin) {
                handler.sendEmptyMessageDelayed(120, 500);
            }
        } else {
            requestBaseInfoFail();
        }
    }

    protected void requestBaseInfoFail() {
        btn.setVisibility(View.GONE);
        txt4.setVisibility(View.VISIBLE);
    }

    protected void applyJoinDialog() {
        final String sayHelloStr =getResources().getString(R.string.Link_Hello_I_am,SharedPreferenceUtil.getInstance().getUser().getName()+
                ","+getResources().getString(R.string.Link_apply_to_join_group));

        DialogUtil.showEditView(activity, getResources().getString(R.string.Link_apply_to_join_group), getString(R.string.Common_Cancel), getString(R.string.Chat_Complete),
                "", sayHelloStr, "", false, -1,new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        if (TextUtils.isEmpty(value)) {
                            value = sayHelloStr;
                        }

                        if (applyType == EApplyGroup.GROUPKEY) {
                            cardJoinGroup(value);
                        } else {
                            applyJoinGroup(value);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
    }

    /**
     * group member inivite to join
     */
    protected void cardJoinGroup(String tips) {
        if (null == groupKey[0] || null == groupKey[1] || null == groupKey[2]) {
            return;
        }
        Connect.GroupInvite invite = Connect.GroupInvite.newBuilder()
                .setIdentifier(groupKey[0])
                .setInviteBy(groupKey[1])
                .setTips(tips)
                .setToken(groupKey[2]).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_INVITE, invite, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                ActivityUtil.goBack(activity);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if(response.getCode() == 2430){
                    ToastEUtil.makeText(activity,R.string.Link_Qr_code_is_invalid,ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }

    protected void applyJoinGroup(String tips) {
        String hash = "";
        if (applyType == EApplyGroup.QRSCAN) {
            hash = groupKey[1];
        } else {
            hash = infoBase.getHash();
        }

        Connect.GroupApply apply = Connect.GroupApply.newBuilder()
                .setIdentifier(groupKey[0])
                .setHash(hash)
                .setTips(tips)
                .setSource(applyType.ordinal()).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_APPLY, apply, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                ActivityUtil.goBack(activity);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if(response.getCode() == 2403){
                    ToastEUtil.makeText(activity,R.string.Link_have_joined_the_group,ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 120:
                    String gKey = groupKey[0];
                    GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(gKey);
                    if (groupEntity == null || TextUtils.isEmpty(groupEntity.getEcdh_key())) {
                        HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.GroupInfo, gKey);
                    } else {
                        Talker talker = new Talker(groupEntity);
                        ChatActivity.startActivity(activity, talker);
                    }
                    break;
            }
        }
    };
}
