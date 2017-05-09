package connect.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.ui.base.BaseApplication;
import connect.utils.system.SystemUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by gtq on 2016/12/13.
 */
public class SideBar extends View {
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;

    private String[] source = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
            "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
    public List<String> b = Arrays.asList(source);

    private int choose = -1;
    private Paint paint = new Paint();
    private Context ctx;

    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SideBar(Context context) {
        super(context);
        initView(context);
    }

    public void initView(Context context) {
        this.ctx = context;
        initLetterDialog();
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();

        int singleHeight = height / b.size();
        for (int i = 0; i < b.size(); i++) {
            paint.setColor(BaseApplication.getInstance().getAppContext().getResources().getColor(R.color.color_cdd0d4));
            //paint.setColor(Color.rgb(33, 65, 98));
            paint.setAntiAlias(true);
            paint.setTextSize(SystemUtil.pxToSp(12));
            paint.setFakeBoldText(false);
            if (i == choose) {
                paint.setColor(Color.parseColor("#3399ff"));
                paint.setFakeBoldText(true);
            }

            // The X coordinate is equal to half of the middle string width
            float xPos = width / 2 - paint.measureText(b.get(i)) / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(b.get(i), xPos, yPos, paint);
            paint.reset();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;

        final int c = (int) (y / getHeight() * b.size());
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setBackgroundDrawable(new ColorDrawable(0x00000000));
                choose = -1;
                invalidate();
                if (letterDialog != null) {
                    letterDialog.dismiss();
                }
                break;
            default:
                setBackgroundResource(R.drawable.pr_sidebar_background);
                if (oldChoose != c) {
                    if (c >= 0 && c < b.size()) {
                        if (listener != null) {
                            listener.onTouchingLetterChanged(b.get(c));
                        }

                        if (letterDialog != null) {
                            dialogText.setText(b.get(c));
                            letterDialog.show();
                        }
                        choose = c;
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }

    /**
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(
            OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    public void setChoose(int choose) {
        this.choose = choose;
    }

    /**
     * @author coder
     */
    public interface OnTouchingLetterChangedListener {
        void onTouchingLetterChanged(String s);
    }

    public List<String> getB() {
        return b;
    }

    public void setB(List<String> b) {
        //# In the end
        if (b.contains("#") && b.size() > 1) {
            int index = b.indexOf("#");

            String last = b.get(b.size() - 1);
            b.set(index, last);
            b.set(b.size() - 1, "#");
        }
    }

    private Dialog letterDialog;
    private View dialogView;
    private TextView dialogText;

    private void initLetterDialog() {
        letterDialog = new Dialog(getContext(), R.style.toast_dialog_style);
        dialogView = inflate(getContext(), R.layout.dialog_sidebar, null);
        dialogText = (TextView) dialogView.findViewById(R.id.txt);
        letterDialog.setContentView(dialogView, new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        WindowManager.LayoutParams lp = letterDialog.getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
    }
}
