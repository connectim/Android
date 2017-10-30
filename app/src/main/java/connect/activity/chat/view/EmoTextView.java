package connect.activity.chat.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import connect.activity.chat.model.ChatMsgUtil;

/**
 * Created by gtq on 2016/12/10.
 */
public class EmoTextView extends TextView {

    public EmoTextView(Context context) {
        super(context);
    }

    public EmoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!TextUtils.isEmpty(text)) {
            text = ChatMsgUtil.txtTransEmotion(getContext(), text.toString());
        }
        super.setText(text, type);
    }
}
