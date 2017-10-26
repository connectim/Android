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

import com.wallet.bean.CurrencyEnum;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseApplication;
import connect.activity.set.SafetyPayFeeActivity;
import connect.activity.wallet.bean.TransferBean;
import connect.activity.wallet.manager.TransferManager;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.activity.wallet.view.TransferEditView;
import com.wallet.inter.WalletListener;

import connect.utils.cryption.SupportKeyUril;
import connect.widget.TopToolBar;

/**
 * Transfer to BTC address
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
    private TransferManager transferManager;

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
        transferManager = new TransferManager(mActivity, CurrencyEnum.BTC);
        if(bundle.containsKey("amount")){
            transferEditView.setInputAmount(bundle.getDouble("amount"));
        }
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
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
        transferManager.transferAddress(null, outMap, new WalletListener<String>() {
            @Override
            public void success(String value) {
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

    private TransferEditView.OnEditListener onEditListener = new TransferEditView.OnEditListener() {
        @Override
        public void onEdit(String value) {
            checkBtn();
        }

        @Override
        public void setFee() {
            SafetyPayFeeActivity.startActivity(mActivity);
        }
    };

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            checkBtn();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == BOOK_CODE) {
            addressTv.setText(data.getExtras().getString("address",""));
        }
    }

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
