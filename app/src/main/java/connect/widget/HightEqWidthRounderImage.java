package connect.widget;

import android.content.Context;
import android.util.AttributeSet;

import connect.widget.roundedimageview.RoundedImageView;

/**
 * Created by Administrator on 2016/12/15.
 */
public class HightEqWidthRounderImage extends RoundedImageView {

    public HightEqWidthRounderImage(Context context) {
        super(context);
    }

    public HightEqWidthRounderImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HightEqWidthRounderImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
