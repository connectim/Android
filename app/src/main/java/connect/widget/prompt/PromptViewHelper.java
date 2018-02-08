package connect.widget.prompt;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.PopupWindow;

import connect.utils.system.SystemUtil;

public class PromptViewHelper {

    private PromptViewManager promptViewManager;
    private Context context;
    private PopupWindow popupWindow;
    private boolean isShow;
    private OnPromptClickListener onItemClickListener;

    public PromptViewHelper(Context activity) {
        this.context = activity;
    }

    public void setPromptViewManager(PromptViewManager promptViewManager) {
        this.promptViewManager = promptViewManager;
        this.promptViewManager.setOnItemClickListener(new OnPromptClickListener() {
            @Override
            public void onPromptClick(String string) {
                if (onItemClickListener != null && popupWindow != null) {
                    onItemClickListener.onPromptClick(string);
                    popupWindow.dismiss();
                }
            }
        });
    }

    public void addPrompt(View srcView) {
        srcView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                createPrompt(v);
                return true;
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void createPrompt(final View srcView) {
        final View promptView = promptViewManager.getPromptView();
        if (popupWindow == null)
            popupWindow = new PopupWindow(context);
        popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setContentView(promptView);
        final int[] location = new int[2];
        promptView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!isShow && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    show(srcView, promptView, location);
                    isShow = true;
                }
            }
        });

        srcView.getLocationOnScreen(location);
        show(srcView, promptView, location);
    }

    public void show(View srcView, View promptView, int[] srcViewLocation) {
        int[] location = new int[2];
        int offset = (promptView.getWidth() - srcView.getWidth()) / 2;
        location[0] = srcViewLocation[0] - offset;
        location[1] = srcViewLocation[1] - promptView.getHeight();

        int stateBarHeight = SystemUtil.getStateBarHeight();
        int titleBarHeight = SystemUtil.dipToPx(45);
        if (location[1] < stateBarHeight + titleBarHeight) {
            location[1] = stateBarHeight + titleBarHeight;
        }

        popupWindow.showAtLocation(srcView, Gravity.NO_GRAVITY, location[0], location[1]);
    }

    public void dissmissPopupwindow() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    public static abstract class PromptViewManager {

        private View promptView;
        protected Context activity;
        private String[] dataArray;
        public OnPromptClickListener onItemClickListener;

        public PromptViewManager(Context activity, String[] dataArray) {
            this.activity = activity;
            this.dataArray = dataArray;
            init();
        }

        public void setOnItemClickListener(OnPromptClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public void init() {
            promptView = inflateView();
            bindData(promptView, dataArray);
        }

        public abstract View inflateView();

        public abstract void bindData(View view, String[] dataArray);

        public View getPromptView() {
            return promptView;
        }
    }

    public void setOnItemClickListener(OnPromptClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnPromptClickListener {
        void onPromptClick(String string);
    }
}
