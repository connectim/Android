package connect.ui.activity.chat.view;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import connect.ui.activity.R;
import connect.ui.activity.chat.model.EmoManager;
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

    public void selectState(boolean state) {
        if (state) {
            setPress();
        } else {
            setNormal();
        }
    }

    public void setNormal() {
        setBackgroundColor(getResources().getColor(R.color.color_cdd0d4));
        GlideUtil.loadImageAssets(img, EmoManager.EMOJI_PATH + "/" + cateName + "_normal.png");
    }

    public void setPress() {
        setBackgroundColor(getResources().getColor(R.color.color_ff6c5a));
        GlideUtil.loadImageAssets(img, EmoManager.EMOJI_PATH + "/" + cateName + "_press.png");
    }
}