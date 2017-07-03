package connect.ui.activity.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.MemoryDataManager;
import connect.db.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.ui.activity.set.PayFeeActivity;
import connect.ui.activity.wallet.bean.TransferBean;
import connect.utils.ProtoBufUtil;
import connect.utils.transfer.TransferError;
import connect.utils.transfer.TransferUtil;
import connect.ui.base.BaseActivity;
import connect.ui.base.BaseApplication;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.MdStyleProgress;
import connect.view.TopToolBar;
import connect.view.payment.PaymentPwd;
import connect.utils.transfer.TransferEditView;
import protos.Connect;

/**
 * Transfer to BTC address
 * Created by Administrator on 2016/12/21.
 */
public class TransferAddressActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.transfer_edit_view)
    TransferEditView transferEditView;
    @Bind(R.id.ok_btn)
    Button okBtn;
    @Bind(R.id.address_tv)
    EditText addressTv;

    private TransferAddressActivity mActivity;
    private final int BOOK_CODE = 100;
    private TransferUtil transaUtil;
    private PaymentPwd paymentPwd;

    public static void startActivity(Activity activity, String address) {
        startActivity(activity,address,null);
    }

    public static void startActivity(Activity activity, String address,Double amount) {
        Bundle bundle = new Bundle();
        bundle.putString("address", address);
        if(amount != null)
            bundle.putDouble("amount", amount);
        ActivityUtil.next(activity, TransferAddressActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_transfer_address);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bundle bundle = getIntent().getExtras();
        if(bundle.containsKey("amount")){
            transferEditView.initView(bundle.getDouble("amount"));
        }else{
            transferEditView.initView();
        }
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Wallet_Transfer);
        toolbarTop.setRightImg(R.mipmap.address_book3x);

        Bundle bundle = getIntent().getExtras();
        addressTv.setText(bundle.getString("address",""));
        addressTv.addTextChangedListener(textWatcher);
        transferEditView.setEditListener(onEditListener);
        transaUtil = new TransferUtil();
        paymentPwd = new PaymentPwd();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void goBook(View view) {
        ActivityUtil.next(mActivity, TransferAddressBookActivity.class, BOOK_CODE);
    }

    @OnClick(R.id.ok_btn)
    void goTransferOut(View view) {
        String address = addressTv.getText().toString().trim();
        if(TextUtils.isEmpty(address) || !SupportKeyUril.checkAddress(address)){
            ToastEUtil.makeText(mActivity,R.string.Wallet_Result_is_not_a_bitcoin_address,ToastEUtil.TOAST_STATUS_FAILE).show();
            return;
        }

        final long amount = RateFormatUtil.stringToLongBtc(transferEditView.getCurrentBtc());
        transaUtil.getOutputTran(mActivity, MemoryDataManager.getInstance().getAddress(), false, address,
                transferEditView.getAvaAmount(),amount,new TransferUtil.OnResultCall(){
            @Override
            public void result(String inputString, String outputString) {
                checkPayPassword(amount, inputString, outputString);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == BOOK_CODE) {
            addressTv.setText(data.getExtras().getString("address",""));
        }
    }

    private void checkPayPassword(final long amount, final String inputString, final String outputString) {
        paymentPwd.showPaymentPwd(mActivity, new PaymentPwd.OnTrueListener() {
            @Override
            public void onTrue() {
                String samValue = transaUtil.getSignRawTrans(MemoryDataManager.getInstance().getPriKey(), inputString, outputString);
                requestBillingSend(amount, samValue);
            }
        });
    }

    private void requestBillingSend(final long amount, final String samValue) {
        Connect.SendBill sendBill = Connect.SendBill.newBuilder()
                .setAmount(amount)
                .setReceiver(addressTv.getText().toString().trim())
                .setTips(transferEditView.getNote())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_BILLING_SEND, sendBill, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.BillHashId billHashId = Connect.BillHashId.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(billHashId)){
                        ParamManager.getInstance().putLatelyTransfer(new TransferBean(3,"","",addressTv.getText().toString()));
                        requestPublicTx(billHashId.getHash(), samValue);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadFail);
                TransferError.getInstance().showError(response.getCode(),response.getMessage());
            }
        });
    }

    private void requestPublicTx(String hashId, String rawTx) {
        Connect.PublishTx publishTx = Connect.PublishTx.newBuilder()
                .setHash(hashId)
                .setRawTx(rawTx)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_BILLING_PUBLISH_TX, publishTx, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadSuccess, new PaymentPwd.OnAnimationListener() {
                    @Override
                    public void onComplete() {
                        List<Activity> list = BaseApplication.getInstance().getActivityList();
                        for (Activity activity : list) {
                            if (activity.getClass().getName().equals(TransferActivity.class.getName())) {
                                activity.finish();
                            }
                        }
                        finish();
                    }
                });
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadFail);
            }
        });
    }

    private TransferEditView.OnEditListener onEditListener = new TransferEditView.OnEditListener() {
        @Override
        public void onEdit(String value) {
            checkBtn();
        }

        @Override
        public void setFee() {
            PayFeeActivity.startActivity(mActivity);
        }
    };

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            checkBtn();
        }
    };

    private void checkBtn(){
        if (!TextUtils.isEmpty(transferEditView.getCurrentBtc())
                && Double.valueOf(transferEditView.getCurrentBtc()) >= 0.0001
                && !TextUtils.isEmpty(addressTv.getText().toString())) {
            okBtn.setEnabled(true);
        } else {
            okBtn.setEnabled(false);
        }
    }

}
