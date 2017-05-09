package connect.ui.activity.set.manager;

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
public class PassManager {

    private Dialog dialogPass;
    private Context mContext;
    private OnResultListence onResultListence;

    public void checkLoginPass(Context mContext,String passHint,OnResultListence onResultListence){
        this.mContext = mContext;
        this.onResultListence = onResultListence;
        checkPass(passHint);
    }

    private void checkPass(String passHint){
        dialogPass = DialogUtil.showEditView(mContext, mContext.getResources().getString(R.string.Set_Enter_Login_Password),
                mContext.getResources().getString(R.string.Common_Cancel),
                mContext.getResources().getString(R.string.Common_OK),
                mContext.getString(R.string.Login_Password_Hint, passHint), "", "", true
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
        new AsyncTask<Void,Void,Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params) {
                String priKey = DecryptionUtil.decodeTalkKey(SharedPreferenceUtil.getInstance().getUser().getTalkKey(), pass);
                return priKey != null && SupportKeyUril.checkPrikey(priKey);
            }

            @Override
            protected void onPostExecute(Boolean b) {
                super.onPostExecute(b);
                ProgressUtil.getInstance().dismissProgress();
                if(b){
                    onResultListence.success();
                }else{
                    ToastEUtil.makeText(mContext,R.string.Login_Password_incorrect,ToastEUtil.TOAST_STATUS_FAILE).show();
                    dialogPass.show();
                }
            }
        }.execute();
    }

    public interface OnResultListence{
        void success();
        void error();
    }

}
