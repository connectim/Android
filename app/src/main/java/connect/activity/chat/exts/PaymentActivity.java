package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import connect.activity.base.BaseActivity;
import connect.activity.chat.exts.contract.PaymentContract;
import connect.activity.chat.exts.presenter.PaymentPresenter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.RegularUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.transfer.TransferEditView;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.widget.TopToolBar;
import connect.widget.roundedimageview.RoundedImageView;

/**
 * gather
 * Created by gtq on 2016/12/21.
 */
public class PaymentActivity extends BaseActivity implements PaymentContract.BView{

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.roundimg)
    RoundedImageView roundimg;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.layout_first)
    RelativeLayout layoutFirst;
    @Bind(R.id.edit)
    EditText edit;
    @Bind(R.id.layout_second)
    RelativeLayout layoutSecond;
    @Bind(R.id.transfer_edit_view)
    TransferEditView transferEditView;
    @Bind(R.id.txt2)
    TextView txt2;
    @Bind(R.id.btn)
    Button btn;

    private PaymentActivity activity;
    /** tag */
    private String Tag = "PaymentActivity";
    private int gatherType;
    private String gatherKey;

    private PaymentContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, int type, String roomkey) {
        Bundle bundle = new Bundle();
        bundle.putInt("TYPE_GATHER", type);
        bundle.putString("KEY_GATHER", roomkey);
        ActivityUtil.next(activity, PaymentActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        gatherType = getIntent().getIntExtra("TYPE_GATHER", 0);
        gatherKey = getIntent().getStringExtra("KEY_GATHER");
    }

    @Override
    protected void onStart() {
        super.onStart();
        transferEditView.initView(activity);
        transferEditView.setVisibilityAmount(View.GONE);
        transferEditView.setFeeVisibility(View.GONE);
        transferEditView.setEditListener(new TransferEditView.OnEditListener() {
            @Override
            public void onEdit(String value) {
                if (TextUtils.isEmpty(value) || Double.valueOf(transferEditView.getCurrentBtc()) < 0.0001) {
                    btn.setEnabled(false);
                } else {
                    btn.setEnabled(true);
                }
            }

            @Override
            public void setFee() {

            }
        });

        new PaymentPresenter(this).start();

        if (gatherType == 0) {
            toolbar.setTitle(getResources().getString(R.string.Wallet_Receipt));
            layoutFirst.setVisibility(View.VISIBLE);
            layoutSecond.setVisibility(View.GONE);
            txt2.setVisibility(View.GONE);

            presenter.loadPayment(gatherKey);
        } else if (gatherType == 1) {
            toolbar.setTitle(getResources().getString(R.string.Chat_Crowd_funding));
            toolbar.setRightTextColor(R.color.color_green);
            toolbar.setRightText(getString(R.string.Chat_Crowdfoundind_History));
            toolbar.setRightListence(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GroupCrowdingRecordsActivity.startActivity(activity);
                }
            });
            layoutFirst.setVisibility(View.GONE);
            layoutSecond.setVisibility(View.VISIBLE);
            transferEditView.setAmountTvGone();

            presenter.loadCrowding(gatherKey);
        }
    }

    @OnClick({R.id.btn})
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.btn:
                CurrencyEnum currencyEnum = transferEditView.getCurrencyEnum();
                long amount = RateFormatUtil.doubleToLongBtc(Double.valueOf(transferEditView.getCurrentBtc()));
                String tips = transferEditView.getNote();

                if (gatherType == 0) {
                    presenter.requestPayment(currencyEnum, amount, tips);
                } else if (gatherType == 1 && !TextUtils.isEmpty(edit.getText())) {
                    String memberstring = edit.getText().toString();
                    if (RegularUtil.matches(memberstring, RegularUtil.ALL_NUMBER)) {
                        int members = Integer.parseInt(memberstring);
                        presenter.requestCrowding(currencyEnum, amount, members, tips);
                    }
                }
                break;
        }
    }

    @OnTextChanged({R.id.edit, R.id.amoutinput_et})
    public void OnEditTextChange() {
        String amoutTxt = transferEditView.getCurrentBtc();
        if (TextUtils.isEmpty(amoutTxt)) amoutTxt = String.valueOf(0D);
        if (!TextUtils.isEmpty(edit.getText())) {
            int memCount = Integer.parseInt(edit.getText().toString());
            long amout = RateFormatUtil.doubleToLongBtc(Double.valueOf(amoutTxt));
            txt2.setText(getString(R.string.Wallet_BTC_Total, RateFormatUtil.longToDoubleBtc(amout * memCount)));
        }
    }

    @Override
    public void setPresenter(PaymentContract.Presenter presenter) {
        this.presenter=presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public String getPubkey() {
        return gatherKey;
    }

    @Override
    public void showPayment(String avatar, String name) {
        GlideUtil.loadAvater(roundimg, avatar);
        txt1.setText(name);
        btn.setText(getString(R.string.Wallet_Receipt));
    }

    @Override
    public void showCrowding(int count) {
        edit.setText(String.valueOf(count - 1));

        txt2.setVisibility(View.VISIBLE);
        String amoutTxt = transferEditView.getCurrentBtc();
        if (TextUtils.isEmpty(amoutTxt)) amoutTxt = String.valueOf(0D);
        long amout = RateFormatUtil.doubleToLongBtc(Double.valueOf(amoutTxt));
        txt2.setText(getString(R.string.Wallet_BTC_Total, RateFormatUtil.longToDoubleBtc(amout * count)));
        btn.setText(getString(R.string.Chat_Crowfunding));
    }
}
