package connect.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import connect.ui.activity.R;

public class MaterialBadgeTextView extends TextView {

    private static final int DEFAULT_FILL_TYPE = 0;

    private int backgroundColor;
    private int borderColor;
    private float borderWidth;
    private float borderAlpha;
    private int ctType;

    private static final float SHADOW_RADIUS = 3.5f;
    private static final int FILL_SHADOW_COLOR = 0x55000000;
    private static final int KEY_SHADOW_COLOR = 0x55000000;

    private static final float X_OFFSET = 0f;
    private static final float Y_OFFSET = 1.75f;


    private float density;
    private int mShadowRadius;
    private int shadowYOffset;
    private int shadowXOffset;

    private int basePadding;
    private int diffWH;

    private boolean isHighLightMode;

    public MaterialBadgeTextView(final Context context) {
        this(context, null);
    }

    public MaterialBadgeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialBadgeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setGravity(Gravity.CENTER);
        density = getContext().getResources().getDisplayMetrics().density;
        mShadowRadius = (int) (density * SHADOW_RADIUS);
        shadowYOffset = (int) (density * Y_OFFSET);
        shadowXOffset = (int) (density * X_OFFSET);
        basePadding = (mShadowRadius * 2);
        float textHeight = getTextSize();
        float textWidth = textHeight / 4;
        diffWH = (int) (Math.abs(textHeight - textWidth) / 2);
        int horizontalPadding = basePadding + diffWH;
        setPadding(horizontalPadding, basePadding, horizontalPadding, basePadding);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaterialBadgeTextView);
        backgroundColor = typedArray.getColor(R.styleable.MaterialBadgeTextView_android_background, Color.WHITE);
        borderColor = typedArray.getColor(R.styleable.MaterialBadgeTextView_mbtv_border_color, Color.TRANSPARENT);
        borderWidth = typedArray.getDimension(R.styleable.MaterialBadgeTextView_mbtv_border_width, 0);
        borderAlpha = typedArray.getFloat(R.styleable.MaterialBadgeTextView_mbtv_border_alpha, 1);
        ctType = typedArray.getInt(R.styleable.MaterialBadgeTextView_mbtv_type, DEFAULT_FILL_TYPE);
        typedArray.recycle();
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        /** Pure red dot mode if the text needs to be changed from nothing, to the size of the view.*/
        String strText = text==null?"":text.toString().trim();
        if(isHighLightMode && !"".equals(strText)){
            ViewGroup.LayoutParams lp = getLayoutParams();
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            setLayoutParams(lp);
            isHighLightMode = false;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        refreshBackgroundDrawable(w, h);
    }

    private void refreshBackgroundDrawable(int targetWidth, int targetHeight) {
        if (targetWidth <= 0 || targetHeight <= 0) {
            return;
        }
        CharSequence text = getText();
        if (text == null) {
            return;
        }
        if (text.length() == 1) {/**The first is a positive circle, when the text is a digit */
            int max = Math.max(targetWidth, targetHeight);
            ShapeDrawable circle;
            final int diameter = max - (2 * mShadowRadius);
            OvalShape oval = new OvalShadow(mShadowRadius, diameter);
            circle = new ShapeDrawable(oval);
            ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, circle.getPaint());
            circle.getPaint().setShadowLayer(mShadowRadius, shadowXOffset, shadowYOffset, KEY_SHADOW_COLOR);
            circle.getPaint().setColor(backgroundColor);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                setBackgroundDrawable(circle);
            } else {
                setBackground(circle);
            }
        } else if (text.length() > 1) {/**The second kind of background is the upper and lower sides of the straight line of the ellipse, when the text length is greater than 1 */
            SemiCircleRectDrawable sr = new SemiCircleRectDrawable();
            ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, sr.getPaint());
            sr.getPaint().setShadowLayer(mShadowRadius, shadowXOffset, shadowYOffset, KEY_SHADOW_COLOR);
            sr.getPaint().setColor(backgroundColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                setBackground(sr);
            } else {
                setBackgroundDrawable(sr);
            }
        } else {
            /** The third case is text=, which is the length of the text of 0, because there is no text, the current TextView background without any updates,
             * But sometimes we need to have a word with the solid small circle, used to express emphasis. This situation because of the need to reset the size of View, so it is not here, please use another method (setHighLightMode) to complete.
             */
        }

    }

    public void setBadgeCount(String count){
        setBadgeCount(count, false);
    }
    public void setBadgeCount(String count, boolean goneWhenZero) {
        int temp = -1;
        try {
            temp = Integer.parseInt(count);
        } catch (Exception e) {
e.printStackTrace();
        }
        if (temp != -1) {
            setBadgeCount(temp, goneWhenZero);
        }
    }

    public void setBadgeCount(int count){
        setBadgeCount(count, true);
    }
    public void setBadgeCount(int count, boolean goneWhenZero){
        if(count >0 && count <= 99){
            setText(String.valueOf(count));
            setVisibility(View.VISIBLE);
        }else if(count >99){
            setText("99+");
            setVisibility(View.VISIBLE);
        }else if(count <= 0){
            setText("0");
            if(goneWhenZero){
                setVisibility(View.GONE);
            }else{
                setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Digital display
     * @param notity The message disturb 0:close 1:open
     * @param count The total number of messages unread
     */
    public void setBadgeCount(int notity, int count) {
        if (0 == notity) {
            setBadgeCount(count);
        } else {
            if (count <= 0) {
                setVisibility(View.GONE);
            } else {
                setHighLightMode();
                setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Clearly show a red dot without any text,
     * Mainly by setting the text setText (") trigger onTextChanged (), and then trigger the chain onSizeChanged () finally updated the background
     */
    public void setHighLightMode(){
        setHighLightMode(false);
    }

    public void clearHighLightMode(){
        isHighLightMode = false;
        setBadgeCount(0);
    }

    /**
     *
     * @param isDisplayInToolbarMenu
     */
    public void setHighLightMode(boolean isDisplayInToolbarMenu){
        isHighLightMode = true;
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = params.width;
        if(isDisplayInToolbarMenu && params instanceof FrameLayout.LayoutParams){
            ((FrameLayout.LayoutParams)params).topMargin=dp2px(getContext(), 10);
            ((FrameLayout.LayoutParams)params).rightMargin=dp2px(getContext(), 10);
        }
        setLayoutParams(params);

        final int diameter = getWidth() - (int) (2.5 * (float) mShadowRadius);
        OvalShadow oval = new OvalShadow(mShadowRadius, diameter);
        ShapeDrawable drawable = new ShapeDrawable(oval);
        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, drawable.getPaint());
        drawable.getPaint().setColor(backgroundColor);
        drawable.getPaint().setAntiAlias(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
        setText("");
        setVisibility(View.VISIBLE);
    }

    public void setBackgroundColor(int color){
        backgroundColor = color;
        refreshBackgroundDrawable(getWidth(), getHeight());
    }

    private class OvalShadow extends OvalShape {
        private RadialGradient mRadialGradient;
        private Paint mShadowPaint;
        private int mCircleDiameter;

        public OvalShadow(int shadowRadius, int circleDiameter) {
            super();
            mShadowPaint = new Paint();
            mShadowRadius = shadowRadius;
            mCircleDiameter = circleDiameter;
            mRadialGradient = new RadialGradient(mCircleDiameter / 2, mCircleDiameter / 2,
                    mShadowRadius, new int[]{
                    FILL_SHADOW_COLOR, Color.TRANSPARENT
            }, null, Shader.TileMode.CLAMP);
            mShadowPaint.setShader(mRadialGradient);
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            final int viewWidth = MaterialBadgeTextView.this.getWidth();
            final int viewHeight = MaterialBadgeTextView.this.getHeight();
            canvas.drawCircle(viewWidth / 2, viewHeight / 2, (mCircleDiameter / 2 + mShadowRadius), mShadowPaint);
            canvas.drawCircle(viewWidth / 2, viewHeight / 2, (mCircleDiameter / 2), paint);
        }
    }

    class SemiCircleRectDrawable extends Drawable {
        private final Paint mPaint;
        private RectF rectF;

        public Paint getPaint() {
            return mPaint;
        }

        public SemiCircleRectDrawable() {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
        }

        @Override
        public void setBounds(int left, int top, int right, int bottom) {
            super.setBounds(left, top, right, bottom);
            if (rectF == null) {
                rectF = new RectF(left + diffWH, top + mShadowRadius+4, right - diffWH, bottom - mShadowRadius-4);
            } else {
                rectF.set(left + diffWH, top + mShadowRadius+4, right - diffWH, bottom - mShadowRadius-4);
            }
        }

        @Override
        public void draw(Canvas canvas) {
            float R = (float)(rectF.bottom * 0.4);
            if (rectF.right < rectF.bottom) {
                R = (float)(rectF.right * 0.4);
            }
            canvas.drawRoundRect(rectF, R, R, mPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            mPaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSPARENT;
        }
    }

    public static int dp2px(Context context, float dpValue) {
        try {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        } catch (Exception e) {
            return (int) (dpValue + 0.5f);
        }
    }
}

