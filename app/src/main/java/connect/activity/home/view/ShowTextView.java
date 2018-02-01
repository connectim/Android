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


    public void showText(int at,int attemtion, String drat, String content) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();

        if (at >= 1) {
            String string = getContext().getString(R.string.Chat_Someone_note_me);
            SpannableStringBuilder builder = new SpannableStringBuilder(string);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
            builder.setSpan(colorSpan, 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.append(builder);
        }

        if (attemtion >= 1) {
            String string = getContext().getString(R.string.Chat_Tip_Attention);
            SpannableStringBuilder builder = new SpannableStringBuilder(string);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
            builder.setSpan(colorSpan, 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.append(builder);
        }

        if (!TextUtils.isEmpty(drat)) {
            String string = getContext().getString(R.string.Chat_Draft);
            SpannableStringBuilder builder = new SpannableStringBuilder(string);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
            builder.setSpan(colorSpan, 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.append(builder);
            stringBuilder.append(apostropheString(drat));
        } else {
            SpannableStringBuilder builder = apostropheString(content);
            stringBuilder.append(builder);
        }
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
