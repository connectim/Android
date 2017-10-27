package connect.widget.bottominput.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.BaseAction;
import connect.activity.chat.bean.LinkMessageRow;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.bean.RoomSession;
import connect.activity.chat.inter.IEmojiClickListener;
import connect.activity.chat.view.ChatEditText;
import connect.ui.activity.R;
import connect.utils.log.LogManager;
import connect.utils.system.SystemUtil;
import connect.widget.bottominput.EmoManager;
import connect.widget.bottominput.InputPanel;
import connect.widget.bottominput.bean.BottomCateView;
import connect.widget.bottominput.bean.StickPagerBean;
import connect.widget.bottominput.bean.StickerCategory;
import connect.widget.bottominput.emoji.EmojiPagerAdapter;
import connect.widget.bottominput.more.MorePagerAdapter;
import protos.Connect;

/**
 * At the bottom of the pop-up components
 * Created by gtq on 2016/11/24.
 */
public class ExBottomLayout extends RelativeLayout {

    @Bind(R.id.more_pagerview)
    ViewPager morePagerview;
    @Bind(R.id.more_indicator)
    LinearLayout moreIndicator;
    @Bind(R.id.include_more)
    LinearLayout includeMore;
    @Bind(R.id.emoji_pagerview)
    ViewPager emojiPagerview;
    @Bind(R.id.emoji_pagernum)
    LinearLayout emojiPagernum;
    @Bind(R.id.emoji_tabview)
    LinearLayout emojiTabview;
    @Bind(R.id.include_emoji)
    LinearLayout includeEmoji;

    private static String TAG = "_ExBottomLayout";
    public static ExBottomLayout exBottomLayout = getInstance();

    private synchronized static ExBottomLayout getInstance() {
        if (exBottomLayout == null) {
            Context context = BaseApplication.getInstance().getBaseContext();
            exBottomLayout = new ExBottomLayout(context);
        }
        return exBottomLayout;
    }

    private List<StickPagerBean> stickPagerBeens = new ArrayList<>();
    private BottomClickListener bottomClickListener = new BottomClickListener();
    private EmojiPagerAdapterClickListener clickListener = new EmojiPagerAdapterClickListener();
    private EmojiPagerAdapter emojiPagerAdapter;

    public ExBottomLayout(Context context) {
        super(context);
    }

    public ExBottomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_bottom_chatex, this);

        loadCheckTab();
        emojiPagerAdapter.setiEmojiClickListener(clickListener);
    }

    public void hideMoreView() {
        includeMore.setVisibility(GONE);
    }

    public void switchMoreView() {
        if (includeMore.getVisibility() == VISIBLE) {
            includeMore.setVisibility(GONE);
        } else {
            includeMore.setVisibility(VISIBLE);
        }
        includeEmoji.setVisibility(GONE);
    }

    public void switchEmojiView() {
        if (includeEmoji.getVisibility() == VISIBLE) {
            includeEmoji.setVisibility(GONE);
        } else {
            includeEmoji.setVisibility(VISIBLE);
        }
        includeMore.setVisibility(GONE);
    }

    public void hideEmojiView() {
        includeMore.setVisibility(GONE);
    }

    public void hideExView() {
        hideEmojiView();
        hideMoreView();
    }

    public void init(View view) {
        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.more_pagerview);
        final ViewGroup indicator = (ViewGroup) view.findViewById(R.id.more_indicator);

        List<BaseAction> actionList = new ArrayList<>();

        Connect.ChatType roomType = RoomSession.getInstance().getRoomType();
        actionList.add(new BaseAction(R.mipmap.chat_bar_picture, R.string.Chat_Photo));
        actionList.add(new BaseAction(R.mipmap.chat_bar_camera, R.string.Chat_Sight));

        if (roomType == Connect.ChatType.CONNECT_SYSTEM) {

        } else {
            actionList.add(new BaseAction(R.mipmap.chat_bar_trasfer, R.string.Wallet_Transfer));
            actionList.add(new BaseAction(R.mipmap.chat_bar_redbag, R.string.Wallet_Packet));
            actionList.add(new BaseAction(R.mipmap.message_send_payment2x, R.string.Wallet_Receipt));
            if (roomType == Connect.ChatType.PRIVATE) {
                actionList.add(new BaseAction(R.mipmap.message_send_privacy_chat3x, R.string.Chat_Read_Burn));
            }
            actionList.add(new BaseAction(R.mipmap.chat_bar_contract, R.string.Chat_Name_Card));
        }
        actionList.add(new BaseAction(R.mipmap.message_send_location3x, R.string.Chat_Loc));

        MorePagerAdapter moreAdapter = new MorePagerAdapter(viewPager, actionList);
        viewPager.setAdapter(moreAdapter);

        int count = moreAdapter.getCount();
        if (count == 1) {
            indicator.setVisibility(View.GONE);
        } else {
            indicator.setVisibility(View.VISIBLE);
            initPagerListener(viewPager, count, indicator);
        }
    }

    /**
     * init PageListener
     *
     * @param viewPager
     * @param count
     * @param indicator
     */
    private static void initPagerListener(ViewPager viewPager, final int count, final ViewGroup indicator) {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setIndicator(indicator, count, position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * Set the page number
     *
     * @param indicator
     * @param total
     * @param current
     */
    private static void setIndicator(ViewGroup indicator, int total, int current) {
        indicator.removeAllViews();
        if (total > 1) {
            indicator.removeAllViews();
            for (int i = 0; i < total; i++) {
                ImageView img = new ImageView(indicator.getContext());
                img.setId(i);
                if (i == current) {
                    img.setBackgroundResource(R.mipmap.page_selected);
                } else {
                    img.setBackgroundResource(R.mipmap.page_unselected);
                }
                indicator.addView(img);
            }
        }
    }


    protected void loadCheckTab() {
        List<StickerCategory> stickerCategories = EmoManager.getInstance().getStickerCategories();
        int catePagers = 0;

        BottomCateView bottomView;
        for (StickerCategory sticker : stickerCategories) {
            int lastCount = catePagers;

            LogManager.getLogger().d(TAG, lastCount + "");
            stickPagerBeens.addAll(sticker.getStickPagerInfos());

            Context context = BaseApplication.getInstance().getBaseContext();
            bottomView = new BottomCateView(context, sticker.getName());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(SystemUtil.dipToPx(62), ViewGroup.LayoutParams.MATCH_PARENT);
            bottomView.setLayoutParams(params);
            bottomView.setTag(lastCount);
            bottomView.setOnClickListener(bottomClickListener);
            emojiTabview.addView(bottomView);

            catePagers += sticker.getPagerCount();
        }

        Context context = BaseApplication.getInstance().getBaseContext();
        emojiPagerAdapter = new EmojiPagerAdapter(context, stickPagerBeens);
        morePagerview.setAdapter(emojiPagerAdapter);
        morePagerview.setOffscreenPageLimit(3);
        morePagerview.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                StickPagerBean stickbean = stickPagerBeens.get(position);
                updateNumLayout(stickbean.getPosition(), stickbean.getCountCate());
                updateBottomTab(stickbean.getBottomPosi());
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        morePagerview.setCurrentItem(0);
        ((BottomCateView) emojiTabview.getChildAt(0)).setPress();

        StickPagerBean stickbean = stickPagerBeens.get(0);
        updateNumLayout(stickbean.getPosition(), stickbean.getCountCate());
    }

    private void updateNumLayout(int posi, int count) {
        int hasCount = emojiPagernum.getChildCount();
        int maxNum = Math.max(hasCount, count);

        ImageView img = null;
        for (int i = 0; i < maxNum; i++) {
            if (count <= hasCount) {
                if (i >= count) {
                    img = (ImageView) emojiPagernum.getChildAt(i);
                    img.setVisibility(View.GONE);
                    continue;
                } else {
                    img = (ImageView) emojiPagernum.getChildAt(i);
                }
            } else {
                if (i < hasCount) {
                    img = (ImageView) emojiPagernum.getChildAt(i);
                } else {
                    Context context = BaseApplication.getInstance().getBaseContext();
                    img = new ImageView(context);
                    img.setBackgroundResource(R.drawable.sec_dot);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(15, 15);
                    img.setLayoutParams(params);
                    params.setMargins(8, 8, 8, 8);
                    emojiPagernum.addView(img);
                }
            }

            img.setSelected(posi == i);
            img.setVisibility(View.VISIBLE);
        }
    }

    private void updateBottomTab(int posi) {
        int hasCount = emojiTabview.getChildCount();
        BottomCateView img = null;
        for (int i = 0; i < hasCount; i++) {
            img = (BottomCateView) emojiTabview.getChildAt(i);
            if (i == posi) {
                img.setPress();
            } else {
                img.setNormal();
            }
        }
    }

    private class BottomClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            int posi = (int) v.getTag();
            morePagerview.setCurrentItem(posi);
        }
    }

    private class EmojiPagerAdapterClickListener implements IEmojiClickListener {

        @Override
        public void onEmjClick(String emi) {
            ChatEditText editText = InputBottomLayout.bottomLayout.getInputedit();
            Editable mEditable = editText.getText();

            if (emi.equals(InputPanel.EMOJI_DELETE)) {
                editText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            } else {
                int start = editText.getSelectionStart();
                int end = editText.getSelectionEnd();
                start = (start < 0 ? 0 : start);
                end = (start < 0 ? 0 : end);
                mEditable.replace(start, end, emi);
            }
        }

        @Override
        public void onEmtClick(String emt) {
            MsgSend.sendOuterMsg(LinkMessageRow.Emotion, emt);
        }
    }
}