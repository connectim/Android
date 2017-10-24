package connect.activity.login.presenter;

import android.os.AsyncTask;
import android.text.TextUtils;

import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.LoginUserContract;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ProgressUtil;
import connect.utils.ToastEUtil;
import instant.utils.cryption.SupportKeyUril;
import connect.wallet.jni.AllNativeMethod;

/**
 * User login Presenter.
 */
public class LoginUserPresenter implements LoginUserContract.Presenter {
    private LoginUserContract.View mView;

    public LoginUserPresenter(LoginUserContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {}

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
    public void checkPassWord(final String talkKey, final String passWord, final UserBean userBean) {
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
                    SharedPreferenceUtil.getInstance().putUser(userBean);
                    mView.launchHome();
                } else {
                    ToastEUtil.makeText(mView.getActivity(),R.string.Login_Password_incorrect,ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        }.execute();
    }
}
