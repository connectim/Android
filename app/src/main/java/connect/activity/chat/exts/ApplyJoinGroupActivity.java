package connect.activity.chat.exts;

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
import connect.activity.base.BaseActivity;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.exts.contract.JoinGroupContract;
import connect.activity.chat.exts.presenter.JoinGroupPresenter;
import connect.activity.home.bean.HttpRecBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;
import connect.widget.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 * apply to join in group
 */
public class ApplyJoinGroupActivity extends BaseActivity implements JoinGroupContract.BView {

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

    private JoinGroupContract.Presenter presenter;

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

        new JoinGroupPresenter(this).start();
        switch (applyType) {
            case TOKEN:
                String token = groupKey[0];
                presenter.requestByToken(token);
                break;
            case GROUPKEY:
                String groupkey = groupKey[0];
                presenter.requestByGroupkey(groupkey);
                break;
            case QRSCAN:
                String identify = groupKey[0];
                String hash = groupKey[1].split("/")[1];
                presenter.requestByLink(identify, hash);
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
        final String sayHelloStr =getResources().getString(R.string.Link_Hello_I_am, SharedPreferenceUtil.getInstance().getUser().getName()+
                ","+getResources().getString(R.string.Link_apply_to_join_group));

        DialogUtil.showEditView(activity, getResources().getString(R.string.Link_apply_to_join_group), getString(R.string.Common_Cancel), getString(R.string.Chat_Complete),
                "", sayHelloStr, "", false, -1,new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        if (TextUtils.isEmpty(value)) {
                            value = sayHelloStr;
                        }

                        String groupkey = groupKey[0];
                        if (applyType == EApplyGroup.GROUPKEY) {
                            if (null == groupKey[0] || null == groupKey[1] || null == groupKey[2]) {
                                return;
                            }

                            String inviteby = groupKey[1];
                            String token = groupKey[2];
                            presenter.requestJoinByInvite(groupkey, inviteby, value, token);
                        } else {
                            String hash = applyType == EApplyGroup.QRSCAN ? groupKey[1] : infoBase.getHash();
                            int source = applyType.ordinal();
                            presenter.requestJoinByLink(groupkey, hash, value, source);
                        }
                    }

                    @Override
                    public void cancel() {

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

    @Override
    public void setPresenter(JoinGroupContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void showTokenInfo(String groupkey, Connect.GroupInfoBase infoBase) {
        groupKey[0] = groupkey;
        this.infoBase = infoBase;
        requestBaseInfoSucces();
    }

    @Override
    public void showGroupkeyInfo(Connect.GroupInfoBase infoBase) {
        this.infoBase=infoBase;
        requestBaseInfoSucces();
    }

    @Override
    public void showLinkInfo(Connect.GroupInfoBase infoBase) {
        this.infoBase=infoBase;
        requestBaseInfoSucces();
    }

    @Override
    public void showFailInfo() {
        btn.setVisibility(View.GONE);
        txt4.setVisibility(View.VISIBLE);
    }
}
