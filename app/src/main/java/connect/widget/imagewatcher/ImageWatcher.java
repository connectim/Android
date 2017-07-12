package connect.widget.imagewatcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import connect.ui.activity.R;

/**
 * Image viewer
 */
public class ImageWatcher extends FrameLayout implements GestureDetector.OnGestureListener, ViewPager.OnPageChangeListener {
    private static final int SINGLE_TAP_UP_CONFIRMED = 1;
    private final Handler mHandler;

    static final float MIN_SCALE = 0.5f;
    static final float MAX_SCALE = 3.8f;
    private int maxTranslateX;
    private int maxTranslateY;

    private static final int TOUCH_MODE_NONE = 0; // stateless
    private static final int TOUCH_MODE_DOWN = 1; // Press the
    private static final int TOUCH_MODE_DRAG = 2; // A single point of drag and drop
    private static final int TOUCH_MODE_EXIT = 3; // Out of action
    private static final int TOUCH_MODE_SLIDE = 4; // Slide the page
    private static final int TOUCH_MODE_SCALE_ROTATE = 5; // Zooming rotation
    private static final int TOUCH_MODE_LOCK = 6; // Zoom lock
    private static final int TOUCH_MODE_AUTO_FLING = 7; // In the animation

    private final float tCurrentIdxTransY;
    private final TextView tCurrentIdx;
    private ImageView iSource;
    private ImageView iOrigin;

    private int mErrorImageRes = R.mipmap.img_default;
    private int mStatusBarHeight;
    private int mWidth, mHeight;
    private int mBackgroundColor = 0x00000000;
    private int mTouchMode = TOUCH_MODE_NONE;
    private final float mTouchSlop;

    private float mFingersDistance;
    private double mFingersAngle; // Compared with [the east] point0 as a starting point;Point1 as the finish
    private float mFingersCenterX;
    private float mFingersCenterY;
    private float mExitScalingRef; // Touch to exit the progress

    private ValueAnimator animBackground;
    private ValueAnimator animImageTransform;
    private boolean isInTransformAnimation;
    private final GestureDetector mGestureDetector;

    private OnPictureLongPressListener mPictureLongPressListener;
    private ImagePagerAdapter adapter;
    private final ViewPager vPager;
    private List<ImageView> mImageGroupList;
    private List<String> mUrlList;
    private int initPosition;
    private int mPagerPositionOffsetPixels;
    private List<ImageView> listImageView = new ArrayList<>();
    private List<String> listPath = new ArrayList<>();

    public ImageWatcher(Context context) {
        this(context, null);
    }

    public ImageWatcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandler = new GestureHandler(this);
        mGestureDetector = new GestureDetector(context, this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        addView(vPager = new ViewPager(getContext()));
        vPager.addOnPageChangeListener(this);
        setVisibility(View.INVISIBLE);

        addView(tCurrentIdx = new TextView(context));
        LayoutParams lpCurrentIdx = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpCurrentIdx.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        tCurrentIdx.setLayoutParams(lpCurrentIdx);
        tCurrentIdx.setTextColor(0xFFFFFFFF);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        tCurrentIdxTransY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, displayMetrics) + 0.5f;
        tCurrentIdx.setTranslationY(tCurrentIdxTransY);
    }

    public void showSingle(ImageView i, ImageView img, String path) {
        listImageView.clear();
        listPath.clear();
        listImageView.add(img);
        listPath.add(path);
        show(i,listImageView,listPath);
    }

    /**
     * @param i              By clicking the ImageView
     * @param imageGroupList Clicked ImageView's list and loading pictures will show in the list have the download is complete in advance when the thumb of images
     * @param urlList        Loaded picture url list, the amount must be greater than or equal to imageGroupList. Size.And shall be consistent with imageGroupList sequence
     */
    public void show(ImageView i, List<ImageView> imageGroupList, final List<String> urlList) {
        if (i == null || imageGroupList == null || urlList == null || imageGroupList.size() < 1 ||
                urlList.size() < imageGroupList.size()) {
            String info = "i[" + i + "]";
            info += "#imageGroupList " + (imageGroupList == null ? "null" : "size : " + imageGroupList.size());
            info += "#urlList " + (urlList == null ? "null" : "size :" + urlList.size());
            throw new IllegalArgumentException("error params \n" + info);
        }
        initPosition = imageGroupList.indexOf(i);
        if (initPosition < 0) {
            throw new IllegalArgumentException("param ImageView i must be a member of the List <ImageView> imageGroupList!");
        }

        if (i.getDrawable() == null) return;

        if (animImageTransform != null) animImageTransform.cancel();
        animImageTransform = null;

        mImageGroupList = imageGroupList;
        mUrlList = urlList;

        iOrigin = null;
        iSource = null;

        ImageWatcher.this.setVisibility(View.VISIBLE);
        vPager.setAdapter(adapter = new ImagePagerAdapter());
        vPager.setCurrentItem(initPosition);
        refreshCurrentIdx(initPosition);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (iSource == null) return true;
        if (isInTransformAnimation) return true;

        ViewState vsDefault = ViewState.read(iSource, ViewState.STATE_DEFAULT);

        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_UP:
                onUp(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (vsDefault != null && (mTouchMode != TOUCH_MODE_SLIDE) || mPagerPositionOffsetPixels == 0) {
                    if (mTouchMode != TOUCH_MODE_SCALE_ROTATE) {
                        mFingersDistance = 0;
                        mFingersAngle = 0;
                        mFingersCenterX = 0;
                        mFingersCenterY = 0;
                        ViewState.write(iSource, ViewState.STATE_TOUCH_SCALE_ROTATE);
                    }
                    mTouchMode = TOUCH_MODE_SCALE_ROTATE;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (vsDefault != null && mTouchMode != TOUCH_MODE_SLIDE) {
                    if (event.getPointerCount() - 1 < 1 + 1) {
                        mTouchMode = TOUCH_MODE_LOCK;
                    }
                }
                break;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mTouchMode = TOUCH_MODE_DOWN;
        ViewState.write(iSource, ViewState.STATE_TOUCH_DOWN);
        vPager.onTouchEvent(e);
        return true;
    }

    public void onUp(MotionEvent e) {
        if (mTouchMode == TOUCH_MODE_EXIT) {
            handleExitTouchResult();
        } else if (mTouchMode == TOUCH_MODE_SCALE_ROTATE
                || mTouchMode == TOUCH_MODE_LOCK) {
            handleScaleRotateTouchResult();
        } else if (mTouchMode == TOUCH_MODE_DRAG) {
            handleDragTouchResult();
        }
        try {
            vPager.onTouchEvent(e);
        } catch (Exception err) {
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        final float moveX = (e1 != null) ? e2.getX() - e1.getX() : 0;
        final float moveY = (e1 != null) ? e2.getY() - e1.getY() : 0;
        if (mTouchMode == TOUCH_MODE_DOWN) {
            if (Math.abs(moveX) > mTouchSlop || Math.abs(moveY) > mTouchSlop) {
                ViewState vsCurrent = ViewState.write(iSource, ViewState.STATE_CURRENT);
                ViewState vsDefault = ViewState.read(iSource, ViewState.STATE_DEFAULT);

                if (vsDefault == null) {
                    // No vsDefault marks the View that is being downloaded.Into Slide gestures, can undertake viewpager page Slide
                    mTouchMode = TOUCH_MODE_SLIDE;
                } else if (vsCurrent.scaleY > vsDefault.scaleY || vsCurrent.scaleX > vsDefault.scaleX) {
                    // Current state to enlarge picture.Wide or high beyond the screen size
                    if (mTouchMode != TOUCH_MODE_DRAG) {
                        ViewState.write(iSource, ViewState.STATE_DRAG);
                    }
                    // Converted to Drag gestures, can Drag and drop images
                    mTouchMode = TOUCH_MODE_DRAG;
                    String imageOrientation = (String) iSource.getTag(R.id.image_orientation);
                    if ("horizontal".equals(imageOrientation)) {
                        float translateXEdge = vsDefault.width * (vsCurrent.scaleX - 1) / 2;
                        if (vsCurrent.translationX >= translateXEdge && moveX > 0) {
                            // Image is located in the border, and still try to drag outside the boundary.Into Slide gestures, can undertake viewpager page Slide
                            mTouchMode = TOUCH_MODE_SLIDE;
                        } else if (vsCurrent.translationX <= -translateXEdge && moveX < 0) {
                            mTouchMode = TOUCH_MODE_SLIDE;
                        }
                    } else if ("vertical".equals(imageOrientation)) {
                        if (vsDefault.width * vsCurrent.scaleX <= mWidth) {
                            mTouchMode = TOUCH_MODE_SLIDE;
                        } else {
                            float translateXRightEdge = vsDefault.width * vsCurrent.scaleX / 2 - vsDefault.width / 2;
                            float translateXLeftEdge = mWidth - vsDefault.width * vsCurrent.scaleX / 2 - vsDefault.width / 2;
                            if (vsCurrent.translationX >= translateXRightEdge && moveX > 0) {
                                mTouchMode = TOUCH_MODE_SLIDE;
                            } else if (vsCurrent.translationX <= translateXLeftEdge && moveX < 0) {
                                mTouchMode = TOUCH_MODE_SLIDE;
                            }
                        }
                    }
                } else if (Math.abs(moveX) < mTouchSlop && moveY > mTouchSlop * 3) {
                    // Single hand vertical drop-down.Into the Exit sign, can see the original interface, and in the process of the drop-down
                    mTouchMode = TOUCH_MODE_EXIT;
                } else if (Math.abs(moveX) > mTouchSlop) {
                    // Sliding around.Into Slide gestures, can undertake viewpager page Slide
                    mTouchMode = TOUCH_MODE_SLIDE;
                }
            }
        }

        if (mTouchMode == TOUCH_MODE_SLIDE) {
            vPager.onTouchEvent(e2);
        } else if (mTouchMode == TOUCH_MODE_SCALE_ROTATE) {
            handleScaleRotateGesture(e2);
        } else if (mTouchMode == TOUCH_MODE_EXIT) {
            handleExitGesture(e2, e1);
        } else if (mTouchMode == TOUCH_MODE_DRAG) {
            handleDragGesture(e2, e1);
        }
        return false;
    }

    /**
     * Handle click finger events
     */
    public boolean onSingleTapConfirmed() {
        if (iSource == null) return false;
        ViewState vsCurrent = ViewState.write(iSource, ViewState.STATE_CURRENT);
        ViewState vsDefault = ViewState.read(iSource, ViewState.STATE_DEFAULT);
        if (vsDefault == null || (vsCurrent.scaleY <= vsDefault.scaleY && vsCurrent.scaleX <= vsDefault.scaleX)) {
            mExitScalingRef = 0;
        } else {
            mExitScalingRef = 1;
        }
        handleExitTouchResult();
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        boolean hadTapMessage = mHandler.hasMessages(SINGLE_TAP_UP_CONFIRMED);
        if (hadTapMessage) {
            mHandler.removeMessages(SINGLE_TAP_UP_CONFIRMED);
            handleDoubleTapTouchResult();
            return true;
        } else {
            mHandler.sendEmptyMessageDelayed(SINGLE_TAP_UP_CONFIRMED, 350);
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (mPictureLongPressListener != null) {
            mPictureLongPressListener.onPictureLongPress(iSource, mUrlList.get(vPager.getCurrentItem()), vPager.getCurrentItem());
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /**
     * Treatment response to exit the picture viewer
     */
    private void handleExitGesture(MotionEvent e2, MotionEvent e1) {
        if (iSource == null) return;
        ViewState vsTouchDown = ViewState.read(iSource, ViewState.STATE_TOUCH_DOWN);
        if (vsTouchDown == null) return;

        mExitScalingRef = 1;
        final float moveY = e2.getY() - e1.getY();
        final float moveX = e2.getX() - e1.getX();
        if (moveY > 0) {
            mExitScalingRef -= moveY / getHeight();
        }
        if (mExitScalingRef < MIN_SCALE) mExitScalingRef = MIN_SCALE;

        iSource.setTranslationX(vsTouchDown.translationX + moveX);
        iSource.setTranslationY(vsTouchDown.translationY + moveY);
        iSource.setScaleX(vsTouchDown.scaleX * mExitScalingRef);
        iSource.setScaleY(vsTouchDown.scaleY * mExitScalingRef);
        setBackgroundColor(mColorEvaluator.evaluate(mExitScalingRef, 0x00000000, 0xFF000000));
    }

    /**
     * Process the response hands drag zoom rotation
     */
    private void handleScaleRotateGesture(MotionEvent e2) {
        if (iSource == null) return;
        final ViewState vsDefault = ViewState.read(iSource, ViewState.STATE_DEFAULT);
        if (vsDefault == null) return;
        final ViewState vsTouchScaleRotate = ViewState.read(iSource, ViewState.STATE_TOUCH_SCALE_ROTATE);
        if (vsTouchScaleRotate == null) return;

        if (e2.getPointerCount() < 2) return;
        final float deltaX = e2.getX(1) - e2.getX(0);
        final float deltaY = e2.getY(1) - e2.getY(0);
        double angle = Math.toDegrees(Math.atan(deltaX / deltaY));
        if (deltaY < 0) angle = angle + 180;
        if (mFingersAngle == 0) mFingersAngle = angle;

        float changedAngle = (float) (mFingersAngle - angle);
        float changedAngleValue = (vsTouchScaleRotate.rotation + changedAngle) % 360;
        if (changedAngleValue > 180) {
            changedAngleValue = changedAngleValue - 360;
        } else if (changedAngleValue < -180) {
            changedAngleValue = changedAngleValue + 360;
        }
        iSource.setRotation(changedAngleValue);

        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (mFingersDistance == 0) mFingersDistance = distance;

        float changedScale = (mFingersDistance - distance) / (mWidth * 0.8f);
        float scaleResultX = vsTouchScaleRotate.scaleX - changedScale;
        if (scaleResultX < MIN_SCALE) scaleResultX = MIN_SCALE;
        else if (scaleResultX > MAX_SCALE) scaleResultX = MAX_SCALE;
        iSource.setScaleX(scaleResultX);
        float scaleResultY = vsTouchScaleRotate.scaleY - changedScale;
        if (scaleResultY < MIN_SCALE) scaleResultY = MIN_SCALE;
        else if (scaleResultY > MAX_SCALE) scaleResultY = MAX_SCALE;
        iSource.setScaleY(scaleResultY);

        float centerX = (e2.getX(1) + e2.getX(0)) / 2;
        float centerY = (e2.getY(1) + e2.getY(0)) / 2;
        if (mFingersCenterX == 0 && mFingersCenterY == 0) {
            mFingersCenterX = centerX;
            mFingersCenterY = centerY;
        }
        float changedCenterX = mFingersCenterX - centerX;
        float changedCenterXValue = vsTouchScaleRotate.translationX - changedCenterX;
        if (changedCenterXValue > maxTranslateX) changedCenterXValue = maxTranslateX;
        else if (changedCenterXValue < -maxTranslateX) changedCenterXValue = -maxTranslateX;
        iSource.setTranslationX(changedCenterXValue);

        float changedCenterY = mFingersCenterY - centerY;
        float changedCenterYValue = vsTouchScaleRotate.translationY - changedCenterY;
        if (changedCenterYValue > maxTranslateY) changedCenterYValue = maxTranslateY;
        else if (changedCenterYValue < -maxTranslateY) changedCenterYValue = -maxTranslateY;
        iSource.setTranslationY(changedCenterYValue);
    }

    /**
     * Process the response single hand drag the translation
     */
    private void handleDragGesture(MotionEvent e2, MotionEvent e1) {
        if (iSource == null) return;
        final float moveY = e2.getY() - e1.getY();
        final float moveX = e2.getX() - e1.getX();

        ViewState vsDefault = ViewState.read(iSource, ViewState.STATE_DEFAULT);
        if (vsDefault == null) return;
        ViewState vsTouchDrag = ViewState.read(iSource, ViewState.STATE_DRAG);
        if (vsTouchDrag == null) return;

        float translateXValue = vsTouchDrag.translationX + moveX * 1.6f;

        String imageOrientation = (String) iSource.getTag(R.id.image_orientation);
        if ("horizontal".equals(imageOrientation)) {
            float translateXEdge = vsDefault.width * (vsTouchDrag.scaleX - 1) / 2;
            if (translateXValue > translateXEdge) {
                translateXValue = translateXEdge + (translateXValue - translateXEdge) * 0.12f;
            } else if (translateXValue < -translateXEdge) {
                translateXValue = -translateXEdge + (translateXValue - (-translateXEdge)) * 0.12f;
            }
        } else if ("vertical".equals(imageOrientation)) {
            if (vsDefault.width * vsTouchDrag.scaleX <= mWidth) {
                mTouchMode = TOUCH_MODE_SLIDE;
            } else {
                float translateXRightEdge = vsDefault.width * vsTouchDrag.scaleX / 2 - vsDefault.width / 2;
                float translateXLeftEdge = mWidth - vsDefault.width * vsTouchDrag.scaleX / 2 - vsDefault.width / 2;

                if (translateXValue > translateXRightEdge) {
                    translateXValue = translateXRightEdge + (translateXValue - translateXRightEdge) * 0.12f;
                } else if (translateXValue < translateXLeftEdge) {
                    translateXValue = translateXLeftEdge + (translateXValue - translateXLeftEdge) * 0.12f;
                }
            }
        }
        iSource.setTranslationX(translateXValue);
        iSource.setTranslationY(vsTouchDrag.translationY + moveY * 1.6f);
    }

    /**
     * Processing end drop-down exit fingers events, exit the picture view or restore to the original state of final animation
     * You also need to restore the background color
     */
    private void handleExitTouchResult() {
        if (iSource == null) return;

        if (mExitScalingRef > 0.9f) {
            ViewState vsDefault = ViewState.read(iSource, ViewState.STATE_DEFAULT);
            if (vsDefault == null) return;
            animSourceViewStateTransform(iSource, vsDefault);
            animBackgroundTransform(0xFF000000);
        } else {
            ViewState vsOrigin = ViewState.read(iSource, ViewState.STATE_ORIGIN);
            if (vsOrigin == null) return;
            if (vsOrigin.alpha == 0)
                vsOrigin.translationX(iSource.getTranslationX()).translationY(iSource.getTranslationY());

            animSourceViewStateTransform(iSource, vsOrigin);
            animBackgroundTransform(0x00000000);

            ((FrameLayout) iSource.getParent()).getChildAt(2).animate().alpha(0).start();
        }
    }

    /**
     * Processing end double-click event finger, zoom to the designated size or restore to the original size of final animation
     */
    private void handleDoubleTapTouchResult() {
        if (iSource == null) return;
        ViewState vsDefault = ViewState.read(iSource, ViewState.STATE_DEFAULT);
        if (vsDefault == null) return;
        ViewState vsCurrent = ViewState.write(iSource, ViewState.STATE_CURRENT);

        if (vsCurrent.scaleY <= vsDefault.scaleY && vsCurrent.scaleX <= vsDefault.scaleX) {
            final float expectedScale = (MAX_SCALE - vsDefault.scaleX) * 0.4f + vsDefault.scaleX;
            animSourceViewStateTransform(iSource,
                    ViewState.write(iSource, ViewState.STATE_TEMP).scaleX(expectedScale).scaleY(expectedScale));
        } else {
            animSourceViewStateTransform(iSource, vsDefault);
        }
    }

    /**
     * Processing end zooming rotation pattern of fingers, to restore to zero rotation Angle and size shrinkage to ending animation within the normal range
     * If from {@link ImageWatcher#TOUCH_MODE_EXIT} halfway into events You also need to restore the background color
     */
    private void handleScaleRotateTouchResult() {
        if (iSource == null) return;
        ViewState vsDefault = ViewState.read(iSource, ViewState.STATE_DEFAULT);
        if (vsDefault == null) return;
        ViewState vsCurrent = ViewState.write(iSource, ViewState.STATE_CURRENT);

        final float endScaleX, endScaleY;
        Log.e("TTT", "AAA  vsCurrent.scaleX :" + vsCurrent.scaleX + "###  vsDefault.scaleX:" + vsDefault.scaleX);
        endScaleX = vsCurrent.scaleX < vsDefault.scaleX ? vsDefault.scaleX : vsCurrent.scaleX;
        endScaleY = vsCurrent.scaleY < vsDefault.scaleY ? vsDefault.scaleY : vsCurrent.scaleY;

        ViewState vsTemp = ViewState.copy(vsDefault, ViewState.STATE_TEMP).scaleX(endScaleX).scaleY(endScaleY);
        iSource.setTag(ViewState.STATE_TEMP, vsTemp);
        animSourceViewStateTransform(iSource, vsTemp);
        animBackgroundTransform(0xFF000000);
    }

    /**
     * Processing end drag model of fingers, over the border back to the boundary finishing animation
     */
    private void handleDragTouchResult() {
        if (iSource == null) return;
        ViewState vsDefault = ViewState.read(iSource, ViewState.STATE_DEFAULT);
        if (vsDefault == null) return;
        ViewState vsCurrent = ViewState.write(iSource, ViewState.STATE_CURRENT);

        final float endTranslateX, endTranslateY;
        String imageOrientation = (String) iSource.getTag(R.id.image_orientation);
        if ("horizontal".equals(imageOrientation)) {
            float translateXEdge = vsDefault.width * (vsCurrent.scaleX - 1) / 2;
            if (vsCurrent.translationX > translateXEdge) endTranslateX = translateXEdge;
            else if (vsCurrent.translationX < -translateXEdge)
                endTranslateX = -translateXEdge;
            else endTranslateX = vsCurrent.translationX;

            if (vsDefault.height * vsCurrent.scaleY <= mHeight) {
                endTranslateY = vsDefault.translationY;
            } else {
                float translateYBottomEdge = vsDefault.height * vsCurrent.scaleY / 2 - vsDefault.height / 2;
                float translateYTopEdge = mHeight - vsDefault.height * vsCurrent.scaleY / 2 - vsDefault.height / 2;

                if (vsCurrent.translationY > translateYBottomEdge)
                    endTranslateY = translateYBottomEdge;
                else if (vsCurrent.translationY < translateYTopEdge)
                    endTranslateY = translateYTopEdge;
                else endTranslateY = vsCurrent.translationY;
            }
        } else if ("vertical".equals(imageOrientation)) {
            float translateYEdge = vsDefault.height * (vsCurrent.scaleY - 1) / 2;
            if (vsCurrent.translationY > translateYEdge) endTranslateY = translateYEdge;
            else if (vsCurrent.translationY < -translateYEdge)
                endTranslateY = -translateYEdge;
            else endTranslateY = vsCurrent.translationY;

            if (vsDefault.width * vsCurrent.scaleX <= mWidth) {
                endTranslateX = vsDefault.translationX;
            } else {
                float translateXRightEdge = vsDefault.width * vsCurrent.scaleX / 2 - vsDefault.width / 2;
                float translateXLeftEdge = mWidth - vsDefault.width * vsCurrent.scaleX / 2 - vsDefault.width / 2;

                if (vsCurrent.translationX > translateXRightEdge)
                    endTranslateX = translateXRightEdge;
                else if (vsCurrent.translationX < translateXLeftEdge)
                    endTranslateX = translateXLeftEdge;
                else endTranslateX = vsCurrent.translationX;
            }
        } else {
            return;
        }
        if (vsCurrent.translationX == endTranslateX && vsCurrent.translationY == endTranslateY) {
            return;// If there is no change to skip the movie touch lock in time
        }
        animSourceViewStateTransform(iSource,
                ViewState.write(iSource, ViewState.STATE_TEMP).translationX(endTranslateX).translationY(endTranslateY));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mPagerPositionOffsetPixels = positionOffsetPixels;
    }

    /**
     * Whenever the ViewPager slide to a new page, this method will be triggered
     * Now essential needs an update at the top of the index, restore a after a state, etc
     */
    @Override
    public void onPageSelected(int position) {
        iSource = adapter.mImageSparseArray.get(position);
        if (iOrigin != null) {
            iOrigin.setVisibility(View.VISIBLE);
        }
        if (position < mImageGroupList.size()) {
            iOrigin = mImageGroupList.get(position);
            if (iOrigin.getDrawable() != null) iOrigin.setVisibility(View.INVISIBLE);
        }
        refreshCurrentIdx(position);

        ImageView mLast = adapter.mImageSparseArray.get(position - 1);
        if (ViewState.read(mLast, ViewState.STATE_DEFAULT) != null) {
            ViewState.restoreByAnim(mLast, ViewState.STATE_DEFAULT).create().start();
        }
        ImageView mNext = adapter.mImageSparseArray.get(position + 1);
        if (ViewState.read(mNext, ViewState.STATE_DEFAULT) != null) {
            ViewState.restoreByAnim(mNext, ViewState.STATE_DEFAULT).create().start();
        }
    }


    @Override
    public void onPageScrollStateChanged(int state) {
    }

    class ImagePagerAdapter extends PagerAdapter {
        private final LayoutParams lpCenter = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        private final SparseArray<ImageView> mImageSparseArray = new SparseArray<>();
        private boolean hasPlayBeginAnimation;

        @Override
        public int getCount() {
            return mUrlList != null ? mUrlList.size() : 0;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            mImageSparseArray.remove(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            FrameLayout itemView = new FrameLayout(container.getContext());
            container.addView(itemView);
            ImageView imageView = new ImageView(container.getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            itemView.addView(imageView);
            mImageSparseArray.put(position, imageView);

            MaterialProgressView loadView = new MaterialProgressView(container.getContext());
            lpCenter.gravity = Gravity.CENTER;
            loadView.setLayoutParams(lpCenter);
            itemView.addView(loadView);
            ImageView errorView = new ImageView(container.getContext());
            errorView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            errorView.setImageResource(mErrorImageRes);
            itemView.addView(errorView);
            errorView.setVisibility(View.GONE);

            if (setDefaultDisplayConfigs(imageView, position, hasPlayBeginAnimation)) {
                hasPlayBeginAnimation = true;
            }
            return itemView;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        /**
         * The current state of the update in the ViewPager each, such as loading, such as whether to load failure
         *
         * @param position The location of the current item
         * @param loading  Whether according to loading
         * @param error    Whether the failure display
         */
        void notifyItemChangedState(int position, boolean loading, boolean error) {
            ImageView imageView = mImageSparseArray.get(position);
            if (imageView != null) {
                FrameLayout itemView = (FrameLayout) imageView.getParent();
                MaterialProgressView loadView = (MaterialProgressView) itemView.getChildAt(1);
                if (loading) {
                    loadView.setVisibility(View.VISIBLE);
                    loadView.start();
                } else {
                    loadView.stop();
                    loadView.setVisibility(View.GONE);
                }

                ImageView errorView = (ImageView) itemView.getChildAt(2);
                errorView.setAlpha(1f);
                errorView.setVisibility(error ? View.VISIBLE : View.GONE);
            }
        }

        private boolean setDefaultDisplayConfigs(final ImageView imageView, final int pos, boolean hasPlayBeginAnimation) {
            boolean isFindEnterImagePicture = false;

            ViewState.write(imageView, ViewState.STATE_ORIGIN).alpha(0).scaleXBy(1.5f).scaleYBy(1.5f);
            if (pos < mImageGroupList.size()) {
                ImageView originRef = mImageGroupList.get(pos);
                if (pos == initPosition && !hasPlayBeginAnimation) {
                    isFindEnterImagePicture = true;
                    iSource = imageView;
                    iOrigin = originRef;
                }

                int[] location = new int[2];
                originRef.getLocationOnScreen(location);
                imageView.setTranslationX(location[0]);
                int locationYOfFullScreen = location[1];
                locationYOfFullScreen -= mStatusBarHeight;
                imageView.setTranslationY(locationYOfFullScreen);
                imageView.getLayoutParams().width = originRef.getWidth();
                imageView.getLayoutParams().height = originRef.getHeight();

                ViewState.write(imageView, ViewState.STATE_ORIGIN).width(originRef.getWidth()).height(originRef.getHeight());

                Drawable bmpMirror = originRef.getDrawable();
                if (bmpMirror != null) {
                    int bmpMirrorWidth = bmpMirror.getBounds().width();
                    int bmpMirrorHeight = bmpMirror.getBounds().height();
                    ViewState vsThumb = ViewState.write(imageView, ViewState.STATE_THUMB).width(bmpMirrorWidth).height(bmpMirrorHeight)
                            .translationX((mWidth - bmpMirrorWidth) / 2).translationY((mHeight - bmpMirrorHeight) / 2);
                    imageView.setImageDrawable(bmpMirror);

                    if (isFindEnterImagePicture) {
                        animSourceViewStateTransform(imageView, vsThumb);
                    } else {
                        ViewState.restore(imageView, vsThumb.mTag);
                    }
                }
            }

            final boolean isPlayEnterAnimation = isFindEnterImagePicture;
            // loadHighDefinitionPicture
            ViewState.clear(imageView, ViewState.STATE_DEFAULT);
            Glide.with(imageView.getContext()).load(mUrlList.get(pos)).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    final int sourceDefaultWidth, sourceDefaultHeight, sourceDefaultTranslateX, sourceDefaultTranslateY;
                    int resourceImageWidth = resource.getWidth();
                    int resourceImageHeight = resource.getHeight();
                    if (resourceImageWidth * 1f / resourceImageHeight > mWidth * 1f / mHeight) {
                        sourceDefaultWidth = mWidth;
                        sourceDefaultHeight = (int) (sourceDefaultWidth * 1f / resourceImageWidth * resourceImageHeight);
                        sourceDefaultTranslateX = 0;
                        sourceDefaultTranslateY = (mHeight - sourceDefaultHeight) / 2;
                        imageView.setTag(R.id.image_orientation, "horizontal");
                    } else {
                        sourceDefaultHeight = mHeight;
                        sourceDefaultWidth = (int) (sourceDefaultHeight * 1f / resourceImageHeight * resourceImageWidth);
                        sourceDefaultTranslateY = 0;
                        sourceDefaultTranslateX = (mWidth - sourceDefaultWidth) / 2;
                        imageView.setTag(R.id.image_orientation, "vertical");
                    }
                    imageView.setImageBitmap(resource);
                    notifyItemChangedState(pos, false, false);

                    ViewState vsDefault = ViewState.write(imageView, ViewState.STATE_DEFAULT).width(sourceDefaultWidth).height(sourceDefaultHeight)
                            .translationX(sourceDefaultTranslateX).translationY(sourceDefaultTranslateY);
                    if (isPlayEnterAnimation) {
                        animSourceViewStateTransform(imageView, vsDefault);
                    } else {
                        ViewState.restore(imageView, vsDefault.mTag);
                        imageView.setAlpha(0f);
                        imageView.animate().alpha(1).start();
                    }
                }

                @Override
                public void onLoadStarted(Drawable placeholder) {
                    notifyItemChangedState(pos, true, false);
                }

                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    notifyItemChangedState(pos, false, imageView.getDrawable() == null);
                }
            });

            if (isPlayEnterAnimation) {
                iOrigin.setVisibility(View.INVISIBLE);
                animBackgroundTransform(0xFF000000);
            }
            return isPlayEnterAnimation;
        }
    }

    private static class GestureHandler extends Handler {
        WeakReference<ImageWatcher> mRef;

        GestureHandler(ImageWatcher ref) {
            mRef = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mRef.get() != null) {
                ImageWatcher holder = mRef.get();
                switch (msg.what) {
                    case SINGLE_TAP_UP_CONFIRMED:
                        holder.onSingleTapConfirmed();
                        break;
                    default:
                        throw new RuntimeException("Unknown message " + msg); //never
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mPagerPositionOffsetPixels == 0;
    }

    /**
     * Animation is executed after joining the listener will automatically record label {@link ImageWatcher#isInTransformAnimation} state
     * IsInTransformAnimation when value is true, can achieve the purpose of the animation screen touch during execution
     */
    final AnimatorListenerAdapter mAnimTransitionStateListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationCancel(Animator animation) {
            isInTransformAnimation = false;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            isInTransformAnimation = true;
            mTouchMode = TOUCH_MODE_AUTO_FLING;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            isInTransformAnimation = false;
        }
    };

    final TypeEvaluator<Integer> mColorEvaluator = new TypeEvaluator<Integer>() {
        @Override
        public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
            int startColor = startValue;
            int endColor = endValue;

            int alpha = (int) (Color.alpha(startColor) + fraction * (Color.alpha(endColor) - Color.alpha(startColor)));
            int red = (int) (Color.red(startColor) + fraction * (Color.red(endColor) - Color.red(startColor)));
            int green = (int) (Color.green(startColor) + fraction * (Color.green(endColor) - Color.green(startColor)));
            int blue = (int) (Color.blue(startColor) + fraction * (Color.blue(endColor) - Color.blue(startColor)));
            return Color.argb(alpha, red, green, blue);
        }
    };

    public void setTranslucentStatus(int statusBarHeight) {
        mStatusBarHeight = statusBarHeight;
        tCurrentIdx.setTranslationY(tCurrentIdxTransY - statusBarHeight);
    }

    public void setErrorImageRes(int resErrorImage) {
        mErrorImageRes = resErrorImage;
    }

    private void refreshCurrentIdx(int position) {
        if (mUrlList.size() > 1) {
            tCurrentIdx.setVisibility(View.VISIBLE);
            tCurrentIdx.setText((position + 1) + " / " + mUrlList.size());
        } else {
            tCurrentIdx.setVisibility(View.GONE);
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        super.setBackgroundColor(color);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        maxTranslateX = mWidth / 2;
        maxTranslateY = mHeight / 2;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animImageTransform != null) animImageTransform.cancel();
        animImageTransform = null;
        if (animBackground != null) animBackground.cancel();
        animBackground = null;
    }

    /**
     * When interface in the image for state need in the Activity of {@link Activity#onBackPressed()}
     * Pass the event to ImageWatcher priority
     * 1, when in ending animation execution status, consumer return key events
     * 2, when images are in a state of amplification, perform image scaling to the size of the original animation, consumer return key events
     * 3, when the image in a state of nature, exit to view images, consumer return key events
     * 4, the other situation, ImageWatcher didn't show pictures
     */
    public boolean handleBackPressed() {
        return isInTransformAnimation || (iSource != null && getVisibility() == View.VISIBLE && onSingleTapConfirmed());
    }

    /**
     * Specifies the ImageView morphology (size, scaling, rotation, translation, transparency) gradually into expectations
     */
    private void animSourceViewStateTransform(ImageView view, final ViewState vsResult) {
        if (view == null) return;
        if (animImageTransform != null) animImageTransform.cancel();

        animImageTransform = ViewState.restoreByAnim(view, vsResult.mTag).addListener(mAnimTransitionStateListener).create();

        if (animImageTransform != null) {
            if (vsResult.mTag == ViewState.STATE_ORIGIN) {
                animImageTransform.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // If it's out of view operation, animation, after the execution, the original was visible click ImageView recovery
                        if (iOrigin != null) iOrigin.setVisibility(View.VISIBLE);
                        setVisibility(View.GONE);
                    }
                });
            }
            animImageTransform.start();
        }
    }

    /**
     * Perform ImageWatcher own background color gradient to expectations [colorResult] animation
     */
    private void animBackgroundTransform(final int colorResult) {
        if (colorResult == mBackgroundColor) return;
        if (animBackground != null) animBackground.cancel();
        final int mCurrentBackgroundColor = mBackgroundColor;
        animBackground = ValueAnimator.ofFloat(0, 1).setDuration(300);
        animBackground.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float p = (float) animation.getAnimatedValue();
                setBackgroundColor(mColorEvaluator.evaluate(p, mCurrentBackgroundColor, colorResult));
            }
        });
        animBackground.start();
    }

    /**
     * The current display pictures long press callback
     */
    public interface OnPictureLongPressListener {
        /**
         * @param v   The current ImageView is pressed
         * @param url The current ImageView load display image url address
         * @param pos The ImageView in showing the positions of the group
         */
        void onPictureLongPress(ImageView v, String url, int pos);
    }

    public void setOnPictureLongPressListener(OnPictureLongPressListener listener) {
        mPictureLongPressListener = listener;
    }

    public static class Helper {
        private static final int VIEW_IMAGE_WATCHER_ID = R.id.view_image_watcher;
        private final ViewGroup activityDecorView;
        private final ImageWatcher mImageWatcher;

        private Helper(Activity activity) {
            mImageWatcher = new ImageWatcher(activity);
            mImageWatcher.setId(VIEW_IMAGE_WATCHER_ID);
            activityDecorView = (ViewGroup) activity.getWindow().getDecorView();
        }

        public static Helper with(Activity activity) {
            return new Helper(activity);
        }

        public Helper setTranslucentStatus(int statusBarHeight) {
            mImageWatcher.mStatusBarHeight = statusBarHeight;
            return this;
        }

        public Helper setErrorImageRes(int resErrorImage) {
            mImageWatcher.mErrorImageRes = resErrorImage;
            return this;
        }

        public Helper setOnPictureLongPressListener(OnPictureLongPressListener listener) {
            mImageWatcher.setOnPictureLongPressListener(listener);
            return this;
        }

        public ImageWatcher create() {
            removeExistingOverlayInView(activityDecorView);
            activityDecorView.addView(mImageWatcher);
            return mImageWatcher;
        }

        void removeExistingOverlayInView(ViewGroup parent) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                if (child.getId() == VIEW_IMAGE_WATCHER_ID) {
                    parent.removeView(child);
                }
                if (child instanceof ViewGroup) {
                    removeExistingOverlayInView((ViewGroup) child);
                }
            }
        }
    }
}
