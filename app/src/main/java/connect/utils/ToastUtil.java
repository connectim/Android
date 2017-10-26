package connect.utils;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import connect.activity.base.BaseApplication;

/**
 * Toast tool
 */
public class ToastUtil {

    private static volatile ToastUtil sToastUtil = null;
    private Toast mToast = null;
    protected Handler handler = new Handler(Looper.getMainLooper());

    public static ToastUtil getInstance() {
        if (sToastUtil == null) {
            synchronized (ToastUtil.class) {
                if (sToastUtil == null) {
                    sToastUtil = new ToastUtil();
                }
            }
        }
        return sToastUtil;
    }

    public void showToast(final String tips){
        showToast(tips, Toast.LENGTH_SHORT);
    }

    public void showToast(final int tips){
        showToast(tips, Toast.LENGTH_SHORT);
    }

    public void showToast(final String tips, final int duration) {
        if (android.text.TextUtils.isEmpty(tips)) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(BaseApplication.getInstance(), tips, duration);
                    mToast.show();
                } else {
                    mToast.setText(tips);
                    mToast.setDuration(duration);
                    mToast.show();
                }
            }
        });
    }

    public void showToast(final int tips, final int duration) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(BaseApplication.getInstance(), tips, duration);
                    mToast.show();
                } else {
                    mToast.setText(tips);
                    mToast.setDuration(duration);
                    mToast.show();
                }
            }
        });
    }

}
