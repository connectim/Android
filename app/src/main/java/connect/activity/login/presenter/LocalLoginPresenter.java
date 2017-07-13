package connect.activity.login.presenter;

import android.app.Activity;
import android.os.AsyncTask;

import java.util.List;

import connect.database.MemoryDataManager;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.LocalLoginContract;
import connect.activity.base.BaseApplication;
import connect.utils.ProgressUtil;
import connect.utils.ToastUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.wallet.jni.AllNativeMethod;

/**
 * Created by Administrator on 2017/4/14 0014.
 */

public class LocalLoginPresenter implements LocalLoginContract.Presenter {

    private LocalLoginContract.View mView;

    public LocalLoginPresenter(LocalLoginContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

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
                if(s != null && SupportKeyUril.checkPrikey(s)){
                    userBean.setPriKey(s);
                    userBean.setPubKey(AllNativeMethod.cdGetPubKeyFromPrivKey(userBean.getPriKey()));
                    userBean.setAddress(AllNativeMethod.cdGetBTCAddrFromPubKey(userBean.getPubKey()));
                    saveUserBean(userBean);
                }else{
                    ToastUtil.getInstance().showToast(R.string.Login_Password_incorrect);
                }
            }
        }.execute();
    }

    private void saveUserBean(UserBean userBean){
        SharedPreferenceUtil.getInstance().putUser(userBean);
        List<Activity> list = BaseApplication.getInstance().getActivityList();
        for (Activity activity1 : list) {
            if (!activity1.getClass().getName().equals(mView.getActivity().getClass().getName())){
                activity1.finish();
            }
        }
        MemoryDataManager.getInstance().putPriKey(userBean.getPriKey());
        mView.complete(userBean.isBack());
    }

}
