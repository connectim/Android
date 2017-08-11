package connect.activity.chat.exts;

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
import connect.activity.base.BaseActivity;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.ApplyGroupBean;
import connect.activity.chat.bean.CardExt1Bean;
import connect.activity.chat.bean.ContainerBean;
import connect.activity.chat.bean.GroupReviewBean;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.exts.contract.HandleGroupRequestContract;
import connect.activity.chat.exts.presenter.HandleGroupRequestPresenter;
import connect.activity.contact.FriendInfoActivity;
import connect.activity.contact.StrangerInfoActivity;
import connect.activity.contact.bean.SourceType;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;
import connect.widget.roundedimageview.RoundedImageView;

/**
 * group audit
 */
public class HandleGroupRequestActivity extends BaseActivity implements HandleGroupRequestContract.BView{

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

    private HandleGroupRequestActivity activity;

    private String messageId;
    private CardExt1Bean ext1Bean;
    private GroupReviewBean reviewBean;
    private HandleGroupRequestContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle_join_group);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String content,String ext1,String messageid) {
        Bundle bundle = new Bundle();
        bundle.putString("CONTENT", content);
        bundle.putString("EXT1", ext1);
        bundle.putString("MESSAGEID", messageid);
        ActivityUtil.next(activity, HandleGroupRequestActivity.class, bundle);
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

        messageId = getIntent().getStringExtra("MESSAGEID");
        String content = getIntent().getStringExtra("CONTENT");
        String extra = getIntent().getStringExtra("EXT1");
        ext1Bean = new Gson().fromJson(extra, CardExt1Bean.class);
        reviewBean = new Gson().fromJson(content, GroupReviewBean.class);


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

        new HandleGroupRequestPresenter(this).start();
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
                presenter.agreeRequest(ext1Bean.getPub_key(), reviewBean.getVerificationCode(), ext1Bean.getAddress());
                break;
            case R.id.btn2:
                presenter.rejectRequest(ext1Bean.getPub_key(), reviewBean.getVerificationCode(), ext1Bean.getAddress());
                break;
            case R.id.btn3:
                presenter.groupChat();
                break;
        }
    }

    @Override
    public String getPubKey() {
        return reviewBean.getGroupKey();
    }

    @Override
    public void setPresenter(HandleGroupRequestContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void showGroupInfo(String avatar, String name, String summary, int member) {
        GlideUtil.loadAvater(roundimg, avatar);
        txt1.setText(name);
        txt2.setText(getString(R.string.Chat_Member_Max, member, 200));

        String profile = TextUtils.isEmpty(summary) ? getString(R.string.Link_Group_brief) : summary;
        txt3.setText(profile);
    }

    @Override
    public void updateGroupRequest(int state) {
        String groupApplyKey = reviewBean.getGroupKey() + ext1Bean.getPub_key();
        ParamManager.getInstance().updateGroupApply(groupApplyKey, reviewBean.getTips(), reviewBean.getSource(), state, messageId);
        ContainerBean.sendRecExtMsg(ContainerBean.ContainerType.ROBOT_HANDLEAPPLY, messageId, state);
        ActivityUtil.goBack(activity);
    }
}
