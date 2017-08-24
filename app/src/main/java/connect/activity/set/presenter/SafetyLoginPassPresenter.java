package connect.activity.set.presenter;

import android.os.AsyncTask;

import connect.database.MemoryDataManager;
import connect.database.SharedPreferenceUtil;
import connect.activity.login.bean.UserBean;
import connect.activity.set.contract.SafetyLoginPassContract;
import connect.utils.ProgressUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

public class SafetyLoginPassPresenter implements SafetyLoginPassContract.Presenter {

    private SafetyLoginPassContract.View mView;
    private String talkKey;

    public SafetyLoginPassPresenter(SafetyLoginPassContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {}

    @Override
    public void requestPass(final String pass, final String hint) {
        new AsyncTask<Void, Void, Connect.ChangeLoginPassword>() {
            @Override
            protected Connect.ChangeLoginPassword doInBackground(Void... params) {
                // The new password encryption private key
                talkKey = SupportKeyUril.createTalkKey(MemoryDataManager.getInstance().getPriKey(),
                        MemoryDataManager.getInstance().getAddress(), pass);
                Connect.ChangeLoginPassword changeLoginPassword = Connect.ChangeLoginPassword.newBuilder()
                        .setPasswordHint(hint)
                        .setEncryptionPri(talkKey)
                        .build();
                return changeLoginPassword;
            }

            @Override
            protected void onPostExecute(Connect.ChangeLoginPassword changeLoginPassword) {
                super.onPostExecute(changeLoginPassword);
                ProgressUtil.getInstance().dismissProgress();
                OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_BACK_KEY, changeLoginPassword, new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                        userBean.setPassHint(hint);
                        userBean.setTalkKey(talkKey);
                        SharedPreferenceUtil.getInstance().putUser(userBean);
                        mView.modifySuccess();
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {}
                });
            }
        }.execute();
    }

}
