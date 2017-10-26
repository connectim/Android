package connect.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import connect.ui.activity.R;

/**
 * Get the outermost content layout, add View custom Toast
 */
public class ToastEUtil {

    private String TOAST_TAG = "TOAST_TAG";
    private static Context mContext;
    private View view;
    private static TextView mTextView;
    private ViewGroup container;
    private static ImageView statusImg;
    private LinearLayout mContainer;
    private static ToastEUtil result;

    private AlphaAnimation mFadeOutAnimation;
    private AlphaAnimation mFadeInAnimation;
    private boolean isShow = false;
    private Handler mHandler = new Handler();
    private static OnToastListener onToastListener;

    public static final int TOAST_STATUS_SUCCESS = 1;
    public static final int TOAST_STATUS_FAILE = 2;
    private final int ANIMATION_DURATION = 600;
    private int HIDE_DELAY = 1500;

    private ToastEUtil(Context context) {
        mContext = context;
        container = (ViewGroup) ((Activity) context).findViewById(android.R.id.content);
        View viewWithTag = container.findViewWithTag(TOAST_TAG);
        if(viewWithTag == null){
            view = ((Activity) context).getLayoutInflater().inflate(R.layout.item_toast, container);
            view.setTag(TOAST_TAG);
        }else{
            view = viewWithTag;
        }
        mContainer = (LinearLayout) view.findViewById(R.id.mbContainer);
        mContainer.setVisibility(View.GONE);
        mTextView = (TextView) view.findViewById(R.id.tips_tv);
        statusImg = (ImageView) view.findViewById(R.id.status_img);
    }

    public static ToastEUtil makeText(Context context, String message) {
        return makeText(context,message,TOAST_STATUS_SUCCESS);
    }

    public static ToastEUtil makeText(Context context, int resId) {
        return makeText(context,resId,TOAST_STATUS_SUCCESS);
    }

    public static ToastEUtil makeText(Context context, int resId, int stutas) {
        String mes = "";
        try{
            mes = context.getResources().getString(resId);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return makeText(context,mes,stutas);
    }

    public static ToastEUtil makeText(Context context, String message, int stutas) {
        return makeText(context,message,stutas,null);
    }

    public static ToastEUtil makeText(Context context, String message, int stutas ,OnToastListener listener) {
        onToastListener=listener;
        if(result == null){
            result = new ToastEUtil(context);
        }else{
            if(!mContext.getClass().getName().equals(context.getClass().getName())){
                result = new ToastEUtil(context);
            }
        }
        mTextView.setText(message);
        if(stutas == TOAST_STATUS_FAILE){
            statusImg.setImageResource(R.mipmap.attention_message3x);
        }else{
            statusImg.setImageResource(R.mipmap.success_message3x);
        }
        return result;
    }

    public void show() {
        if(isShow){
            return;
        }
        isShow = true;
        mFadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        mFadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        mFadeOutAnimation.setDuration(ANIMATION_DURATION);
        mFadeOutAnimation
                .setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        isShow = false;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mContainer.setVisibility(View.GONE);
                        if (onToastListener != null) {
                            onToastListener.animFinish();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
        mContainer.setVisibility(View.VISIBLE);
        mFadeInAnimation.setDuration(ANIMATION_DURATION);
        mContainer.startAnimation(mFadeInAnimation);
        mHandler.postDelayed(mHideRunnable, HIDE_DELAY);
    }

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mContainer.startAnimation(mFadeOutAnimation);
        }
    };

    public void cancel(){
        if(isShow) {
            isShow = false;
            mContainer.setVisibility(View.GONE);
            mHandler.removeCallbacks(mHideRunnable);
        }
    }

    public static void reset(){
        result = null;
    }

    public void setText(CharSequence s){
        if(result == null) return;
        TextView mTextView = (TextView) view.findViewById(R.id.tips_tv);
        if(mTextView == null) throw new RuntimeException("This Toast was not created with Toast.makeText()");
        mTextView.setText(s);
    }

    public void setText(int resId) {
        setText(mContext.getText(resId));
    }

    public interface OnToastListener {
        void animFinish();
    }

}
