package connect.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * width equals height LinearLayout
 */
public class HeightEqWidthImage extends ImageView {

    public HeightEqWidthImage(Context context) {
        super(context);
    }

    public HeightEqWidthImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeightEqWidthImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
