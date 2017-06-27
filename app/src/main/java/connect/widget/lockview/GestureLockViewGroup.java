package connect.widget.lockview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import connect.ui.activity.R;
import connect.utils.ToastEUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.widget.lockview.GestureLockView.Mode;
/**
 * The whole contains n * n GestureLockView, each interval between GestureLockView mMarginBetweenLockView,
 * the outermost layer of GestureLockView with container presence mMarginBetweenLockView outside margin
 * About GestureLockView side length (n * n): n * mGestureLockViewWidth + ( n + 1 ) *
 * mMarginBetweenLockView = mWidth ;get:mGestureLockViewWidth = 4 * mWidth / ( 5
 * * mCount + 1 ) note:mMarginBetweenLockView = mGestureLockViewWidth * 0.25 ;
 */
public class GestureLockViewGroup extends RelativeLayout {

    private static final String TAG = "GestureLockViewGroup";
    /**
     * Save all GestureLockView
     */
    private GestureLockView[] mGestureLockViews;
    /**
     * The number of GestureLockView on each side
     */
    private int mCount = 4;
    /**
     * Store the answer
     */
    private String mAnswer = "";
    /**
     * Save the user's selected GestureLockView id
     */
    private ArrayList<Integer> mChoose = new ArrayList<>();

    private Paint mPaint;
    /**
     * The spacing between each GestureLockView is set to: mGestureLockViewWidth * 25%
     */
    private int mMarginBetweenLockView = 30;
    /**
     * GestureLockView side length 4 * mWidth / (5 * mCount + 1)
     */
    private int mGestureLockViewWidth;
    /**
     * GestureLockView No color in the state of the outside of the finger touch
     */
    private int mNoFingerOuterCircleColor = 0xFF007AFF;
    /**
     * GestureLockView finger touches the state of the inner and outer circles of the color
     */
    private int mFingerOnColor = 0xFF007AFF;
    /**
     * GestureLockView Finger raised in the state of the inner and outer colors
     */
    private int mFingerUpColor = 0xFF007AFF;

    /**
     * width
     */
    private int mWidth;
    /**
     * height
     */
    private int mHeight;

    private Path mPath;
    /**
     * The starting position of the line x
     */
    private int mLastPathX;
    /**
     * The starting position of the guide line
     */
    private int mLastPathY;
    /**
     * Under the guidance of the end position
     */
    private Point mTmpTarget = new Point();
    /**
     * Maximum number of attempts
     */
    private int mTryTimes = 4;
    /**
     * Callback interface
     */
    private OnGestureLockViewListener mOnGestureLockViewListener;
    private String salt = "";
    private String priKey;

    public GestureLockViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureLockViewGroup(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        /**
         * Gets the value of all custom parameters
         */
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.GestureLockViewGroup, defStyle, 0);
        int n = a.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.GestureLockViewGroup_color_no_finger_outer_circle:
                    mNoFingerOuterCircleColor = a.getColor(attr,
                            mNoFingerOuterCircleColor);
                    break;
                case R.styleable.GestureLockViewGroup_color_finger_on:
                    mFingerOnColor = a.getColor(attr, mFingerOnColor);
                    break;
                case R.styleable.GestureLockViewGroup_color_finger_up:
                    mFingerUpColor = a.getColor(attr, mFingerUpColor);
                    break;
                case R.styleable.GestureLockViewGroup_count:
                    mCount = a.getInt(attr, 3);
                    break;
                case R.styleable.GestureLockViewGroup_tryTimes:
                    mTryTimes = a.getInt(attr, 4);
                default:
                    break;
            }
        }

        a.recycle();

        // Initialize the brush
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mHeight = mWidth = mWidth < mHeight ? mWidth : mHeight;
        if (mGestureLockViews == null) {
            mGestureLockViews = new GestureLockView[mCount * mCount];
            // Calculate the width of each GestureLockView
            mGestureLockViewWidth = (int) (4 * mWidth * 1.0f / (5 * mCount + 1));
            // Calculate the spacing of each GestureLockView
            mMarginBetweenLockView = (int) (mGestureLockViewWidth * 0.25);
            // Set the width of the brush to the GestureLockView's inner diameter slightly smaller
            mPaint.setStrokeWidth(mGestureLockViewWidth * 0.29f);

            for (int i = 0; i < mGestureLockViews.length; i++) {
                // Initialize each GestureLockView
                mGestureLockViews[i] = new GestureLockView(getContext(),mNoFingerOuterCircleColor,
                        mFingerOnColor, mFingerUpColor);
                mGestureLockViews[i].setId(i + 1);
                // Set the parameters, mainly to locate the location between GestureLockView
                LayoutParams lockerParams = new LayoutParams(
                        mGestureLockViewWidth, mGestureLockViewWidth);

                // Not the first of each line, then set the position to the right of the previous one
                if (i % mCount != 0) {
                    lockerParams.addRule(RelativeLayout.RIGHT_OF,
                            mGestureLockViews[i - 1].getId());
                }
                // Starting from the second line, set to the same line below the same location
                if (i > mCount - 1) {
                    lockerParams.addRule(RelativeLayout.BELOW,
                            mGestureLockViews[i - mCount].getId());
                }
                // Set the margins on the lower right and left
                int rightMargin = mMarginBetweenLockView;
                int bottomMargin = mMarginBetweenLockView;
                int leftMagin = 0;
                int topMargin = 0;
                /**
                 * Each View has a right outer margin and an outer margin. The first row has an upper margin.
                 * The first column has a left outer margin
                 */
                if (i >= 0 && i < mCount)// first row
                {
                    topMargin = mMarginBetweenLockView;
                }
                if (i % mCount == 0)// first row
                {
                    leftMagin = mMarginBetweenLockView;
                }

                lockerParams.setMargins(leftMagin, topMargin, rightMargin,
                        bottomMargin);
                mGestureLockViews[i].setMode(Mode.STATUS_NO_FINGER);
                addView(mGestureLockViews[i], lockerParams);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // reset
                reset();
                break;
            case MotionEvent.ACTION_MOVE:
                mPaint.setColor(mFingerOnColor);
                mPaint.setAlpha(50);
                GestureLockView child = getChildIdByPos(x, y);
                if (child != null) {
                    int cId = child.getId();
                    if (!mChoose.contains(cId)) {
                        mChoose.add(cId);
                        child.setMode(Mode.STATUS_FINGER_ON);
                        if (mOnGestureLockViewListener != null)
                            mOnGestureLockViewListener.onBlockSelected(cId);
                        // Set the starting point of the finger
                        mLastPathX = child.getLeft() / 2 + child.getRight() / 2;
                        mLastPathY = child.getTop() / 2 + child.getBottom() / 2;

                        if (mChoose.size() == 1)// Currently added as the first one
                        {
                            mPath.moveTo(mLastPathX, mLastPathY);
                        } else
                        // Not the first one, the two use the line connected
                        {
                            mPath.lineTo(mLastPathX, mLastPathY);
                        }
                    }
                }
                // The end of the line
                mTmpTarget.x = x;
                mTmpTarget.y = y;
                break;
            case MotionEvent.ACTION_UP:

                mPaint.setColor(mFingerUpColor);
                mPaint.setAlpha(50);
                // When the connection point is greater than or equal to 4 before the callback
                if (mChoose.size() >= 4) {
                    this.mTryTimes--;
                    if (mOnGestureLockViewListener != null) {
                        boolean isTrue = checkAnswer();
                        mOnGestureLockViewListener.onGestureEvent(isTrue);
                        if (!isTrue && this.mTryTimes == 0) {
                            mOnGestureLockViewListener.onUnmatchedExceedBoundary();
                        }
                    }
                } else {
                    ToastEUtil.makeText(getContext(), R.string.Set_Please_connect_at_least_4_points, ToastEUtil.TOAST_STATUS_FAILE).show();
                }
                reset();
                // Set the end point as the starting point, ie cancel the finger
                mTmpTarget.x = mLastPathX;
                mTmpTarget.y = mLastPathY;

                // Change the state of the child element to UP
                changeItemMode();
                break;

        }
        invalidate();
        return true;
    }

    private void changeItemMode() {
        for (GestureLockView gestureLockView : mGestureLockViews) {
            if (mChoose.contains(gestureLockView.getId())) {
                gestureLockView.setMode(Mode.STATUS_FINGER_UP);
            }
        }
    }

    /**
     * Do some necessary reset
     */
    private void reset() {
        mChoose.clear();
        mPath.reset();
        for (GestureLockView gestureLockView : mGestureLockViews) {
            gestureLockView.setMode(Mode.STATUS_NO_FINGER);
        }
    }

    /**
     * Check that the user's gestures are correct
     * @return
     */
    private boolean checkAnswer() {
        String value = "";
        for (Integer integer : mChoose) {
            value = value + integer;
        }
        if (TextUtils.isEmpty(mAnswer)) {
            return false;
        }

        priKey = SupportKeyUril.decodePri(mAnswer, salt, value);
        return priKey != null && SupportKeyUril.checkPrikey(priKey);
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }

    /**
     * Check whether the current left is in child
     * @param child
     * @param x
     * @param y
     * @return
     */
    private boolean checkPositionInChild(View child, int x, int y) {
        // Set the inner margin,
        // that is, x, y must fall into the middle of the inner GestureLockView in the middle of the small area,
        // you can adjust the padding so that x, y fall into the range does not change, or do not set padding
        int padding = (int) (mGestureLockViewWidth * 0.15);

        return x >= child.getLeft() + padding && x <= child.getRight() - padding
                && y >= child.getTop() + padding
                && y <= child.getBottom() - padding;
    }

    /**
     * Get the GestureLockView through x, y
     * @param x
     * @param y
     * @return
     */
    private GestureLockView getChildIdByPos(int x, int y) {
        for (GestureLockView gestureLockView : mGestureLockViews) {
            if (checkPositionInChild(gestureLockView, x, y)) {
                return gestureLockView;
            }
        }

        return null;

    }

    /**
     * Set the callback interface
     * @param listener
     */
    public void setOnGestureLockViewListener(OnGestureLockViewListener listener) {
        this.mOnGestureLockViewListener = listener;
    }

    /**
     * Set the answer
     * @param answer
     */
    public void setAnswer(String answer, String salt) {
        this.mAnswer = answer;
        this.salt = salt;
    }

    public String getAnswer() {
        return mAnswer;
    }

    public String getSalt() {
        return salt;
    }

    /**
     * Gets the result of the selection
     * @return
     */
    public ArrayList<Integer> getMChoose() {
        return mChoose;
    }

    /**
     * Set the maximum number of experiments
     * @param boundary
     */
    public void setUnMatchExceedBoundary(int boundary) {
        this.mTryTimes = boundary;
    }

    public int getUnMatchExceedBoundary() {
        return mTryTimes;
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // Draw a connection between GestureLockView
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
        // Draw a guideline
        if (mChoose.size() > 0) {
            if (mLastPathX != 0 && mLastPathY != 0)
                canvas.drawLine(mLastPathX, mLastPathY, mTmpTarget.x,
                        mTmpTarget.y, mPaint);
        }

    }

    public interface OnGestureLockViewListener {
        /**
         * Separate the element's Id alone
         */
        void onBlockSelected(int cId);

        /**
         * Whether it matches
         * @param matched
         */
        void onGestureEvent(boolean matched);

        /**
         * More than the number of attempts
         */
        void onUnmatchedExceedBoundary();
    }
}
