package connect.view.lockview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import connect.ui.activity.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/3.
 */
public class GestureTopView extends View {

    private List<Integer> mChoose = new ArrayList<>();
    private Paint paintRing;
    private Paint paintCircle;
    /** By default the radius of the circle is always high 1/10 */
    private int cricleRadius;

    public GestureTopView(Context context) {
        super(context);
        initView();
    }

    public GestureTopView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureTopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.GestureLockViewGroup, defStyleAttr, 0);
        initView();
    }

    private void initView() {
        paintRing = new Paint();
        paintRing.setStyle(Paint.Style.STROKE);
        paintRing.setStrokeWidth(2);
        paintRing.setAntiAlias(true);
        paintRing.setColor(getResources().getColor(R.color.color_858998));

        paintCircle = new Paint();
        paintCircle.setStyle(Paint.Style.FILL);
        paintCircle.setAntiAlias(true);
        paintCircle.setColor(getResources().getColor(R.color.color_007aff));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int mHeight = MeasureSpec.getSize(heightMeasureSpec);
        cricleRadius = mHeight / 10;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 1; i < 10; i++) {
            int[] xyArray = getXY(i);
            if (mChoose.size() > 0 && mChoose.contains(i)) {
                canvas.drawCircle(xyArray[0], xyArray[1], cricleRadius, paintCircle);
            } else {
                canvas.drawCircle(xyArray[0], xyArray[1], cricleRadius, paintRing);
            }
        }
    }

    /**
     * According to the index by dot coordinates
     * @param index
     */
    private int[] getXY(int index) {
        index = index - 1;
        int line = index / 3;
        int raw = index % 3;
        int x = raw * 3 * cricleRadius + 2 * cricleRadius;
        int y = line * 3 * cricleRadius + 2 * cricleRadius;
        int[] intArray = new int[]{x, y};
        return intArray;
    }

    public void setChooseData(List<Integer> mChoose) {
        if (mChoose != null) {
            this.mChoose.clear();
            this.mChoose.addAll(mChoose);
        } else {
            this.mChoose.clear();
        }
        invalidate();
    }

}
