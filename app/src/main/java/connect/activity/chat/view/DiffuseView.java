package connect.activity.chat.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import connect.ui.activity.R;
import connect.utils.log.LogManager;
import connect.utils.system.SystemUtil;

/**
 * Created by gtq on 2016/12/15.
 */
public class DiffuseView extends ImageView {

    private String Tag = "DiffuseView";

    private Paint paint;
    private int mDiffuseColor = getResources().getColor(R.color.color_green);
    private int imageId = R.mipmap.chatbar_recording;

    private float widthFloat = 1f;
    private ValueAnimator animator = null;

    private int locationX;
    private int locationY;

    public DiffuseView(Context context) {
        this(context, null);
        initView();
    }

    public DiffuseView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
        initView();
    }

    public DiffuseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DiffuseView, defStyleAttr, 0);
        mDiffuseColor = a.getColor(R.styleable.DiffuseView_diffuse_color, mDiffuseColor);
        imageId = a.getResourceId(R.styleable.DiffuseView_diffuse_image, -1);
        a.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(mDiffuseColor);

        initView();
    }

    protected void initView(){
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(mDiffuseColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageId);
        if (bitmap != null) {
            canvas.drawCircle(locationX, locationY - bitmap.getHeight() / 2, SystemUtil.dipToPx(40) * widthFloat, paint);
            canvas.drawBitmap(bitmap, locationX - bitmap.getWidth() / 2, locationY - bitmap.getHeight(), paint);
        }
    }

    public void startDiffuse(int width) {
        LogManager.getLogger().d(Tag,"startDiffuse ===="+width);
        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        animator = ValueAnimator.ofFloat(widthFloat, width);
        animator.setDuration(320);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                LogManager.getLogger().d(Tag, "onAnimationUpdate ====" + value);
                widthFloat = value;
                invalidate();
            }
        });
        animator.start();
    }

    public void stopDiffuse() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        widthFloat = 1;
        invalidate();
    }

    public void setDiffuseState(int locx, int color) {
        this.locationX = locx;
        paint.setColor(color);
        invalidate();
    }

    public void setLocationY(int locy) {
        this.locationY = locy;
    }
}
