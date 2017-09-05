package connect.activity.login.presenter;

import android.os.AsyncTask;
import android.text.TextUtils;

import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.CodeLoginContract;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ProgressUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;

/**
 * User login Presenter.
 */
public class CodeLoginPresenter implements CodeLoginContract.Presenter {
    private CodeLoginContract.View mView;
    private String passwordHint = "";
    private String talkKey;

    public CodeLoginPresenter(CodeLoginContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {}

    @Override
    public void setPasswordHintData(String passwordHint) {
        this.passwordHint = passwordHint;
    }

    @Override
    public void passEditChange(String pass, String nick) {
        if (!TextUtils.isEmpty(pass) && !TextUtils.isEmpty(nick)) {
            mView.setNextBtnEnable(true);
        } else {
            mView.setNextBtnEnable(false);
        }
    }

    /**
     * Verify password
     *
     * @param talkKey Encrypted priKey
     * @param passWord pass
     * @param userBean The user information
     */
    @Override
    public void checkTalkKey(final String talkKey, final String passWord, final UserBean userBean) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String priKey = SupportKeyUril.decodeTalkKey(talkKey, passWord);
                return priKey;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                ProgressUtil.getInstance().dismissProgress();
                if (s != null && SupportKeyUril.checkPriKey(s)) {
                    userBean.setPriKey(s);
                    userBean.setPubKey(AllNativeMethod.cdGetPubKeyFromPrivKey(userBean.getPriKey()));
                    userBean.setAddress(AllNativeMethod.cdGetBTCAddrFromPubKey(userBean.getPubKey()));
                    SharedPreferenceUtil.getInstance().loginSaveUserBean(userBean, mView.getActivity());
                    mView.goinHome(userBean.isBack());
                } else {
                    ToastEUtil.makeText(mView.getActivity(),R.string.Login_Password_incorrect,ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        }.execute();
    }

    /**
     * To reset the login password
     *
     * @param password pass
     * @param userBean The user information
     * @param token Change the password token
     */
    @Override
    public void requestSetPassword(final String password, final UserBean userBean, final String token) {
        new AsyncTask<Void,Void, Connect.UserPrivateSign>() {
            @Override
            protected Connect.UserPrivateSign doInBackground(Void... params) {
                talkKey = SupportKeyUril.createTalkKey(userBean.getPriKey(),userBean.getAddress(),password);
                Connect.UserPrivateSign.Builder builder = Connect.UserPrivateSign.newBuilder();
                builder.setToken(token);
                builder.setEncryptionPri(talkKey);
                if (!TextUtils.isEmpty(passwordHint)) {
                    builder.setPasswordHint(passwordHint);
                }
                return builder.build();
            }

            @Override
            protected void onPostExecute(Connect.UserPrivateSign userPrivateSign) {
                super.onPostExecute(userPrivateSign);
                ProgressUtil.getInstance().dismissProgress();
                OkHttpUtil.getInstance().postEncry(UriUtil.CONNECT_V1_PRIVATE_SIGN, userPrivateSign, EncryptionUtil.ExtendedECDH.EMPTY,
                        userBean.getPriKey(), userBean.getPubKey(), new ResultCall<Connect.HttpResponse>() {
                            @Override
                            public void onResponse(Connect.HttpResponse response) {
                                userBean.setTalkKey(talkKey);
                                userBean.setPassHint(passwordHint);
                                SharedPreferenceUtil.getInstance().loginSaveUserBean(userBean, mView.getActivity());
                                mView.goinHome(userBean.isBack());
                            }

                            @Override
                            public void onError(Connect.HttpResponse response) {}
                        });
            }
        }.execute();
    }
}
