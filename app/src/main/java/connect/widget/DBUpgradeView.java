package connect.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import connect.ui.activity.R;
import connect.utils.system.SystemUtil;

public class DBUpgradeView extends View {

    private String Tag = "DBUpgradeView";

    private int progress;
    private int ROUND_DIRECT = -360;
    private int padding;

    private boolean drawFinish = false;
    private Paint mPaint = new Paint();
    private Paint backPaint = new Paint();
    private RectF rectf;

    public DBUpgradeView(Context context) {
        super(context);
        initView();
    }

    public DBUpgradeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DBUpgradeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    protected void initView() {
        padding = SystemUtil.dipToPx(5);

        mPaint.setColor(getContext().getResources().getColor(R.color.color_00c400));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(SystemUtil.pxToSp(5));

        backPaint.setColor(getContext().getResources().getColor(R.color.color_b3b5bc));
        backPaint.setStyle(Paint.Style.STROKE);
        backPaint.setAntiAlias(true);
        backPaint.setStrokeWidth(SystemUtil.pxToSp(5));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackCircle(canvas);
        drawProgressCircle(canvas);
            drawProgress(canvas);
    }

    private float curProgress = 0;

    public void drawProgress(Canvas canvas) {
        if (progress < 100) {
//            int fontsize = SystemUtil.dipToPx(16);
//            int startX = getWidth()/2 - Math.abs(progress) < 10 ? fontsize / 2 : fontsize;
//            canvas.drawText(String.valueOf(progress), startX, getHeight() / 2 - fontsize / 2, mPaint);
        } else {
            Path path = new Path();
            path.moveTo(getWidth() * 3 / 10-10, getWidth() * 4 / 10);
            float[] firstLine = calculateFirstLine(curProgress);
            path.lineTo(firstLine[0], firstLine[1]);

            if (curProgress > 50) {
                float[] secondLine = calculateSecondLine(curProgress);
                path.lineTo(secondLine[0], secondLine[1]);
            }
            canvas.drawPath(path, mPaint);
        }
    }

    public float[] calculateFirstLine(float progress) {
        float[] point = new float[2];
        float percent = progress / 50;
        if (percent >= 1) {
            percent = 1;
        }
        point[0] = getWidth() * 3 / 10-10 + (getWidth() * 2 / 10) * percent;
        point[1] = getWidth() * 4 / 10 + (getWidth() * 2 / 10) * percent;
        return point;
    }

    public float[] calculateSecondLine(float progress) {
        progress -= 50;
        float[] point = {0, 0};
        float percent = progress / 50;
        if (percent > 0) {
            point[0] = getWidth() * 5 / 10-10 + (getWidth() * 3 / 10) * percent;
            point[1] = getWidth() * 6 / 10 - (getWidth() * 3/ 10) * percent;
        }
        return point;
    }

    protected void drawProgressCircle(Canvas canvas) {
        rectf = new RectF(padding, padding, getWidth() - padding, getHeight() - padding);
        canvas.drawArc(rectf, -90, -progress * ROUND_DIRECT / 100, false, mPaint);
    }

    protected void drawBackCircle(Canvas canvas) {
        rectf = new RectF(padding, padding, getWidth() - padding, getHeight() - padding);
        canvas.drawArc(rectf, -90, 360, false, backPaint);
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();

        if (progress == 100) {
            ValueAnimator animator = ValueAnimator.ofFloat(0, 100);
            animator.setDuration(200).start();
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    curProgress = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
        }
    }
}
