package connect.view.payment;

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
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;

import connect.db.MemoryDataManager;
import connect.db.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.ui.activity.set.PaymentActivity;
import connect.ui.activity.set.bean.PaySetBean;
import connect.ui.activity.common.adapter.ViewPagerAdapter;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.StringUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.utils.system.SystemDataUtil;
import connect.view.ControlScrollViewPager;
import connect.view.MdStyleProgress;
import protos.Connect;

/**
 * Pay components
 * Created by Administrator on 2016/12/23.
 */
public class PaymentPwd implements View.OnClickListener{

    private ControlScrollViewPager viewPager;

    private Dialog dialog;

    private MdStyleProgress progressBar;
    private OnTrueListener onTrueListener;
    private PaySetBean paySetBean;
    private VirtualKeyboardView keyboardView;
    private Activity activity;
    private ImageView closeImg;
    /** Text state */
    private TextView statusTv;
    /** The current number of ViewPage pages */
    private int currentIndex;
    /**
     * Pay the password status
     * 0: default 1: no password set 2: set password again input 3: payment success 4: payment failed 5: payment
     */
    private int statusPay = 0;
    private String passFrist = "";
    private PayEditView payEdit;
    private TextView titleTv;
    private Handler handler;

    /**
     * show pay the password box
     */
    public Dialog showPaymentPwd(Activity activity, final OnTrueListener onTrueListener) {
        this.activity = activity;
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

        titleTv = (TextView)view.findViewById(R.id.title_tv);

        if(paySetBean != null && paySetBean.getNoSecretPay()){
            statusChange(5);
            onTrueListener.onTrue();
        }

        if (paySetBean == null || TextUtils.isEmpty(paySetBean.getPayPin())) {
            titleTv.setText(R.string.Set_Set_Payment_Password);
            statusChange(1);
        }

        return dialog;
    }

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

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float offset, int positionOffsetPixels) {

        }
        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    paySetBean = ParamManager.getInstance().getPaySet();
                    if(TextUtils.isEmpty(paySetBean.getPayPin())){
                        statusChange(1);
                    }else{
                        statusChange(0);
                    }
                    break;
                default:
                    break;
            }
            currentIndex = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * Password input box to listen
     */
    PayEditView.InputCompleteListener completeListener = new PayEditView.InputCompleteListener(){
        @Override
        public void inputComplete(String pass) {
            if(statusPay == 1 || statusPay == 2){
                setPayPass(pass);
                return;
            }
            // Verify the payment password
            statusChange(5);
            if(TextUtils.isEmpty(paySetBean.getVersionPay()) || paySetBean.getVersionPay().equals("0")){
                viewPager.setCurrentItem(1);
            }else{
                getPayVersion(pass);
            }
        }
    };

    /**
     * set password
     * @param pass
     */
    private void setPayPass(String pass){
        // Set the password flow (record the first password)
        if(statusPay == 1){
            passFrist = pass;
            statusChange(2);
            return;
        }
        // Re-enter the password
        if(statusPay == 2){
            if(passFrist.equals(pass)){
                keyboardView.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                payEdit.setVisibility(View.GONE);
                statusTv.setText(R.string.Wallet_Verifying);
                requestSetPay(pass);
            }else{
                titleTv.setText(R.string.Wallet_PINs_do_not_match);
                viewPager.setCurrentItem(2);
            }
            return;
        }
    }

    /**
     * Verify that the payment password is correct
     * @param pass
     */
    private void decodePass(String pass){
        if(!TextUtils.isEmpty(paySetBean.getPayPin())){
            try {
                byte[] ecdh  = SupportKeyUril.rawECDHkey(MemoryDataManager.getInstance().getPriKey(),
                        MemoryDataManager.getInstance().getPubKey());
                byte[] valueByte = StringUtil.hexStringToBytes(paySetBean.getPayPin());
                byte[] passByte = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.NONE,ecdh,Connect.GcmData.parseFrom(valueByte));
                String payPass = new String(passByte,"UTF-8");
                if (pass.equals(payPass)) {// Enter the payment password correctly
                    onTrueListener.onTrue();
                } else {// Incorrect password
                    viewPager.setCurrentItem(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Modify the View display according to the status
     * @param status
     */
    private void statusChange(int status) {
        statusPay = status;
        switch (status){
            case 0:
                keyboardView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                payEdit.setVisibility(View.VISIBLE);
                statusTv.setText("");
                break;
            case 1://There is no set the password
                statusTv.setText(R.string.Wallet_Enter_4_Digits);
                titleTv.setText(R.string.Set_Set_Payment_Password);
                break;
            case 2://Set the password Input again
                statusTv.setText(R.string.Wallet_Enter_again);
                titleTv.setText(R.string.Wallet_Confirm_Payment_password);
                TranslateAnimation animation = new TranslateAnimation(500, 0f, 0f, 0f);
                animation.setDuration(200);
                payEdit.startAnimation(animation);
                break;
            case 3://Pay for success
                statusTv.setText(R.string.Wallet_Payment_Successful);
                break;
            case 4://Pay for failure
                statusTv.setText(R.string.Wallet_Pay_Faied);
                break;
            case 5://In the payment
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
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        if(status == MdStyleProgress.Status.LoadSuccess){
                            statusChange(3);
                            progressBar.setStatus(status);
                            progressBar.startAnima();
                        }else if(status == MdStyleProgress.Status.LoadFail){
                            statusChange(4);
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

    /**
     * Get the payment password version
     * @param pass
     */
    private void getPayVersion(final String pass){
        Connect.PayPinVersion payPinVersion = Connect.PayPinVersion.newBuilder()
                .setVersion(paySetBean.getVersionPay())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_PAY_VERSION, payPinVersion, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.PayPinVersion payPinVersion = Connect.PayPinVersion.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(payPinVersion)){
                        if(payPinVersion.getVersion().equals(paySetBean.getVersionPay()) ){
                            decodePass(pass);
                        }else{
                            requestSetPayInfo(pass);
                        }
                        paySetBean.setVersionPay(payPinVersion.getVersion());
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    /**
     * Synchronize payment settings
     * @param pass
     */
    public void requestSetPayInfo(final String pass) {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_PAY_SUNC, ByteString.copyFrom(SupportKeyUril.createrBinaryRandom()),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            Connect.PaymentSetting paymentSetting = Connect.PaymentSetting.parseFrom(structData.getPlainData());
                            if(ProtoBufUtil.getInstance().checkProtoBuf(paymentSetting)){
                                paySetBean.setPayPin(paymentSetting.getPayPin());
                                ParamManager.getInstance().putPaySet(paySetBean);
                                decodePass(pass);
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {

                    }
                });
    }

    /**
     * Set up payment password
     * @param pass
     */
    private void requestSetPay(String pass){
        byte[] ecdh  = SupportKeyUril.rawECDHkey(MemoryDataManager.getInstance().getPriKey(),MemoryDataManager.getInstance().getPubKey());
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
                            onTrueListener.onTrue();
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

    public interface OnTrueListener {

        void onTrue();

    }

    public interface OnAnimationListener {
        void onComplete();
    }

}
