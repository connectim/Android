package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.chat.exts.contract.TransferToContract;
import connect.activity.chat.exts.presenter.TransferToPresenter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;

/**
 * transaction
 * Created by gtq on 2016/12/23.
 */
public class TransferToActivity extends BaseActivity implements TransferToContract.BView{

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg)
    ImageView roundimg;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.layout_first)
    RelativeLayout layoutFirst;
    @Bind(R.id.layout_second)
    LinearLayout layoutSecond;
    @Bind(R.id.btn)
    Button btn;

    private TransferToActivity activity;
    private static String TAG = "_TransferToActivity";
    private static String TRANSFER_TYPE = "TRANSFER_TYPE";
    private static String TRANSFER_IDENTIFY = "TRANSFER_IDENTIFY";
    private static String TRANSFER_AMOUNT = "TRANSFER_AMOUNT";
    private String transAddress;

    private TransferToContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transferto);
        ButterKnife.bind(this);
        initView();
    }

    private TransferType transferType;

    public enum TransferType {
        CHAT,
        ADDRESS,
    }

    public static void startActivity(Activity activity, String pulicKey) {
        startActivity(activity, TransferType.CHAT, pulicKey, null);
    }

    public static void startActivity(Activity activity, String pulicKey, Double amount) {
        startActivity(activity, TransferType.ADDRESS, pulicKey, amount);
    }

    public static void startActivity(Activity activity, TransferType type, String address, Double amount) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(TRANSFER_TYPE, type);
        bundle.putString(TRANSFER_IDENTIFY, address);
        if (amount != null) {
            bundle.putDouble(TRANSFER_AMOUNT, amount);
        }
        ActivityUtil.next(activity, TransferToActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();

        toolbar.setTitle(getString(R.string.Wallet_Transfer));
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        transferType = (TransferType) getIntent().getSerializableExtra(TRANSFER_TYPE);
        transAddress = getIntent().getStringExtra(TRANSFER_IDENTIFY);

        layoutFirst.setVisibility(View.VISIBLE);
        layoutSecond.setVisibility(View.GONE);
        if(getIntent().getExtras().containsKey(TRANSFER_AMOUNT)){
            //transferEditView.setInputAmount(getIntent().getExtras().getDouble(TRANSFER_AMOUNT));
        }
        new TransferToPresenter(this).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @OnClick({R.id.btn})
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.btn:
                //Long currentlong = transferEditView.getCurrentBtcLong();
                //presenter.requestSingleTransfer(currentlong);
                break;
        }
    }

    @Override
    public String getPubkey() {
        return null;
    }

    @Override
    public void setPresenter(TransferToContract.Presenter presenter) {
        this.presenter=presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public String getTransferAddress() {
        return transAddress;
    }

    @Override
    public void showTransferInfo(String avatar, String tansferinfo) {
        GlideUtil.loadAvatarRound(roundimg, avatar);
        txt1.setText(tansferinfo);
    }

    @Override
    public long getCurrentAmount() {
        return 0;
        //return RateFormatUtil.doubleToLongBtc(Double.valueOf(transferEditView.getCurrentBtc()));
    }

    @Override
    public String getTransferNote() {
        return "";
        //return transferEditView.getNote();
    }

    @Override
    public TransferType getTransType() {
        return transferType;
    }
}
