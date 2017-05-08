package connect.view.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import connect.ui.activity.R;

/**
 * Shooting video button
 * Created by Administrator on 2017/2/7.
 */

public class VideoBtnView extends LinearLayout{

    private final ImageView outCircularImg;
    private final ImageView inCircularImg;
    private final CricleProgressbar progressbar;
    private OnTouchStatusListence onTouchStatusListence;
    private boolean isLong = false;

    public VideoBtnView(Context context) {
        this(context,null);
    }

    public VideoBtnView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VideoBtnView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(context, R.layout.view_video_btn, this);
        outCircularImg = (ImageView)view.findViewById(R.id.out_circular_img);
        inCircularImg = (ImageView)view.findViewById(R.id.in_circular_img);
        progressbar = (CricleProgressbar)view.findViewById(R.id.myProgressBar);
        this.setClipChildren(false);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onTouchStatusListence.clickView();
            }
        });

        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showVideoAni();
                setLong(true);
                onTouchStatusListence.longClickView();
                return false;
            }
        });
    }

    public void setOnTouchStatusListence(OnTouchStatusListence onTouchStatusListence){
        this.onTouchStatusListence = onTouchStatusListence;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if(action == MotionEvent.ACTION_UP && isLong){
            isLong = false;
            progressbar.setVisibility(GONE);
            showPictureAni();
            onTouchStatusListence.cancleLongClick();
            return true;
        }
        return super.onTouchEvent(ev);
    }

    public void showVideoAni(){
        Animation animationOut = getAnimScale(1.4f,1.4f);
        Animation animationIn = getAnimScale(0.75f,0.75f);
        outCircularImg.startAnimation(animationOut);
        inCircularImg.startAnimation(animationIn);
    }

    private void showPictureAni(){
        Animation animationOut = getAnimScale(1f,1f);
        Animation animationIn = getAnimScale(1f,1f);
        outCircularImg.startAnimation(animationOut);
        inCircularImg.startAnimation(animationIn);
    }

    private Animation getAnimScale(float toX,float toY){
        Animation scaleAnimation = new ScaleAnimation(1f, toX,1f,toY,
                Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setDuration(200);
        return scaleAnimation;
    }

    public void setPeogressCricle(float length){
        if(length >= CameraTakeActivity.MAX_LENGTH / 1000){
            isLong = false;
            progressbar.setVisibility(GONE);
            showPictureAni();
        }else{
            progressbar.setVisibility(VISIBLE);
            progressbar.setEndAngle(length * 36);
        }
    }

     public interface OnTouchStatusListence{
         void clickView();
         void longClickView();
         void cancleLongClick();
    }

    public boolean isLong() {
        return isLong;
    }

    public void setLong(boolean aLong) {
        isLong = aLong;
    }
}
