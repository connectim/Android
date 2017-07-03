package connect.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import connect.ui.activity.R;

/**
 * Loading ProgressBar
 * Created by john on 2016/11/25.
 */

public class ProgressUtil {

    private static volatile ProgressUtil sProgressUtil = null;
    private Dialog dialog;

    public static ProgressUtil getInstance() {
        if (sProgressUtil == null) {
            synchronized (ToastUtil.class) {
                if (sProgressUtil == null) {
                    sProgressUtil = new ProgressUtil();
                }
            }
        }
        return sProgressUtil;
    }

    protected Handler handler = new Handler(Looper.getMainLooper());

    public void showProgress(Context mContext){
        showProgress(mContext,R.string.Common_Loading);
    }

    public void showProgress(Context mContext,int idRes){
        if(dialog != null){
            try {
                dialog.dismiss();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        dialog = new Dialog(mContext, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_progress, null);
        TextView text = (TextView)view.findViewById(R.id.text);
        text.setText(idRes);

        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(view);
        dialog.show();
    }

    public void dismissProgress(){
        if(dialog != null){
            try {
                dialog.dismiss();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
