package connect.widget.payment;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import connect.activity.common.adapter.ViewPagerAdapter;
import connect.activity.set.PaymentActivity;
import connect.activity.set.bean.PaySetBean;
import connect.activity.wallet.bean.WalletBean;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.system.SystemDataUtil;
import connect.widget.ControlScrollViewPager;
import connect.widget.MdStyleProgress;

/**
 * Created by Administrator on 2017/7/12 0012.
 */

public class PinTransferDialog implements View.OnClickListener{

    private ControlScrollViewPager viewPager;

    private Dialog dialog;

    private MdStyleProgress progressBar;
    private PaymentPwd.OnTrueListener onTrueListener;
    private PaySetBean paySetBean;
    private VirtualKeyboardView keyboardView;
    private Activity activity;
    private ImageView closeImg;
    /** Text state */
    private TextView statusTv;
    /** The current number of ViewPage pages */
    private int currentIndex;
    private PayEditView payEdit;
    private Handler handler;
    private String payload;

    /**
     * show pay the password box
     */
    public Dialog showPaymentPwd(Activity activity, String payload, final PaymentPwd.OnTrueListener onTrueListener) {
        this.activity = activity;
        this.payload = payload;
        paySetBean = ParamManager.getInstance().getPaySet();

        this.onTrueListener = onTrueListener;
        dialog = new Dialog(activity, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.dialog_pay_password, null);
        dialog.setContentView(view);

        viewPager = (ControlScrollViewPager) view.findViewById(R.id.view_pager);
        closeImg = (ImageView) view.findViewById(R.id.close_img);
        closeImg.setOnClickListener(this);
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
        View editPass = LayoutInflater.from(activity).inflate(R.layout.dialog_pay_edit_pass, null);
        View passForget = LayoutInflater.from(activity).inflate(R.layout.dialog_pay_forget, null);
        View setPay = LayoutInflater.from(activity).inflate(R.layout.dialog_pay_setpay, null);
        arrayList.add(editPass);
        arrayList.add(passForget);
        arrayList.add(setPay);

        viewPager.setAdapter(new ViewPagerAdapter(arrayList));
        viewPager.addOnPageChangeListener(pageChangeListener);
        viewPager.setScrollble(false);

        initEditPay(editPass);
        initPassForget(passForget);
        initSetPay(setPay);
    }

    private void initSetPay(View view) {
        TextView retryTv = (TextView) view.findViewById(R.id.retry_tv);
        retryTv.setOnClickListener(this);
    }

    private void initEditPay(View view) {
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
    }

    private void initPassForget(View view) {
        TextView forgetTv = (TextView) view.findViewById(R.id.forget_tv);
        TextView retryTv = (TextView) view.findViewById(R.id.retry_tv);
        retryTv.setOnClickListener(this);
        forgetTv.setOnClickListener(this);
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float offset, int positionOffsetPixels) {}
        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
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
        public void inputComplete(String pass) {
            statusChange(3);
            String decodeStr = SupportKeyUril.decodePin(payload,pass);
            if(TextUtils.isEmpty(decodeStr)){
                viewPager.setCurrentItem(1);
            }else{
                onTrueListener.onTrue(decodeStr);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.close_img:
                dialog.cancel();
                break;
            case R.id.retry_tv:
                viewPager.setCurrentItem(0);
                break;
            case R.id.forget_tv:
                ActivityUtil.next(activity, PaymentActivity.class);
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

    public void closeStatusDialog(final MdStyleProgress.Status status, final PaymentPwd.OnAnimationListener onAnimationListener) {
        //Sleep 2s Loading
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        if(status == MdStyleProgress.Status.LoadSuccess){
                            statusChange(1);
                            progressBar.setStatus(status);
                            progressBar.startAnima();
                        }else if(status == MdStyleProgress.Status.LoadFail){
                            statusChange(2);
                            progressBar.setStatus(status);
                            progressBar.failAnima();
                        }
                        break;
                    case 2:
                        dialog.dismiss();
                        if(onAnimationListener != null){
                            onAnimationListener.onComplete();
                        }
                        break;
                    default:
                        break;
                }
                //Sleep 1.3s (Successful failure time)
                handler.sendEmptyMessageDelayed(2,1300);
            }
        };
        //Sleep 1s (Circle time)
        handler.sendEmptyMessageDelayed(1,1000);
    }

    public interface OnTrueListener {
        void onTrue(String decodeStr);
    }

}
