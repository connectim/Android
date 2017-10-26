package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wallet.bean.CurrencyEnum;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseApplication;
import connect.activity.chat.adapter.CrowdingDetailAdapter;
import connect.activity.chat.exts.contract.CrowdingDetailContract;
import connect.activity.chat.exts.presenter.CrowdingDetailPresenter;
import connect.activity.wallet.BlockchainActivity;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Created by gtq on 2016/12/21.
 */
public class CrowdingDetailActivity extends BaseActivity implements CrowdingDetailContract.BView {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg)
    ImageView roundimg;
    @Bind(R.id.txt1)
    TextView senderNameTxt;
    @Bind(R.id.txt2)
    TextView txt2;
    @Bind(R.id.txt3)
    TextView txt3;
    @Bind(R.id.txt4)
    TextView txt4;
    @Bind(R.id.txt5)
    TextView txt5;
    @Bind(R.id.btn)
    Button btn;
    @Bind(R.id.layout_first)
    LinearLayout layoutFirst;
    @Bind(R.id.txt6)
    TextView txt6;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private CrowdingDetailActivity activity;
    private static String GATHER_HASHID = "GATHER_HASHID";
    private static String GATHER_MSGID = "GATHER_MSGID";

    private String msgId;
    private String hashid;
    private CrowdingDetailAdapter adapter;
    private CrowdingDetailContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather_groupdetail);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String hashid) {
        startActivity(activity, hashid, null);
    }

    public static void startActivity(Activity activity, String hashid, String msgid) {
        Bundle bundle = new Bundle();
        bundle.putString(GATHER_HASHID, hashid);
        if (!TextUtils.isEmpty(msgid)) {
            bundle.putString(GATHER_MSGID, msgid);
        }
        ActivityUtil.next(activity, CrowdingDetailActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Chat_Crowdfunding));
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        hashid = getIntent().getStringExtra(GATHER_HASHID);
        msgId = getIntent().getStringExtra(GATHER_MSGID);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        adapter = new CrowdingDetailAdapter();
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(adapter);
        adapter.setItemClickListener(new CrowdingDetailAdapter.OnItemClickListener() {
            @Override
            public void itemClick(Connect.CrowdfundingRecord record) {
                if (record != null) {
                    BlockchainActivity.startActivity(activity, CurrencyEnum.BTC, record.getTxid());
                }
            }
        });
        new CrowdingDetailPresenter(this).start();
        presenter.requestCrowdingDetail(hashid);
    }

    @OnClick({R.id.btn})
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.btn:
                presenter.requestCrowdingPay(hashid);
                break;
        }
    }

    @Override
    public void setPresenter(CrowdingDetailContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void senderInfo(String avatar, String name) {
        GlideUtil.loadAvatarRound(roundimg, avatar);
        senderNameTxt.setText(getString(R.string.Chat_Crowd_funding_by_who, name));
    }

    @Override
    public void showBalance(long balance) {
        txt6.setText(BaseApplication.getInstance().getString(R.string.Wallet_Balance,
                RateFormatUtil.longToDoubleBtc(balance)));
    }

    @Override
    public void showTips(String tips) {
        if (TextUtils.isEmpty(tips)) {
            txt2.setVisibility(View.GONE);
        } else {
            txt2.setText(tips);
        }
    }

    @Override
    public void showCrowdingInfo(long total, long each, int state, String uid) {
        txt3.setText(String.format(getString(R.string.Wallet_Goal), RateFormatUtil.longToDoubleBtc(total)));
        txt4.setText(String.format(getString(R.string.Wallet_BTC_Each), RateFormatUtil.longToDoubleBtc(each)));

        if (state == 0) {
            layoutFirst.setVisibility(View.GONE);
            txt6.setVisibility(View.VISIBLE);
            if (SharedPreferenceUtil.getInstance().getUser().getPubKey().equals(uid)) {//You initiate the raise
                btn.setVisibility(View.GONE);
            } else {
                btn.setVisibility(View.VISIBLE);
            }
        } else {
            layoutFirst.setVisibility(View.VISIBLE);
            txt6.setVisibility(View.GONE);
            btn.setVisibility(View.GONE);
        }
    }

    @Override
    public void showPaidInfo(String info) {
        txt5.setText(info);
    }

    @Override
    public String getMessageId() {
        return msgId;
    }

    @Override
    public void showCrowdingRecords(List<Connect.CrowdfundingRecord> records, boolean state) {
        if (state) {
            txt6.setVisibility(View.GONE);
            btn.setVisibility(View.GONE);
        }
        adapter.setDatas(records);
    }
}
