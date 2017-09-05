package connect.activity.login.presenter;

import android.os.AsyncTask;

import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.LocalLoginContract;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ProgressUtil;
import connect.utils.ToastUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.wallet.jni.AllNativeMethod;

public class LocalLoginPresenter implements LocalLoginContract.Presenter {

    private LocalLoginContract.View mView;

    public LocalLoginPresenter(LocalLoginContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {}

    @Override
    public void checkTalkKey(final String talkKey, final String passWord, final UserBean userBean) {
        new AsyncTask<Void,Void,String>(){

            @Override
            protected String doInBackground(Void... params) {
                String priKey = SupportKeyUril.decodeTalkKey(talkKey, passWord);
                return priKey;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                ProgressUtil.getInstance().dismissProgress();
                if(s != null && SupportKeyUril.checkPriKey(s)){
                    userBean.setPriKey(s);
                    userBean.setPubKey(AllNativeMethod.cdGetPubKeyFromPrivKey(userBean.getPriKey()));
                    userBean.setAddress(AllNativeMethod.cdGetBTCAddrFromPubKey(userBean.getPubKey()));
                    SharedPreferenceUtil.getInstance().loginSaveUserBean(userBean, mView.getActivity());
                    mView.complete(userBean.isBack());
                }else{
                    ToastUtil.getInstance().showToast(R.string.Login_Password_incorrect);
                }
            }
        }.execute();
    }

}
