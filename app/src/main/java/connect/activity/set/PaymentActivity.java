package connect.activity.set;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import java.security.SecureRandom;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.login.bean.UserBean;
import connect.activity.set.bean.PaySetBean;
import connect.activity.wallet.bean.WalletBean;
import connect.activity.wallet.manager.CurrencyManage;
import connect.activity.wallet.manager.CurrencyType;
import connect.activity.wallet.manager.PinManager;
import connect.activity.wallet.manager.WalletManager;
import connect.database.MemoryDataManager;
import connect.database.SharePreferenceUser;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.LoginPassCheckUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.cryption.EncoPinBean;
import connect.utils.data.RateFormatUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.data.RateFormatUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.jni.AllNativeMethod;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Payment Settings
 * Created by Administrator on 2016/12/5.
 */
public class PaymentActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.paypass_tv)
    TextView paypassTv;
    @Bind(R.id.pas_ll)
    LinearLayout pasLl;
    @Bind(R.id.fingerprint_tb)
    View fingerprintTb;
    @Bind(R.id.without_tb)
    View withoutTb;
    @Bind(R.id.miner_tv)
    TextView minerTv;
    @Bind(R.id.miner_ll)
    LinearLayout minerLl;

    private PaymentActivity mActivity;
    private UserBean userBean;
    private PaySetBean paySetBean;
    private String payPass = "";

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

        userBean = SharedPreferenceUtil.getInstance().getUser();
        paySetBean = ParamManager.getInstance().getPaySet();
        if (paySetBean != null) {
            updataView();
        }
    }

    private void updataView() {
        withoutTb.setSelected(paySetBean.getNoSecretPay());
        if (TextUtils.isEmpty(paySetBean.getPayPin())) {
            paypassTv.setText(R.string.Set_Setting);
        } else {
            paypassTv.setText(R.string.Wallet_Reset_password);
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

    @OnClick(R.id.pas_ll)
    void goSetPassword(View view) {
        // 判断是否创建了钱包，没有创建的话直接返回
        final WalletBean walletBean = SharePreferenceUser.getInstance().getWalletInfo();
        if(walletBean != null){
            String payload = "";
            if(!TextUtils.isEmpty(walletBean.getPayload())){
                payload = walletBean.getPayload();
            }else{
                CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(CurrencyType.BTC.getCode());
                payload = currencyEntity.getPayload();

            }
            new PinManager().showCheckPin(mActivity, payload, new PinManager.OnPinListener() {
                @Override
                public void success(String value) {
                    setPayPass(value);
                }
            });

        }else{
            ToastEUtil.makeText(mActivity,"你的钱包改没有创建不能修改密码").show();
        }
    }

    private void setPayPass(final String value){
        new PinManager().showSetNewPin(mActivity, new PinManager.OnPinListener() {
            @Override
            public void success(String pass) {
                WalletBean walletBean =  SharePreferenceUser.getInstance().getWalletInfo();
                EncoPinBean encoPinBean = SupportKeyUril.encoPinDefult(value,pass);
                if(!TextUtils.isEmpty(walletBean.getPayload())){
                    walletBean.setPayload(encoPinBean.getPayload());
                    walletBean.setN(encoPinBean.getN());
                    WalletManager walletManager = new WalletManager(mActivity);
                    walletManager.updateWalletInfo(walletBean, new WalletManager.OnWalletListener() {
                        @Override
                        public void complete() {
                            ToastEUtil.makeText(mActivity,R.string.Set_Set_success).show();
                        }

                        @Override
                        public void fail(String message) {
                            ToastEUtil.makeText(mActivity,R.string.Set_Setting_Faied).show();
                        }
                    });
                }else{
                    CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(CurrencyType.BTC.getCode());
                    currencyEntity.setPayload(encoPinBean.getPayload());
                    new CurrencyManage().setCurrencyInfo(currencyEntity,new CurrencyManage.OnCurrencyListener(){
                        @Override
                        public void success() {
                            ToastEUtil.makeText(mActivity,R.string.Set_Set_success).show();
                        }

                        @Override
                        public void fail(String message) {
                            ToastEUtil.makeText(mActivity,R.string.Set_Setting_Faied).show();
                        }
                    });
                }

            }
        });
    }

    @OnClick(R.id.without_tb)
    void switchWtthout(View view) {
        LoginPassCheckUtil.getInstance().checkLoginPass(mActivity, new LoginPassCheckUtil.OnResultListence() {
            @Override
            public void success(String priKey) {
                if (withoutTb.isSelected()) {
                    paySetBean.setNoSecretPay(false);
                } else {
                    paySetBean.setNoSecretPay(true);
                }
                requestSetpay();
            }

            @Override
            public void error() {

            }
        });
    }

    @OnClick(R.id.fingerprint_tb)
    void switchFingerprint(View view) {
        if (fingerprintTb.isSelected()) {
            fingerprintTb.setSelected(false);
        } else {
            fingerprintTb.setSelected(true);
        }
    }

    @OnClick(R.id.miner_ll)
    void goMinerFee(View view) {
        PayFeeActivity.startActivity(mActivity);
    }

    private void setPatpass() {
        Integer title;
        if(TextUtils.isEmpty(payPass)){
            title = R.string.Set_Payment_Password;
        }else {
            title = R.string.Wallet_Confirm_PIN;
        }
        DialogUtil.showPayEditView(mActivity, title, R.string.Wallet_Enter_4_Digits, new DialogUtil.OnItemClickListener() {
            @Override
            public void confirm(String value) {
                if(TextUtils.isEmpty(payPass)){
                    payPass = value;
                    setPatpass();
                }else if(payPass.equals(value)){
                    requestSetPay(value);
                }else{
                    ToastUtil.getInstance().showToast(R.string.Login_Password_incorrect);
                }
            }

            @Override
            public void cancel() {

            }
        });
    }

    private void requestSetpay() {
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
                updataView();
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    private void requestSetPay(String pass){
        // 获取数据库里币种对应的seed依次加密，并上传到服务器并更新到本地
        String encodeStr = SupportKeyUril.encodePri("aaaa","salt",pass);
        byte[] ecdh  = SupportKeyUril.rawECDHkey(MemoryDataManager.getInstance().getPriKey(),userBean.getPubKey());
        try {
            Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(SupportKeyUril.EcdhExts.NONE, ecdh, pass.getBytes("UTF-8"));
            byte[] gcmDataByte = gcmData.toByteArray();
            final String encryPass = StringUtil.bytesToHexString(gcmDataByte);
            Connect.PayPin payPin = Connect.PayPin.newBuilder()
                    .setPayPin(encryPass)
                    .build();
            OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_PAY_PIN_SETTING, payPin, new ResultCall<Connect.HttpResponse>() {
                @Override
                public void onResponse(Connect.HttpResponse response) {
                    try {
                        Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                        Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                        Connect.PayPinVersion payPinVersion = Connect.PayPinVersion.parseFrom(structData.getPlainData());
                        if(ProtoBufUtil.getInstance().checkProtoBuf(payPinVersion)){
                            paySetBean.setPayPin(encryPass);
                            paySetBean.setVersionPay(payPinVersion.getVersion());
                            ParamManager.getInstance().putPaySet(paySetBean);
                            ToastEUtil.makeText(mActivity, R.string.Wallet_Set_Payment_Password_Successful).show();
                            updataView();
                        }
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Connect.HttpResponse response) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
