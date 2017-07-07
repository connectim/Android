package connect.ui.activity.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import connect.ui.activity.R;
import connect.ui.activity.chat.model.emoji.EmojiPanel;
import connect.ui.activity.chat.model.more.MorePanel;
import connect.utils.system.SystemUtil;

/**
 * At the bottom of the pop-up components
 * Created by gtq on 2016/11/24.
 */
public class ExBottomLayout extends RelativeLayout{

    private View moreView;
    private View emojiView;

    private MorePanel morePanel;
    private EmojiPanel emojiPanel;

    public ExBottomLayout(Context context) {
        super(context);
        initView();
    }

    public ExBottomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void initView() {
        Context context = getContext();
        moreView = View.inflate(context, R.layout.layout_more, null);
        emojiView = View.inflate(context, R.layout.layout_emoji, null);

        LinearLayout.LayoutParams moreLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SystemUtil.dipToPx(180));
        moreView.setLayoutParams(moreLayoutParams);
        morePanel = new MorePanel();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SystemUtil.dipToPx(180));
        emojiView.setLayoutParams(layoutParams);
        emojiPanel = new EmojiPanel();

        morePanel.setView(moreView);
        emojiPanel.init(emojiView);
        addView(moreView);
        addView(emojiView);
    }

    public void hideMoreView(){
        moreView.setVisibility(GONE);
    }

    public void switchMoreView() {
        if (moreView.getVisibility() == VISIBLE) {
            moreView.setVisibility(GONE);
        } else {
            moreView.setVisibility(VISIBLE);
        }
        emojiView.setVisibility(GONE);
    }

    public void switchEmojiView() {
        if (emojiView.getVisibility() == VISIBLE) {
            emojiView.setVisibility(GONE);
        } else {
            emojiView.setVisibility(VISIBLE);
        }
        moreView.setVisibility(GONE);
    }

    public void hideEmojiView(){
        emojiView.setVisibility(GONE);
    }

    public void hideExView(){
        hideEmojiView();
        hideMoreView();
    }

    public boolean exBottomShow() {
        return moreView.getVisibility() == VISIBLE || emojiView.getVisibility() == VISIBLE;
    }

    public EmojiPanel getEmojiPanel() {
        return emojiPanel;
    }

    public MorePanel getMorePanel() {
        return morePanel;
    }
}