package connect.widget.bottominput.bean;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import connect.ui.activity.R;
import connect.widget.bottominput.EmoManager;
import connect.utils.system.SystemUtil;
import connect.utils.glide.GlideUtil;

/**
 * Pictures selected species at the bottom of the expression
 * Created by gtq on 2016/11/25.
 */
public class BottomCateView extends FrameLayout {

    private String cateName;
    private ImageView img;

    public BottomCateView(Context context, String name) {
        super(context);
        this.cateName = name;

        img = new ImageView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(SystemUtil.dipToPx(30), SystemUtil.dipToPx(30));
        params.gravity = Gravity.CENTER;
        img.setLayoutParams(params);
        addView(img);

        setNormal();
    }

    public void setNormal() {
        setBackgroundColor(getResources().getColor(R.color.color_F5F5F5));
        GlideUtil.loadImageAssets(img, EmoManager.EMOJI_PATH + "/" + cateName + "_normal.png");
    }

    public void setPress() {
        setBackgroundColor(getResources().getColor(R.color.color_e7e7e7));
        GlideUtil.loadImageAssets(img, EmoManager.EMOJI_PATH + "/" + cateName + "_press.png");
    }
}