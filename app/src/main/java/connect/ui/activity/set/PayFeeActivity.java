package connect.ui.activity.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.ui.activity.set.bean.PaySetBean;
import connect.ui.activity.set.manager.EditInputFilterPrice;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import protos.Connect;

/**
 *
 * Created by Administrator on 2016/12/2.
 */
public class PayFeeActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.fee_et)
    EditText feeEt;
    @Bind(R.id.save_btn)
    TextView saveBtn;
    @Bind(R.id.auto_switch)
    View autoSwitch;
    @Bind(R.id.close_img)
    ImageButton closeImg;
    @Bind(R.id.textView)
    TextView textView;
    @Bind(R.id.title_set_fee)
    TextView titleSetFee;

    private PayFeeActivity mActivity;
    private PaySetBean paySetBean;

    public static void startActivity(Activity activity) {
        Bundle bundle = new Bundle();
        ActivityUtil.next(activity, PayFeeActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_set_minerfee);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Miner_fee);
        paySetBean = ParamManager.getInstance().getPaySet();
        autoSwitch.setSelected(paySetBean.isAutoFee());

        InputFilter[] inputFilters = {new EditInputFilterPrice(Double.valueOf(0.01), 8)};
        feeEt.setFilters(inputFilters);
        feeEt.addTextChangedListener(textWatcher);
        updataView();
    }

    private void updataView(){
        if(paySetBean.isAutoFee()){
            feeEt.setText(RateFormatUtil.longToDoubleBtc(paySetBean.getAutoMaxFee()));
            titleSetFee.setText(R.string.Wallet_Set_max_trasfer_fee);
        }else{
            feeEt.setText(RateFormatUtil.longToDoubleBtc(paySetBean.getFee()));
            titleSetFee.setText(R.string.Wallet_Set_transaction_fee_specified);
        }
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.save_btn)
    void savaFee(View view) {
        long fee = RateFormatUtil.doubleToLongBtc(Double.valueOf(feeEt.getText().toString()));
        if (fee >= 50) {
            if(paySetBean.isAutoFee()){
                paySetBean.setAutoMaxFee(fee);
            }else{
                paySetBean.setFee(fee);
            }
            requestSetpay();
        }
    }

    @OnClick(R.id.auto_switch)
    void autoFee(View view) {
        boolean isAuto = autoSwitch.isSelected();
        autoSwitch.setSelected(!isAuto);
        paySetBean.setAutoFee(!isAuto);
        ParamManager.getInstance().putPaySet(paySetBean);
        updataView();
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                long fee = RateFormatUtil.doubleToLongBtc(Double.valueOf(s.toString()));
                if (fee >= 50) {
                    saveBtn.setEnabled(true);
                } else {
                    saveBtn.setEnabled(false);
                }
            } catch (Exception e) {
                saveBtn.setEnabled(false);
            }

        }
    };

    private void requestSetpay() {
        Connect.PaymentSetting paymentSetting = Connect.PaymentSetting.newBuilder()
                .setFee(paySetBean.getFee())
                .setNoSecretPay(paySetBean.getNoSecretPay())
                .setPayPin(paySetBean.getPayPin())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_PAY_SETTING, paymentSetting, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                ToastEUtil.makeText(mActivity,R.string.Login_Save_successful).show();
                ParamManager.getInstance().putPaySet(paySetBean);
                ActivityUtil.goBack(mActivity);
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

}
