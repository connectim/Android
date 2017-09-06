package connect.widget.payment;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.activity.common.adapter.ViewPagerAdapter;
import connect.activity.set.bean.PaySetBean;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.system.SystemDataUtil;
import connect.wallet.cwallet.NativeWallet;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.widget.ControlScrollViewPager;
import connect.widget.MdStyleProgress;
import wallet_gateway.WalletOuterClass;

public class PinTransferDialog implements View.OnClickListener{

    private ControlScrollViewPager viewPager;

    private Dialog dialog;

    private MdStyleProgress progressBar;
    private OnTrueListener onTrueListener;
    private PaySetBean paySetBean;
    private VirtualKeyboardView keyboardView;
    private Activity activity;
    /** Text state */
    private TextView statusTv;
    /** The current number of ViewPage pages */
    private int currentIndex;
    private PayEditView payEdit;
    private String payload;
    private List<WalletOuterClass.Txout> txouts;
    private long fee;
    private CurrencyEnum currencyEnum;
    private ArrayList<String> inputsList;
    private long fixedFee;

    /**
     * show pay the password box
     */
    public Dialog showPaymentPwd(Activity activity, ArrayList<String> inputsList, List<WalletOuterClass.Txout> txouts, long fee, long fixedFee,int currency, String payload, final OnTrueListener onTrueListener) {
        this.activity = activity;
        this.payload = payload;
        this.txouts = txouts;
        this.inputsList = inputsList;
        this.fee = fee;
        this.fixedFee = fixedFee;
        this.currencyEnum = CurrencyEnum.getCurrency(currency);
        paySetBean = ParamManager.getInstance().getPaySet();

        this.onTrueListener = onTrueListener;
        dialog = new Dialog(activity, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.dialog_pay_password, null);
        dialog.setContentView(view);

        viewPager = (ControlScrollViewPager) view.findViewById(R.id.view_pager);
        initViewPage();

        Window mWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = mWindow.getAttributes();
        lp.width = SystemDataUtil.getScreenWidth();
        mWindow.setGravity(Gravity.BOTTOM);
        mWindow.setWindowAnimations(R.style.DialogAnim);
        mWindow.setAttributes(lp);
        dialog.show();
        return dialog;
    }

    /**
     * init view
     */
    private void initViewPage() {
        ArrayList<View> arrayList = new ArrayList<>();
        View payDetail = LayoutInflater.from(activity).inflate(R.layout.dialog_pay_detail, null);
        View editPass = LayoutInflater.from(activity).inflate(R.layout.dialog_pay_edit_pass, null);
        View passForget = LayoutInflater.from(activity).inflate(R.layout.dialog_pay_forget, null);
        arrayList.add(payDetail);
        arrayList.add(editPass);
        arrayList.add(passForget);

        viewPager.setAdapter(new ViewPagerAdapter(arrayList));
        viewPager.addOnPageChangeListener(pageChangeListener);
        viewPager.setScroll(false);

        initEditPay(editPass);
        initPassForget(passForget);
        initPayDetail(payDetail);
    }

    private void initPayDetail(View view) {
        TextView feeTv = (TextView) view.findViewById(R.id.fee_tv);
        TextView transferTv = (TextView) view.findViewById(R.id.transfer_tv);
        ImageView closeImg = (ImageView) view.findViewById(R.id.close_img);
        LinearLayout detailLin = (LinearLayout)view.findViewById(R.id.detail_lin);

        transferTv.setText(activity.getString(R.string.Wallet_Transfer_To_User,":"));
        feeTv.setText(activity.getString(R.string.Wallet_Fee_BTC,
                Double.valueOf(NativeWallet.getInstance().initCurrency(currencyEnum).longToDoubleCurrency(fee + fixedFee))));

        for (WalletOuterClass.Txout txout : txouts) {
            if(inputsList.contains(txout.getAddress())){
                continue;
            }
            ContactEntity contactEntity = ContactHelper.getInstance().loadFriendEntity(txout.getAddress());
            View detailView = LayoutInflater.from(activity).inflate(R.layout.item_pay_address, null);
            TextView address = (TextView) detailView.findViewById(R.id.address_tv);
            TextView amount = (TextView) detailView.findViewById(R.id.amount_tv);
            if(fixedFee == 0){
                if(contactEntity != null){
                    address.setText(contactEntity.getUsername() + "( "+ activity.getString(R.string.Link_Friend) + ")");
                }else{
                    address.setText(txout.getAddress());
                }
                amount.setText(NativeWallet.getInstance().initCurrency(currencyEnum).longToDoubleCurrency(txout.getAmount()) + "BTC");
            }else{
                address.setText(activity.getString(R.string.Wallet_The_Connect_system_address));
                amount.setText(NativeWallet.getInstance().initCurrency(currencyEnum).longToDoubleCurrency(txout.getAmount() - fixedFee) + "BTC");
            }
            detailLin.addView(detailView);
        }

        Button okBtn = (Button) view.findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(this);
        closeImg.setOnClickListener(this);
    }

    private void initEditPay(View view) {
        ImageView backImg = (ImageView) view.findViewById(R.id.back_img);
        progressBar = (MdStyleProgress) view.findViewById(R.id.progress);
        payEdit = (PayEditView) view.findViewById(R.id.pay_edit);
        statusTv = (TextView)view.findViewById(R.id.status_tv);
        keyboardView = (VirtualKeyboardView) view.findViewById(R.id.virtualKeyboardView);
        keyboardView.setAddNumberListence(new VirtualKeyboardView.AddNumberListence() {
            @Override
            public void changeText(String value) {
                payEdit.setEditText(value);
            }
        });
        payEdit.setEditClosePan();
        payEdit.setInputCompleteListener(completeListener);
        backImg.setOnClickListener(this);
    }

    private void initPassForget(View view) {
        TextView retryTv = (TextView) view.findViewById(R.id.retry_tv);
        retryTv.setOnClickListener(this);
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float offset, int positionOffsetPixels) {}
        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 1:
                    statusChange(0);
                    break;
                default:
                    break;
            }
            currentIndex = position;
        }
        @Override
        public void onPageScrollStateChanged(int state) {}
    };

    /**
     * Password input box to listen
     */
    PayEditView.InputCompleteListener completeListener = new PayEditView.InputCompleteListener(){
        @Override
        public void inputComplete(final String pass) {
            statusChange(3);
            new AsyncTask<Void,Void,String>(){
                @Override
                protected String doInBackground(Void... params) {
                    CurrencyEntity currencyEntity = CurrencyHelper.getInstance().loadCurrency(currencyEnum.getCode());
                    String decodeStr = SupportKeyUril.decryptionPinDefault(currencyEntity.getCategory() ,payload ,pass);
                    return decodeStr;
                }

                @Override
                protected void onPostExecute(String decodeStr) {
                    super.onPostExecute(decodeStr);
                    if(TextUtils.isEmpty(decodeStr)){
                        viewPager.setCurrentItem(2);
                    }else{
                        onTrueListener.onTrue(decodeStr);
                    }
                }
            }.execute();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.close_img:
                dialog.cancel();
                break;
            case R.id.ok_btn:
                viewPager.setCurrentItem(1);
                break;
            case R.id.back_img:
                viewPager.setCurrentItem(0);
                break;
            case R.id.retry_tv:
                viewPager.setCurrentItem(1);
                break;
            default:
                break;
        }
    }

    /**
     * Modify the View display according to the status
     * @param status
     */
    private void statusChange(int status) {
        switch (status){
            case 0:
                keyboardView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                payEdit.setVisibility(View.VISIBLE);
                statusTv.setText("");
                break;
            case 1: // Pay for success
                statusTv.setText(R.string.Wallet_Payment_Successful);
                break;
            case 2: // Pay for failure
                statusTv.setText(R.string.Wallet_Pay_Faied);
                break;
            case 3: // In the payment
                keyboardView.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                payEdit.setVisibility(View.GONE);
                statusTv.setText(R.string.Wallet_Verifying);
                break;
        }
    }

    /**
     * The success of the payment failed
     * @param status
     */
    public void closeStatusDialog(MdStyleProgress.Status status) {
        closeStatusDialog(status, null);
    }

    public void closeStatusDialog(final MdStyleProgress.Status status, final OnAnimationListener onAnimationListener) {
        //Sleep 2s Loading
        Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                if(status == MdStyleProgress.Status.LoadSuccess){
                    statusChange(1);
                    progressBar.setStatus(status);
                    progressBar.startAnima();
                }else if(status == MdStyleProgress.Status.LoadFail){
                    statusChange(2);
                    progressBar.setStatus(status);
                    progressBar.failAnima();
                }
                sleepCloseDialog(onAnimationListener);
            }
        };
        //Sleep 1s (Circle time)
        handler.sendEmptyMessageDelayed(1,1000);
    }

    private void sleepCloseDialog(final OnAnimationListener onAnimationListener){
        Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                dialog.dismiss();
                if(onAnimationListener != null){
                    onAnimationListener.onComplete();
                }
            }
        };
        //Sleep 1.3s (Successful failure time)
        handler.sendEmptyMessageDelayed(2,1300);
    }

    public interface OnTrueListener {
        void onTrue(String decodeStr);
    }

    public interface OnAnimationListener {
        void onComplete();
    }

}
