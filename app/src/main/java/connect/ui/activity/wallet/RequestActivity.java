package connect.ui.activity.wallet;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.set.manager.EditInputFilterPrice;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ConfigUtil;
import connect.utils.ToastEUtil;
import connect.view.TopToolBar;
import connect.view.zxing.utils.CreateScan;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * payment
 * Created by Administrator on 2016/12/10.
 */
public class RequestActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.address_tv)
    TextView addressTv;
    @Bind(R.id.address_lin)
    LinearLayout addressLin;
    @Bind(R.id.amount_et)
    EditText amountEt;
    @Bind(R.id.amount_lin)
    LinearLayout amountLin;
    @Bind(R.id.scan_img)
    ImageView scanImg;
    @Bind(R.id.setAmount_tv)
    TextView setAmountTv;

    private RequestActivity mActivity;
    public static String TRANSFER_SCAN_HEAD = "bitcoin:";
    public static String TRANSFER_AMOUNT_HEAD = "amount=";
    private EditInputFilterPrice bitEditFilter = new EditInputFilterPrice(Double.valueOf(999),8);
    //bitcoin:1CDheG1rvKoaPMnkswzcr3xphPVTyxxzYY?amount=1.0
    //https://transfer.connect.im/share/v1/pay?address=18gzAo5jxbsF1G2F741EHrwxdD2v11RvXf&amount=0.08
    public String scanHead;
    public String shareUrl = ConfigUtil.getInstance().sharePayAddress() + "?address=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_request);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.close_white3x);

        toolbarTop.setTitle(null, R.string.Wallet_Receipt);
        toolbarTop.setRightImg(R.mipmap.wallet_share_payment2x);

        scanHead = TRANSFER_SCAN_HEAD + MemoryDataManager.getInstance().getAddress() + "?" +TRANSFER_AMOUNT_HEAD;

        addressTv.setText(MemoryDataManager.getInstance().getAddress());
        InputFilter[] inputFiltersBtc = {bitEditFilter};
        amountEt.setFilters(inputFiltersBtc);
        amountEt.addTextChangedListener(textWatcher);
        CreateScan createScan = new CreateScan();
        Bitmap bitmap = createScan.generateQRCode(TRANSFER_SCAN_HEAD + MemoryDataManager.getInstance().getAddress());
        scanImg.setImageBitmap(bitmap);
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.address_lin)
    void goCopy(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(MemoryDataManager.getInstance().getAddress());
        ToastEUtil.makeText(mActivity,R.string.Set_Copied).show();
    }

    @OnClick(R.id.right_lin)
    void goshare(View view) {
        String url = shareUrl + MemoryDataManager.getInstance().getAddress();
        if(!TextUtils.isEmpty(amountEt.getText().toString())){
            url = url + "&amount=" + amountEt.getText().toString();
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, "share to"));
    }

    @OnClick(R.id.setAmount_tv)
    void setAmount(View view){
        if(addressLin.getVisibility() == View.VISIBLE){
            addressLin.setVisibility(View.GONE);
            amountLin.setVisibility(View.VISIBLE);
            setAmountTv.setText(R.string.Wallet_Clear);
        }else{
            addressLin.setVisibility(View.VISIBLE);
            amountLin.setVisibility(View.GONE);
            setAmountTv.setText(R.string.Wallet_Set_Amount);
            amountEt.setText("");
        }
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
            String scanUrl;
            if(TextUtils.isEmpty(s.toString())){
                scanUrl = TRANSFER_SCAN_HEAD + MemoryDataManager.getInstance().getAddress();
            }else{
                scanUrl = scanHead + s.toString();
            }
            CreateScan createScan = new CreateScan();
            Bitmap bitmap = createScan.generateQRCode(scanUrl);
            scanImg.setImageBitmap(bitmap);
        }
    };
}
