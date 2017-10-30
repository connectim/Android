package connect.widget.bottominput;

import android.app.Activity;
import connect.activity.chat.view.RecordView;
import connect.widget.bottominput.view.ExBottomLayout;
import connect.widget.bottominput.view.InputBottomLayout;

/**
 * Created by gtq on 2016/11/26.
 */
public class InputPanel {

    private static String TAG = "_InputPanel";
    public static String EMOJI_DELETE = "[DEL]";

    private Activity activity;
    private RecordView recordView;
    private InputBottomLayout inputBottomLayout;
    private ExBottomLayout exBottomLayout;

    public static InputPanel inputPanel = getInstance();

    private static InputPanel getInstance() {
        if (inputPanel == null) {
            inputPanel = new InputPanel();
        }
        return inputPanel;
    }

    public InputPanel() {
        inputBottomLayout = InputBottomLayout.bottomLayout;
        exBottomLayout = ExBottomLayout.exBottomLayout;
    }

    public void switchMoreView() {
        inputBottomLayout.hideInputBottomEditeText();
        exBottomLayout.switchMoreView();
    }

    public void switchEmojView() {
        inputBottomLayout.hideInputBottomEditeText();
        exBottomLayout.switchEmojiView();
    }

    public void hideBottomPanel() {
        inputBottomLayout.hideInputBottomEditeText();
        exBottomLayout.hideExView();
    }

    public void setRecordView(RecordView recordView) {
        this.recordView = recordView;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public RecordView getRecordView() {
        return recordView;
    }

    public Activity getActivity() {
        return activity;
    }
}