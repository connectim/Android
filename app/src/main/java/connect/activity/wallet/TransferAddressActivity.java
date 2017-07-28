package connect.activity.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseApplication;
import connect.activity.set.PayFeeActivity;
import connect.activity.wallet.bean.TransferBean;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.transfer.TransferEditView;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.business.BaseBusiness;
import connect.wallet.cwallet.inter.WalletListener;
import connect.widget.TopToolBar;
import connect.widget.random.RandomVoiceActivity;
import wallet_gateway.WalletOuterClass;

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
    private BaseBusiness baseBusiness;

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
            transferEditView.initView(bundle.getDouble("amount"), mActivity);
        }else{
            transferEditView.initView(mActivity);
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

        baseBusiness = new BaseBusiness(mActivity, CurrencyEnum.BTC);
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
        CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(CurrencyEnum.BTC.getCode());
        if(TextUtils.isEmpty(address) || !SupportKeyUril.checkAddress(address)){
            ToastEUtil.makeText(mActivity,R.string.Wallet_Result_is_not_a_bitcoin_address,ToastEUtil.TOAST_STATUS_FAILE).show();
            return;
        }else if(currencyEntity == null){
            ToastEUtil.makeText(mActivity,R.string.Wallet_synchronization_data_failed,ToastEUtil.TOAST_STATUS_FAILE).show();
            return;
        }

        HashMap<String,Long> outMap = new HashMap<>();
        outMap.put(address,transferEditView.getCurrentBtcLong());
        baseBusiness.transferAddress(null, outMap, new WalletListener<String>() {
            @Override
            public void success(String value) {
                // Store the last 10 transfer records
                ParamManager.getInstance().putLatelyTransfer(new TransferBean(3,"","",addressTv.getText().toString()));
                List<Activity> list = BaseApplication.getInstance().getActivityList();
                for (Activity activity : list) {
                    if (activity.getClass().getName().equals(TransferActivity.class.getName())) {
                        activity.finish();
                    }
                }
                finish();
            }

            @Override
            public void fail(WalletError error) {
                ToastEUtil.makeText(mActivity,R.string.Login_Send_failed).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == BOOK_CODE) {
            addressTv.setText(data.getExtras().getString("address",""));
        }else if(resultCode == RESULT_OK && requestCode == RandomVoiceActivity.REQUEST_CODE){
            transferEditView.createrWallet(data);
        }
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
