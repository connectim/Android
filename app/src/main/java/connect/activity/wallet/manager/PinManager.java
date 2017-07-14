package connect.activity.wallet.manager;

import android.app.Activity;
import android.text.TextUtils;

import connect.ui.activity.R;
import connect.utils.DialogUtil;
import connect.utils.ToastUtil;
import connect.utils.cryption.SupportKeyUril;

/**
 * Created by Administrator on 2017/7/11 0011.
 */

public class PinManager {

    private String payPass;
    private Activity mActivity;
    private OnPinListener onPinListener;

    /**
     * 检查支付密码
     * @param mActivity
     * @param payload
     * @param onPinListener
     */
    public void showCheckPin(final Activity mActivity, final String payload, final OnPinListener onPinListener){
        DialogUtil.showPayEditView(mActivity, R.string.Set_Enter_Login_Password, R.string.Wallet_Enter_4_Digits, new DialogUtil.OnItemClickListener(){
            @Override
            public void confirm(String value) {
                String baseSeed = SupportKeyUril.decodePin(payload,value);
                if(TextUtils.isEmpty(baseSeed)){
                    ToastUtil.getInstance().showToast(R.string.Login_Password_incorrect);
                }else{
                    onPinListener.success(baseSeed);
                }
            }
            @Override
            public void cancel() {

            }
        });
    }

    /**
     * 重新设置支付密码
     * @param mActivity
     * @param onPinListener
     */
    public void showSetNewPin(Activity mActivity, final OnPinListener onPinListener){
        this.mActivity = mActivity;
        this.onPinListener = onPinListener;
        payPass = "";
        setPin();
    }

    private void setPin() {
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
                    setPin();
                }else if(payPass.equals(value)){
                    //设置密码完成
                    onPinListener.success(value);
                }else{
                    ToastUtil.getInstance().showToast(R.string.Login_Password_incorrect);
                }
            }

            @Override
            public void cancel() {

            }
        });
    }

    public interface OnPinListener {
        void success(String value);
    }

}