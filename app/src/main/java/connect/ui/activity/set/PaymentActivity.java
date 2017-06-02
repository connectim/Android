package connect.ui.activity.set;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.set.bean.PaySetBean;
import connect.ui.activity.set.manager.PassManager;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
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
        PassManager passManager = new PassManager();
        passManager.checkLoginPass(mActivity, userBean.getPassHint(), new PassManager.OnResultListence() {
            @Override
            public void success() {
                setPatpass();
            }

            @Override
            public void error() {

            }
        });
    }

    @OnClick(R.id.without_tb)
    void switchWtthout(View view) {
        PassManager passManager = new PassManager();
        passManager.checkLoginPass(mActivity, userBean.getPassHint(), new PassManager.OnResultListence() {
            @Override
            public void success() {
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
        DialogUtil.showPayEditView(mActivity, R.string.Set_Payment_Password, R.string.Wallet_Enter_4_Digits, new DialogUtil.OnItemClickListener() {
            @Override
            public void confirm(String value) {
                setPaypassAgain(value);
            }

            @Override
            public void cancel() {

            }
        });
    }

    private void setPaypassAgain(final String pass) {
        DialogUtil.showPayEditView(mActivity, R.string.Wallet_Confirm_PIN, R.string.Wallet_Enter_4_Digits, new DialogUtil.OnItemClickListener() {
            @Override
            public void confirm(String value) {
                if (pass.equals(value)) {
                    //String ecdh = SupportKeyUril.cdEncryPayPasswordKey(userBean.getPriKey());
                    requestSetPay(value);

                } else {
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
