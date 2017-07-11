package connect.activity.wallet.manager;

import android.app.Activity;
import android.text.TextUtils;

import connect.ui.activity.R;
import connect.utils.DialogUtil;
import connect.utils.ToastUtil;

/**
 * Created by Administrator on 2017/7/11 0011.
 */

public class PinManager {

    private String payPass;
    private Activity mActivity;

    public PinManager(Activity mActivity) {
        this.mActivity = mActivity;
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

                }else{
                    ToastUtil.getInstance().showToast(R.string.Login_Password_incorrect);
                }
            }

            @Override
            public void cancel() {

            }
        });
    }

    public interface OnCurrencyListener {
        void success();

        void fail(String message);
    }

}
