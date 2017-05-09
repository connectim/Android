package connect.view;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.ui.base.BaseApplication;

/**
 * Created by john on 2016/11/22.
 */
public class TopToolBar extends LinearLayout{

    private final Context context;
    private RelativeLayout leftRela;
    private ImageView leftImg;
    private ImageView titleImg;
    private TextView titleTv;
    private ImageView rightImg;
    private TextView rightText;
    private LinearLayout rightLayout;

    public TopToolBar(Context context) {
        this(context,null);
    }

    public TopToolBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.toolbar_head, this, true);
        initView(view);
    }

    private void initView(View view) {
        leftRela = (RelativeLayout) view.findViewById(R.id.left_rela);
        leftImg = (ImageView) view.findViewById(R.id.left_img);
        titleImg = (ImageView) view.findViewById(R.id.title_img);
        titleTv = (TextView) view.findViewById(R.id.title_tv);
        rightImg = (ImageView) view.findViewById(R.id.right_img);
        rightText = (TextView) view.findViewById(R.id.right_text);
        rightLayout = (LinearLayout) view.findViewById(R.id.right_lin);
    }

    public void setBlackStyle(){
        this.setBackgroundResource(R.color.color_161A21);
        titleTv.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_ffffff));
        rightText.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_ffffff));
        //SystemUtil.setWindowStatusBarColor((Activity)context,R.color.color_161A21);
    }

    public void setRedStyle(){
        this.setBackgroundResource(R.color.color_ff6c5a);
        titleTv.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_ffffff));
        rightText.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_ffffff));
        setWindowStatusBarColor((Activity)context,R.color.color_ff6c5a);
    }

    public void setBackground(int resid){
        this.setBackgroundResource(resid);
    }

    public void setLeftImg(Integer resId){
        leftImg.setImageResource(resId);
    }

    public void setLeftListence(OnClickListener onClickListener){
        leftRela.setOnClickListener(onClickListener);
    }

    public void setTitle(String title) {
        titleImg.setVisibility(GONE);
        titleTv.setText(title);
    }

    public void setTitleImg(Integer imgId) {
        if (imgId != null) {
            titleImg.setVisibility(VISIBLE);
            titleImg.setImageResource(imgId);
        }
    }

    public void setTitle(Integer imgId,Integer textId){
        if(imgId != null){
            titleImg.setVisibility(VISIBLE);
            titleImg.setImageResource(imgId);
        }
        if(textId != null){
            titleTv.setText(textId);
        }
    }

    public void setTitle(Integer imgId,String textId){
        if(imgId != null){
            titleImg.setVisibility(VISIBLE);
            titleImg.setImageResource(imgId);
        }
        if(textId != null){
            titleTv.setText(textId);
        }
    }

    public void setRightImg(Integer resId){
        if(resId == null){
            rightImg.setVisibility(View.GONE);
        }else{
            rightImg.setVisibility(View.VISIBLE);
            rightImg.setImageResource(resId);
        }
    }

    public void setRightText(Integer resId){
        rightText.setText(resId);
    }

    public void setRightText(String resId){
        rightText.setText(resId);
    }

    public void setRightTextEnable(boolean enable){
        rightText.setEnabled(enable);
        if(enable){
            rightText.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_00c400));
        }else{
            rightText.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_767a82));
        }
        rightLayout.setEnabled(enable);
    }

    public void setRightTextColor(Integer resId){
        rightText.setTextColor(BaseApplication.getInstance().getResources().getColor(resId));
    }

    public void setRightListence(OnClickListener onClickListener){
        rightLayout.setOnClickListener(onClickListener);
    }

    public void setWindowStatusBarColor(Activity activity, int colorResId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(activity.getResources().getColor(colorResId));
                //window.setNavigationBarColor(activity.getResources().getColor(colorResId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
