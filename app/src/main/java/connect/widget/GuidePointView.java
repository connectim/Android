package connect.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import connect.ui.activity.R;

/**
 * Boot dot
 */
public class GuidePointView extends LinearLayout {

    private View point1;
    private View point2;
    private View point3;

    public GuidePointView(Context context) {
        this(context,null);
    }

    public GuidePointView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GuidePointView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.item_guide_point, this, true);
        initView(view);
    }

    private void initView(View view) {
        point1 = view.findViewById(R.id.point_1);
        point2 = view.findViewById(R.id.point_2);
        point3 = view.findViewById(R.id.point_3);
    }

    public void setSelePoint(int index){
        if(index > 3)
            return;
        switch (index){
            case 0:
                point1.setBackgroundResource(R.drawable.shape_guide_point_black);
                point2.setBackgroundResource(R.drawable.shape_guide_point_white);
                point3.setBackgroundResource(R.drawable.shape_guide_point_white);
                break;
            case 1:
                point1.setBackgroundResource(R.drawable.shape_guide_point_white);
                point2.setBackgroundResource(R.drawable.shape_guide_point_black);
                point3.setBackgroundResource(R.drawable.shape_guide_point_white);
                break;
            case 2:
                point1.setBackgroundResource(R.drawable.shape_guide_point_white);
                point2.setBackgroundResource(R.drawable.shape_guide_point_white);
                point3.setBackgroundResource(R.drawable.shape_guide_point_black);
                break;
            default:
                break;
        }
    }

}
