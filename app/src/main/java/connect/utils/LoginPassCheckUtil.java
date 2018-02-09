package connect.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import connect.ui.activity.R;
import connect.utils.dialog.DialogUtil;

/**
 * Verify login password
 */
public class LoginPassCheckUtil {

    private Context mContext;
    private OnResultListener onResultListener;

    private static LoginPassCheckUtil passCheckUtil = null;

    public static LoginPassCheckUtil getInstance() {
        if (null == passCheckUtil) {
            passCheckUtil = new LoginPassCheckUtil();
        }
        return passCheckUtil;
    }

    public void checkLoginPass(Context mContext,OnResultListener onResultListener){
        this.mContext = mContext;
        this.onResultListener = onResultListener;
        showPassDialog();
    }

    /**
     * Enter the password dialog
     */
    private void showPassDialog(){
        DialogUtil.showEditView(mContext, mContext.getResources().getString(R.string.Set_Enter_Login_Password),
                mContext.getResources().getString(R.string.Common_Cancel),
                mContext.getResources().getString(R.string.Common_OK),
                "", "", "", true, 32,new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        if(TextUtils.isEmpty(value)){

                        }else{
                            checkLoginPass(value);
                        }
                    }

                    @Override
                    public void cancel() {}
                });
    }

    /**
     * Verify password is correct
     *
     * @param pass login pass
     */
    private void checkLoginPass(final String pass){
        ProgressUtil.getInstance().showProgress(mContext);
        new AsyncTask<Void,Void,String>(){
            @Override
            protected String doInBackground(Void... params) {
                //String priKey = SupportKeyUril.decodeTalkKey(SharedPreferenceUtil.getInstance().getUser().getTalkKey(), pass);
                return "";
            }

            @Override
            protected void onPostExecute(String priKey) {
                super.onPostExecute(priKey);
                ProgressUtil.getInstance().dismissProgress();
//                if(priKey != null && SupportKeyUril.checkPriKey(priKey)){
//                    onResultListener.success(priKey);
//                } else {
//                    ToastEUtil.makeText(mContext,R.string.Login_Password_incorrect,ToastEUtil.TOAST_STATUS_FAILE).show();
//                }
            }
        }.execute();
    }

    public interface OnResultListener{
        void success(String priKey);
        void error();
    }

}
