package connect.widget.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import connect.ui.activity.R;

/**
 * Created by Administrator on 2017/2/7.
 */

public class CricleProgressbar extends View{

    private Paint paint;
    private int mWidth;
    private int lineWidth = 8;
    private float sweepAngle = 0;

    public CricleProgressbar(Context context) {
        this(context,null);
    }

    public CricleProgressbar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CricleProgressbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.color_00c400));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(lineWidth);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF oval = new RectF(lineWidth/2 + lineWidth, lineWidth/2 + lineWidth,
                mWidth-lineWidth/2 - lineWidth,mWidth-lineWidth/2 - lineWidth);
        canvas.drawArc(oval,-90,sweepAngle,false,paint);
    }

    public void setEndAngle(float sweepAngle){
        this.sweepAngle = sweepAngle;
        postInvalidate();
    }

    public void setLineWidth(int lineWidth){
        this.lineWidth = lineWidth;
        paint.setStrokeWidth(lineWidth);
    }

}
