package connect.activity.chat.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import connect.ui.activity.R;
import connect.utils.system.SystemUtil;

/**
 * video download
 * Created by pujin on 2017/1/24.
 */
public class DVideoProView extends View {

    private boolean downFinish;
    private int progress;

    private int ROUND_DIRECT = -360;
    private Paint mPaint = new Paint();
    private RectF rectf;

    public DVideoProView(Context context) {
        super(context);
        initView();
    }

    public DVideoProView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DVideoProView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    protected void initView() {
        mPaint.setColor(getContext().getResources().getColor(R.color.color_white));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(SystemUtil.pxToSp(2));
    }

    /**
     * download state
     *
     * @param downfinish true:downlaod finish
     * @param progress
     */
    public void loadState(boolean downfinish, int progress) {
        this.downFinish = downfinish;
        this.progress = progress;
        invalidate();
    }

    protected void drawCircle(Canvas canvas) {
        rectf = new RectF(2, 2, getWidth() - 2, getHeight() - 2);
        if (!downFinish) {
            canvas.drawArc(rectf, -90, -progress * ROUND_DIRECT / 100, false, mPaint);
        } else {
            canvas.drawArc(rectf, -90, 360, false, mPaint);
        }
    }

    protected void drawImg(Canvas canvas) {
        int resid = 0;
        if (downFinish) {
            resid = R.mipmap.message_video_play2x;
        } else {
            if (progress == 0) {
                resid = R.mipmap.message_download2x;
            } else {
                resid = R.mipmap.message_download_cancel2x;
            }
        }
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resid);
        RectF rectF = new RectF(0, 0, getWidth(), getHeight());
        canvas.drawBitmap(bitmap, null, rectF, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawImg(canvas);
        drawCircle(canvas);
    }
}
