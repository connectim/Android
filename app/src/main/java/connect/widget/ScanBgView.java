package connect.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import connect.ui.activity.R;

public class ScanBgView extends View{

    private Paint paint;

    public ScanBgView(Context context) {
        this(context,null);
    }

    public ScanBgView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ScanBgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint(){
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.color_161A21));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0,0,getWidth(),getHeight(),paint);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        paint.setColor(getResources().getColor(R.color.color_00ffbf));
        RectF rect = new RectF();
        rect.left = 0;
        rect.right = getWidth();
        rect.top = 0 ;
        rect.bottom = getHeight();
        canvas.drawRoundRect(rect,20,20,paint);
    }
}
