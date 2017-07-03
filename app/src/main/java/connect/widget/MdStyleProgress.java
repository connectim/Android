package connect.widget;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import connect.ui.activity.R;

/**
 * Created by Administrator on 2017/3/1.
 */

public class MdStyleProgress extends View {

    private static final int PROGRESS_COLOR = Color.parseColor("#007aff");
    private static final int PROGRESS_WIDTH = 3;
    private static final int RADIUS = 25;

    private int mProgressColor = PROGRESS_COLOR;
    private int mProgressWidth = dp2px(PROGRESS_WIDTH);
    private int mRadius = dp2px(RADIUS);

    private Paint progressPaint;

    private int rotateDelta = 7;
    private int curAngle = 0;

    private int minAngle = -90;
    private int startAngle = -90;
    private int endAngle = 120;

    private Path path;
    private Status mStatus = Status.Loading;
    private float lineValueLeft;//On the left side of the hook
    private float lineValueRight;//The right to check
    private float failLineFirst;//cross
    private float failLineSecond;

    private float statusCricle;
    private float failAdd = 5;

    public MdStyleProgress(Context context) {
        this(context,null);
    }

    public MdStyleProgress(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MdStyleProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //Set brush
        setPaint();
        //Get custom attributes
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MdStyleProgress);
        int indexCount = typedArray.getIndexCount();
        for (int i=0;i<indexCount;i++){
            int attr = typedArray.getIndex(i);
            switch (attr){
                case R.styleable.MdStyleProgress_progress_color:
                    mProgressColor = typedArray.getColor(attr,PROGRESS_COLOR);
                    break;
                case R.styleable.MdStyleProgress_progress_width:
                    mProgressWidth = (int) typedArray.getDimension(attr,mProgressWidth);
                    break;
                case R.styleable.MdStyleProgress_radius:
                    mRadius = (int) typedArray.getDimension(attr,mRadius);
                    break;
            }
        }
        //Retrieve TypedArray objects
        typedArray.recycle();

        path = new Path();
        path.moveTo(mRadius/2,mRadius);
        path.lineTo(mRadius,mRadius+mRadius/2);
        path.lineTo(mRadius+mRadius/2,mRadius/2);

    }

    private void setPaint() {
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setDither(true);
        progressPaint.setColor(mProgressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(mProgressWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int widthSize;
        int heightSize;
        if(widthMode != MeasureSpec.EXACTLY){
            widthSize = getPaddingLeft() + mProgressWidth + mRadius*2 + getPaddingRight();
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize,MeasureSpec.EXACTLY);
        }
        if(heightMode != MeasureSpec.EXACTLY){
            heightSize = getPaddingTop() + mProgressWidth + mRadius*2 + getPaddingBottom();
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize,MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(getPaddingLeft(),getPaddingTop());

        if(mStatus == Status.Loading){
            if (startAngle == minAngle) {
                endAngle += 6;
            }
            if (endAngle >= 300 || startAngle > minAngle) {
                startAngle += 6;
                if(endAngle > 20) {
                    endAngle -= 6;
                }
            }
            if (startAngle > minAngle + 300) {
                minAngle = startAngle;
                endAngle = 20;
            }
            canvas.rotate(curAngle += rotateDelta,mRadius,mRadius);//rotating
            canvas.drawArc(new RectF(0,0,mRadius*2,mRadius*2),startAngle,endAngle,false,progressPaint);
            invalidate();
        }else if(mStatus == Status.LoadSuccess){
            canvas.drawArc(new RectF(0,0,mRadius*2,mRadius*2),startAngle + endAngle,statusCricle,false,progressPaint);
            canvas.drawLine(mRadius/2,mRadius*95/100f,mRadius/2+lineValueLeft,mRadius*95/100f+lineValueLeft,progressPaint);
            canvas.drawLine(mRadius*17/20f,mRadius+mRadius*3/10f,mRadius*17/20f+lineValueRight*13/12f,mRadius+mRadius*3/10f-lineValueRight,progressPaint);
        }else {
            canvas.drawArc(new RectF(0,0,mRadius*2,mRadius*2),startAngle + endAngle,statusCricle,false,progressPaint);

            canvas.drawLine(mRadius+mRadius/2-failAdd,mRadius/2+failAdd,mRadius*3/2-failAdd-failLineFirst,mRadius/2+failAdd+failLineFirst,progressPaint);
            canvas.drawLine(mRadius/2+failAdd,mRadius/2+failAdd,mRadius/2+failLineSecond+failAdd,mRadius/2+failLineSecond+failAdd,progressPaint);
        }

        canvas.restore();
    }


    public enum Status{
        Loading,
        LoadSuccess,
        LoadFail
    }

    public void startAnima(){
        //The properties of the animation to hook on the left line
        ValueAnimator animatorLeft = ValueAnimator.ofFloat(0f,mRadius*7/20f);
        animatorLeft.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lineValueLeft = (float) animation.getAnimatedValue();
                invalidate();//Redrawn re-paint, adjustable ontouch ()
            }
        });
        animatorLeft.setDuration(400);
        //The properties of the animation to hook on the right side of the line
        ValueAnimator animatorRight = ValueAnimator.ofFloat(0f,mRadius*3/5f);
        animatorRight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lineValueRight = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animatorRight.setDuration(400);
        //Will be combined into multiple animation
        ValueAnimator cricleAni = getCeicleAni();
        cricleAni.setDuration(800);

        AnimatorSet animatorSetOne = new AnimatorSet();
        animatorSetOne.play(animatorRight).after(animatorLeft);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animatorSetOne).with(cricleAni);
        animatorSet.start();
    }

    public void failAnima(){
        ValueAnimator failOne = ValueAnimator.ofFloat(0f,mRadius-2*failAdd);
        failOne.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                failLineFirst = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        failOne.setDuration(400);
        ValueAnimator failOther = ValueAnimator.ofFloat(0f,mRadius-2*failAdd);
        failOther.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                failLineSecond = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        failOther.setDuration(400);

        ValueAnimator cricleAni = getCeicleAni();
        cricleAni.setDuration(800);

        AnimatorSet animatorSetOne = new AnimatorSet();
        animatorSetOne.play(failOther).after(failOne);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animatorSetOne).with(cricleAni);
        animatorSet.start();
    }

    private ValueAnimator getCeicleAni(){
        int endAng = 360 - endAngle;
        ValueAnimator animatorCricle = ValueAnimator.ofFloat(0f,360f);
        animatorCricle.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                statusCricle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        return animatorCricle;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status mStatus) {
        this.mStatus = mStatus;
        invalidate();
    }

    /**
     * dp 2 px
     */
    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }
    /**
     * sp 2 px
     */
    protected int sp2px(int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, getResources().getDisplayMetrics());
    }

}
