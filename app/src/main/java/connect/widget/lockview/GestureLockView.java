package connect.widget.lockview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.View;
public class GestureLockView extends View {
    private static final String TAG = "GestureLockView";
    /**
     * GestureLockView three states
     */
    enum Mode {
        STATUS_NO_FINGER, STATUS_FINGER_ON, STATUS_FINGER_UP
    }
    /**
     * The current state of GestureLockView
     */
    private Mode mCurrentStatus = Mode.STATUS_NO_FINGER;
    /**
     * width
     */
    private int mWidth;
    /**
     * height
     */
    private int mHeight;
    /**
     * Outer radius
     */
    private int mRadius;
    /**
     * The width of the brush
     */
    private int mStrokeWidth = 2;
    /**
     * Center coordinates
     */
    private int mCenterX;
    private int mCenterY;
    private Paint mPaint;
    /**
     * Radius of inner circle = mInnerCircleRadiusRate * mRadus
     */
    private float mInnerCircleRadiusRate = 0.3F;
    /**
     * Four colors, can be customized by user, initialized by GestureLockViewGroup incoming
     */
    private int mColorNoFingerOutter;
    private int mColorFingerOn;
    private int mColorFingerUp;

    public GestureLockView(Context context, int colorNoFingerOutter, int colorFingerOn, int colorFingerUp) {
        super(context);
        this.mColorNoFingerOutter = colorNoFingerOutter;
        this.mColorFingerOn = colorFingerOn;
        this.mColorFingerUp = colorFingerUp;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        // Take a small value in length and width
        mWidth = mWidth < mHeight ? mWidth : mHeight;
        mRadius = mCenterX = mCenterY = mWidth / 2;
        mRadius -= mStrokeWidth / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        switch (mCurrentStatus) {
            case STATUS_FINGER_ON:
                // Draw the outer circle
                mPaint.setStyle(Style.STROKE);
                mPaint.setColor(mColorFingerOn);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                // Draw inner circle
                mPaint.setStyle(Style.FILL);
                canvas.drawCircle(mCenterX, mCenterY, mRadius
                        * mInnerCircleRadiusRate, mPaint);
                break;
            case STATUS_FINGER_UP:
                // Draw the outer circle
                mPaint.setColor(mColorFingerUp);
                mPaint.setStyle(Style.STROKE);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                break;

            case STATUS_NO_FINGER:
                // Draw the outer circle
                mPaint.setStyle(Style.STROKE);
                mPaint.setStrokeWidth(2);
                mPaint.setColor(mColorNoFingerOutter);
                canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                break;

        }

    }

    /**
     * Set the current model and interface
     *
     * @param mode
     */
    public void setMode(Mode mode) {
        this.mCurrentStatus = mode;
        invalidate();
    }

}
