package connect.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.DialogUtil;
import connect.utils.ProgressUtil;
import connect.utils.ToastEUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;

/**
 * Verify login password
 * Created by Administrator on 2016/12/6.
 */
public class LoginPassCheckUtil {

    private Context mContext;
    private OnResultListence onResultListence;

    private static LoginPassCheckUtil passCheckUtil = null;

    public static LoginPassCheckUtil getInstance() {
        if (null == passCheckUtil) {
            passCheckUtil = new LoginPassCheckUtil();
        }
        return passCheckUtil;
    }

    public void checkLoginPass(Context mContext,OnResultListence onResultListence){
        this.mContext = mContext;
        this.onResultListence = onResultListence;
        checkPass();
    }

    private void checkPass(){
        String passHint = SharedPreferenceUtil.getInstance().getUser().getPassHint();
        passHint = TextUtils.isEmpty(passHint) ? "" : mContext.getString(R.string.Login_Password_Hint,passHint);
        DialogUtil.showEditView(mContext, mContext.getResources().getString(R.string.Set_Enter_Login_Password),
                mContext.getResources().getString(R.string.Common_Cancel),
                mContext.getResources().getString(R.string.Common_OK),
                passHint, "", "", true
                , 32,new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        if(TextUtils.isEmpty(value)){

                        }else{
                            encryptionPass(value);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
    }

    private void encryptionPass(final String pass){
        ProgressUtil.getInstance().showProgress(mContext);
        new AsyncTask<Void,Void,String>(){
            @Override
            protected String doInBackground(Void... params) {
                String priKey = DecryptionUtil.decodeTalkKey(SharedPreferenceUtil.getInstance().getUser().getTalkKey(), pass);
                return priKey;
            }

            @Override
            protected void onPostExecute(String priKey) {
                super.onPostExecute(priKey);
                ProgressUtil.getInstance().dismissProgress();
                if(priKey != null && SupportKeyUril.checkPrikey(priKey)){
                    onResultListence.success(priKey);
                }else {
                    ToastEUtil.makeText(mContext,R.string.Login_Password_incorrect,ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        }.execute();
    }

    public interface OnResultListence{
        void success(String priKey);
        void error();
    }

}
