package connect.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * width equals height LinearLayout
 * Created by Administrator on 2016/8/27.
 */
public class HightEqWidthImage extends ImageView {

    public HightEqWidthImage(Context context) {
        super(context);
    }

    public HightEqWidthImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HightEqWidthImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
