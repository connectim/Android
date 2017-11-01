package connect.activity.home.view;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.utils.log.LogManager;
import connect.widget.bottominput.EmoManager;

/**
 * message direction
 * Created by gtq on 2016/11/22.
 */
public class ShowTextView extends TextView {

    private String Tag = "ShowTextView";
    private final int limitLength = 40;

    public ShowTextView(Context context) {
        super(context);
    }

    public ShowTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShowTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * @param drat
     * @param content
     */
    public void showText(int at, String drat, String content) {
        SpannableStringBuilder stringBuilder = null;

        if (at == 1) {
            stringBuilder = new SpannableStringBuilder();
            String string = getContext().getString(R.string.Chat_Someone_note_me);
            SpannableStringBuilder builder = new SpannableStringBuilder(string);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
            builder.setSpan(colorSpan, 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.append(builder);
            stringBuilder.append(apostropheString(content));
        } else if (!TextUtils.isEmpty(drat)) {
            stringBuilder = new SpannableStringBuilder();
            String string = getContext().getString(R.string.Chat_Draft);
            SpannableStringBuilder builder = new SpannableStringBuilder(string);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
            builder.setSpan(colorSpan, 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.append(builder);
            stringBuilder.append(apostropheString(drat));
        } else {
            stringBuilder = apostropheString(content);
        }

        LogManager.getLogger().d(Tag, stringBuilder.toString());
        setText(stringBuilder);
    }

    /**
     * text to expression
     * @param content
     * @return
     */
    private SpannableStringBuilder apostropheString(String content) {
        boolean largeLen = content.length() > limitLength;
        if (largeLen) {
            content = content.substring(0, limitLength);
        }
        SpannableStringBuilder string = EmoManager.emojiManager.txtTransEmotion(content);
        if (largeLen) {
            string = string.append("...");
        }
        return string;
    }
}
