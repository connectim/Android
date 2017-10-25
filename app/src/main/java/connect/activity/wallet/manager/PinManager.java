package connect.activity.wallet.manager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.wallet.NativeWallet;
import com.wallet.currency.BaseCurrency;
import com.wallet.inter.WalletListener;

import connect.ui.activity.R;
import connect.utils.DialogUtil;
import connect.utils.ToastUtil;
import connect.utils.system.SystemUtil;
import connect.activity.wallet.view.PayEditView;

/**
 * Pay the password management class
 */

public class PinManager {

    private static PinManager pinManager;
    private String payPass;

    public static PinManager getInstance() {
        if (pinManager == null) {
            synchronized (PinManager.class) {
                if (pinManager == null) {
                    pinManager = new PinManager();
                }
            }
        }
        return pinManager;
    }

    /**
     * Set up to pay the password
     */
    public void showSetNewPin(Activity mActivity, WalletListener listener) {
        payPass = "";
        setPin(mActivity,listener);
    }

    private void setPin(final Activity mActivity, final WalletListener listener) {
        Integer title;
        if (TextUtils.isEmpty(payPass)) {
            title = R.string.Set_Set_Payment_Password;
        } else {
            title = R.string.Wallet_Confirm_PIN;
        }
        showPayEditView(mActivity, title, R.string.Wallet_Enter_4_Digits, new DialogUtil.OnItemClickListener() {
            @Override
            public void confirm(String value) {
                if (TextUtils.isEmpty(payPass)) {
                    payPass = value;
                    setPin(mActivity,listener);
                } else if (payPass.equals(value)) {
                    // Set password complete
                    listener.success(value);
                } else {
                    showSetNewPin(mActivity,listener);
                    ToastUtil.getInstance().showToast(R.string.Wallet_Payment_Password_do_not_match);
                }
            }

            @Override
            public void cancel() {}
        });
    }

    /**
     * Check payment password
     *
     * @param activity
     * @param payload
     * @param listener
     */
    public void checkPwd(Activity activity, final String payload, final WalletListener listener) {
        showPayEditView(activity, R.string.Wallet_Enter_your_PIN, R.string.Wallet_Enter_4_Digits, new DialogUtil.OnItemClickListener() {
            @Override
            public void confirm(final String value) {
                new AsyncTask<Void,Void,PinBean>() {
                    @Override
                    protected PinBean doInBackground(Void... params) {
                        String baseSeed = NativeWallet.getInstance().decryptionPin(BaseCurrency.CATEGORY_BASESEED,payload, value);
                        if (TextUtils.isEmpty(baseSeed)) {
                            return null;
                        } else {
                            PinBean pinBean = new PinBean();
                            pinBean.setPin(value);
                            pinBean.setBaseSeed(baseSeed);
                            return pinBean;
                        }
                    }

                    @Override
                    protected void onPostExecute(PinBean pinBean) {
                        super.onPostExecute(pinBean);
                        if (pinBean == null) {
                            ToastUtil.getInstance().showToast(R.string.Login_Password_incorrect);
                        } else {
                            listener.success(pinBean);
                        }
                    }
                }.execute();
            }

            @Override
            public void cancel() {}
        });
    }

    public Dialog showPayEditView(Context mContext, Integer title, Integer message, final DialogUtil.OnItemClickListener onItemClickListener) {
        final Dialog dialog = new Dialog(mContext, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_paypass, null);
        TextView titleTv = (TextView) view.findViewById(R.id.title_tv);
        TextView messageTv = (TextView) view.findViewById(R.id.message_tv);
        final PayEditView payEditView = (PayEditView) view.findViewById(R.id.payEditView);

        titleTv.setText(title);
        if (message != null) {
            messageTv.setText(message);
            messageTv.setVisibility(View.VISIBLE);
        }
        payEditView.editText.setFocusableInTouchMode(true);
        payEditView.editText.requestFocus();
        SystemUtil.showKeyBoard(mContext,payEditView.editText);
        payEditView.setInputCompleteListener(new PayEditView.InputCompleteListener() {
            @Override
            public void inputComplete(String pass) {
                onItemClickListener.confirm(pass);
                dialog.dismiss();
            }
        });

        view.setMinimumWidth(SystemUtil.dipToPx(250));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
        dialog.show();

        return dialog;
    }

}
