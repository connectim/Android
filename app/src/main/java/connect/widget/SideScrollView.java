package connect.widget;

/**
 * Created by MJJ on 2015/7/25.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

import connect.ui.activity.R;
import connect.utils.log.LogManager;

public class SideScrollView extends HorizontalScrollView {

    private String Tag = "SideScrollView";
    private Boolean isOpen = false;
    private Boolean once = false;

    private View bottomLayout;
    private View contentLayout;
    private SideScrollListener sideScrollListener;

    public SideScrollView(Context context) {
        this(context, null);
    }

    public SideScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SideScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOverScrollMode(OVER_SCROLL_NEVER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!once) {
            once = true;
            bottomLayout = findViewById(R.id.bottom_layout);
            contentLayout = findViewById(R.id.content_layout);
            /*ViewGroup.LayoutParams layoutParams = contentLayout.getLayoutParams();
            layoutParams.width = SystemUtil.getScreenWidth();
            contentLayout.setLayoutParams(layoutParams);*/
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        LogManager.getLogger().d(Tag, "onLayout start:");
        if (changed) {
            LogManager.getLogger().d(Tag, "onLayout start: changed");
            this.scrollTo(0, 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                sideScrollListener.onDownOrMove(this);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                changeScrollx();
                return true;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        bottomLayout.setTranslationX(l - bottomLayout.getWidth());
    }

    public void changeScrollx() {
        if (getScrollX() >= (bottomLayout.getWidth() / 2)) {
            this.smoothScrollTo(bottomLayout.getWidth(), 0);
            isOpen = true;
            sideScrollListener.onMenuIsOpen(this);
        } else {
            this.smoothScrollTo(0, 0);
            isOpen = false;
        }
    }

    public void openMenu() {
        if (isOpen) {
            return;
        }
        this.smoothScrollTo(bottomLayout.getWidth(), 0);
        isOpen = true;
        sideScrollListener.onMenuIsOpen(this);
    }

    public boolean isOpen(){
        return isOpen;
    }

    public void closeMenu() {
        if (!isOpen) {
            return;
        }
        this.smoothScrollTo(0, 0);
        isOpen = false;
    }

    public void setSideScrollListener(SideScrollListener listener) {
        sideScrollListener = listener;
    }

    public interface SideScrollListener {
        void onMenuIsOpen(View view);

        void onDownOrMove(SideScrollView sideScrollView);
    }
}
