package connect.utils.http;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import java.lang.ref.SoftReference;
import java.lang.reflect.ParameterizedType;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import connect.utils.ProgressUtil;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
/**
 * 网络访问完，结果处理
 * 1.是否显示Loading 访问前
 * 2.对于错误的code的处理，是否Retry
 * 3.回调防返回Activity正确结果内容
 */
public class ProgressObserver<T> implements Observer<T> {

    private SoftReference<HttpCallListener> listener;
    private SoftReference<Activity> activity;
    private boolean isShowProgress = true;

    public ProgressObserver(HttpCallListener listener) {
        this.listener = new SoftReference(listener);
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        if(isShowProgress){
            ProgressUtil.getInstance().showProgress(activity.get());
        }
    }

    @Override
    public void onNext(T t) {
        try {
            Class<?> entityClass = t.getClass();
            int code = (Integer) entityClass.getMethod("getCode").invoke(t);
            if(code == 2000){
                listener.get().onResponse(t);
            }else{
                listener.get().onError(t);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Throwable t) {
        ProgressUtil.getInstance().dismissProgress();
        Context context = activity.get();
        if (context == null) return;
        if (t instanceof SocketTimeoutException) {
            Toast.makeText(context, "网络中断，请检查您的网络状态", Toast.LENGTH_SHORT).show();
        } else if (t instanceof ConnectException) {
            Toast.makeText(context, "网络中断，请检查您的网络状态", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "错误" + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onComplete() {
        ProgressUtil.getInstance().dismissProgress();
    }

    /**
     * 是否显示加载Loading
     * @param showProgress
     */
    public void setShowProgress(boolean showProgress) {
        isShowProgress = showProgress;
    }

    /**
     * 传入avtivity的软引用，避免内存泄漏
     * @param activity
     */
    public void setActivity(Activity activity) {
        this.activity = new SoftReference(activity);
    }

    public SoftReference<HttpCallListener> getListener(){
        return listener;
    }

}
