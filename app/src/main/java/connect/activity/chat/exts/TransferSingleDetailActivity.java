package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.chat.bean.ContainerBean;
import connect.activity.chat.exts.contract.TransferSingleDetailContract;
import connect.activity.chat.exts.presenter.TransferSingleDetailPresenter;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.TimeUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;

/**
 * Transfer details
 */
public class TransferSingleDetailActivity extends BaseActivity implements TransferSingleDetailContract.BView{

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg1)
    ImageView roundimg1;
    @Bind(R.id.roundimg2)
    ImageView roundimg2;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.txt2)
    TextView txt2;
    @Bind(R.id.txt3)
    TextView txt3;
    @Bind(R.id.txt4)
    TextView txt4;
    @Bind(R.id.txt5)
    TextView txt5;
    @Bind(R.id.txt6)
    TextView txt6;
    @Bind(R.id.txt7)
    TextView txt7;
    @Bind(R.id.linearlayout)
    LinearLayout linearlayout;

    private TransferSingleDetailActivity activity;
    private static String TAG = "_TransferSingleDetailActivity";
    private static String TRANSFER_TYPE = "TRANSFER_TYPE";
    private static String HASH_ID = "HASH_ID";
    private static String MESSAGE_ID = "MESSAGE_ID";

    private int transferType;// 0:outer 1:inner 2:mul
    private String hashId;
    private String msgId;

    private TransferSingleDetailContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_detail);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, int transferType, String hashid, String msgid) {
        Bundle bundle = new Bundle();
        bundle.putInt(TRANSFER_TYPE, transferType);
        bundle.putString(HASH_ID, hashid);
        bundle.putString(MESSAGE_ID, msgid);
        ActivityUtil.next(activity, TransferSingleDetailActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getString(R.string.Chat_Transfer_Detail));
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        transferType = getIntent().getIntExtra(TRANSFER_TYPE, 0);
        hashId = getIntent().getStringExtra(HASH_ID);
        msgId = getIntent().getStringExtra(MESSAGE_ID);

        new TransferSingleDetailPresenter(this).start();
        if (transferType == 0 || transferType == 1) {
            presenter.requestTransferInnerDetail(hashId);
        } else if (transferType == 2) {
            presenter.requestTransferOuterDetail(hashId);
        }
    }

    @OnClick({R.id.linearlayout})
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.linearlayout:
                break;
        }
    }

    @Override
    public void setPresenter(TransferSingleDetailContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void showTips(String tips) {
        if (TextUtils.isEmpty(tips)) {
            txt7.setVisibility(View.GONE);
        } else {
            txt7.setText(tips);
        }
    }

    @Override
    public void showTransferAmount(long amount) {
        txt1.setText(getString(R.string.Set_BTC_symbol) + "" + RateFormatUtil.longToDoubleBtc(amount));
    }

    @Override
    public void showTransferTxtid(final String txtid) {
        txt2.setText(txtid);
        linearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void showCreateTime(long createtime) {
        txt3.setText(TimeUtil.getTime(createtime * 1000, TimeUtil.DEFAULT_DATE_FORMAT));
    }

    @Override
    public void showTransferState(int transferstate) {
        String state = "";
        switch (transferstate) {
            case 0://do not pay
                state = getString(R.string.Chat_Unpaid);
                txt4.setBackgroundResource(R.drawable.shape_radius8_37c65c);
                break;
            case 1://do not confirm
                state = getString(R.string.Wallet_Unconfirmed);
                txt4.setBackgroundResource(R.drawable.shape_radius8_ff6c5a);
                break;
            case 2://confirm
                state = getString(R.string.Wallet_Confirmed);
                txt4.setBackgroundResource(R.drawable.shape_radius8_37c65c);
                break;
        }
        txt4.setText(state);

        if (!TextUtils.isEmpty(msgId)) {
            TransactionHelper.getInstance().updateTransEntity(hashId, msgId, transferstate);
            ContainerBean.sendRecExtMsg(ContainerBean.ContainerType.TRANSFER_STATE, msgId, hashId);
        }
    }

    @Override
    public void showUserInfo(int direct, String avatar, String name) {
        if (direct == 0) {
            GlideUtil.loadAvatarRound(roundimg1, avatar);
            txt5.setText(name);
        } else if (direct == 1) {
            GlideUtil.loadAvatarRound(roundimg2, avatar);
            txt6.setText(name);
        }
    }
}
