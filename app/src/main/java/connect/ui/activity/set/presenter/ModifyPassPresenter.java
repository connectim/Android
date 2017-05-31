package connect.ui.activity.set.presenter;

import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.google.gson.Gson;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.set.contract.ModifyPassContract;
import connect.utils.ActivityUtil;
import connect.utils.ProgressUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/17 0017.
 */

public class ModifyPassPresenter implements ModifyPassContract.Presenter{

    private ModifyPassContract.View mView;
    private String talkKey;

    public ModifyPassPresenter(ModifyPassContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void start() {

    }

    @Override
    public TextWatcher getPassTextChange() {
        return passWatcherNew;
    }


    private TextWatcher passWatcherNew = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String pass = s.toString();
            if(!android.text.TextUtils.isEmpty(pass)){
                mView.setRightTextEnable(true);
            }else{
                mView.setRightTextEnable(false);
            }
        }
    };

    @Override
    public void requestPass(final String pass, final String hint) {
        new AsyncTask<Void, Void, Connect.ChangeLoginPassword>() {
            @Override
            protected Connect.ChangeLoginPassword doInBackground(Void... params) {
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
                        ActivityUtil.goBack(mView.getActivity());
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {

                    }
                });
            }
        }.execute();
    }
}
