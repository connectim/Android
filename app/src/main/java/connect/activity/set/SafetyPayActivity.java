package connect.activity.set;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.set.bean.PaySetBean;
import connect.activity.wallet.bean.WalletBean;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.LoginPassCheckUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.EncoPinBean;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.data.RateFormatUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.cwallet.NativeWallet;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.bean.PinBean;
import connect.wallet.cwallet.currency.BaseCurrency;
import connect.wallet.cwallet.inter.WalletListener;
import connect.widget.TopToolBar;
import protos.Connect;
import wallet_gateway.WalletOuterClass;

/**
 * Payment Settings.
 */
public class SafetyPayActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.pay_pass_tv)
    TextView payPassTv;
    @Bind(R.id.pas_ll)
    LinearLayout pasLl;
    @Bind(R.id.without_tb)
    View withoutTb;
    @Bind(R.id.miner_tv)
    TextView minerTv;
    @Bind(R.id.miner_ll)
    LinearLayout minerLl;

    private SafetyPayActivity mActivity;
    private PaySetBean paySetBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_payment);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Payment);

        paySetBean = ParamManager.getInstance().getPaySet();
        if (paySetBean != null) {
            updateView();
        }

        if (CurrencyHelper.getInstance().loadCurrencyList() == null) {
            pasLl.setVisibility(View.INVISIBLE);
        }
    }

    private void updateView() {
        withoutTb.setSelected(paySetBean.getNoSecretPay());
        if (TextUtils.isEmpty(paySetBean.getPayPin())) {
            payPassTv.setText(R.string.Set_Setting);
        } else {
            payPassTv.setText(R.string.Wallet_Reset_password);
        }

        if (paySetBean.isAutoFee()) {
            minerTv.setText(R.string.Set_Auto);
        } else {
            minerTv.setText(getResources().getString(R.string.Set_BTC_symbol) + " " + RateFormatUtil.longToDoubleBtc(paySetBean.getFee()));
        }
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.without_tb)
    void switchWithout(View view) {
        LoginPassCheckUtil.getInstance().checkLoginPass(mActivity, new LoginPassCheckUtil.OnResultListener() {
            @Override
            public void success(String priKey) {
                if (withoutTb.isSelected()) {
                    paySetBean.setNoSecretPay(false);
                } else {
                    paySetBean.setNoSecretPay(true);
                }
                requestSetPay();
            }

            @Override
            public void error() {

            }
        });
    }

    @OnClick(R.id.miner_ll)
    void goMinerFee(View view) {
        SafetyPayFeeActivity.startActivity(mActivity);
    }

    @OnClick(R.id.pas_ll)
    void goSetPassword(View view) {
        // Check payment password
        NativeWallet.getInstance().checkPin(mActivity,new WalletListener<PinBean>() {
            @Override
            public void success(final PinBean pinBean) {
                // Set up to pay the password
                NativeWallet.getInstance().showSetPin(mActivity, new WalletListener<String>() {
                    @Override
                    public void success(String pin) {
                        requestPayload(pinBean, pin);
                    }

                    @Override
                    public void fail(WalletError error) {}
                });
            }

            @Override
            public void fail(WalletError error) {}
        });
    }

    /**
     * Upload payment infrastructure
     */
    private void requestSetPay() {
        Connect.PaymentSetting paymentSetting = Connect.PaymentSetting.newBuilder()
                .setFee(paySetBean.getFee())
                .setNoSecretPay(paySetBean.getNoSecretPay())
                .setPayPin(paySetBean.getPayPin())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_PAY_SETTING, paymentSetting, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                ParamManager.getInstance().putPaySet(paySetBean);
                ToastEUtil.makeText(mActivity, R.string.Chat_Set_success).show();
                updateView();
            }

            @Override
            public void onError(Connect.HttpResponse response) {}
        });
    }

    /**
     * Set the new password needs to be updated wallet information and content
     *
     * @param pinBean old pass
     * @param pinNew new pass
     */
    private void requestPayload(final PinBean pinBean, final String pinNew) {
        final CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(CurrencyEnum.BTC.getCode());
        final EncoPinBean encoPinBean = SupportKeyUril.encoPinDefult(BaseCurrency.CATEGORY_BASESEED, pinBean.getBaseSeed(),pinNew);
        NativeWallet.getInstance().updateWallet(encoPinBean.getPayload(), encoPinBean.getVersion(), new WalletListener<WalletBean>() {
            @Override
            public void success(WalletBean bean) {
                if (currencyEntity.getCategory() == BaseCurrency.CATEGORY_PRIKEY) {
                    String priKey = SupportKeyUril.decodePinDefult(BaseCurrency.CATEGORY_PRIKEY, currencyEntity.getPayload(), pinBean.getPin());
                    EncoPinBean priEncoPinBean = SupportKeyUril.encoPinDefult(BaseCurrency.CATEGORY_PRIKEY, priKey, pinNew);

                    NativeWallet.getInstance().initCurrency(CurrencyEnum.BTC).setCurrencyInfo(priEncoPinBean.getPayload(), null,
                            new WalletListener<WalletOuterClass.Coin>() {
                        @Override
                        public void success(WalletOuterClass.Coin coin) {
                            ToastEUtil.makeText(mActivity,R.string.Set_Set_success).show();
                        }

                        @Override
                        public void fail(WalletError error) {
                            ToastEUtil.makeText(mActivity,R.string.Set_Setting_Faied).show();
                        }
                    });
                } else {
                    ToastEUtil.makeText(mActivity,R.string.Set_Set_success).show();
                }
            }

            @Override
            public void fail(WalletError error) {
                ToastEUtil.makeText(mActivity,R.string.Set_Setting_Faied).show();
            }
        });
    }

}
