package connect.ui.activity.chat.model.emoji;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import connect.ui.activity.R;
import connect.ui.activity.chat.bean.StickPagerBean;
import connect.ui.activity.chat.bean.StickerCategory;
import connect.ui.activity.chat.inter.IEmojiClickListener;
import connect.ui.activity.chat.model.EmoManager;
import connect.ui.activity.chat.model.EmojiPagerAdapter;
import connect.ui.activity.chat.view.BottomCateView;
import connect.utils.system.SystemUtil;
import connect.utils.log.LogManager;

/**
 * emoji
 * Created by gtq on 2016/11/25.
 */
public class EmojiPanel {

    private static EmojiPanel emojiPanel;

    public static EmojiPanel getInstance() {
        if (emojiPanel == null) {
            emojiPanel = new EmojiPanel();
        }
        return emojiPanel;
    }

    private String Tag = "EmojiPanel";

    private Context context;
    private ViewPager emojiPager;
    private LinearLayout pagerNumLayout;
    private LinearLayout tabView;

    private EmojiPagerAdapter emojiPagerAdapter;

    private List<StickPagerBean> stickPagerBeens = new ArrayList<>();

    public void init(View view) {
        this.context = view.getContext();
        emojiPager = (ViewPager) view.findViewById(R.id.emoji_pagerview);
        pagerNumLayout = (LinearLayout) view.findViewById(R.id.emoji_pagernum);
        tabView = (LinearLayout) view.findViewById(R.id.emoji_tabview);
        loadCheckTab();
    }

    protected void loadCheckTab() {
        List<StickerCategory> stickerCategories = EmoManager.getInstance().getStickerCategories();
        int catePagers = 0;

        BottomCateView bottomView;
        for (StickerCategory sticker : stickerCategories) {
            int lastCount = catePagers;

            LogManager.getLogger().d(Tag, lastCount + "");
            stickPagerBeens.addAll(sticker.getStickPagerInfos());

            bottomView = new BottomCateView(context, sticker.getName());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(SystemUtil.dipToPx(62), ViewGroup.LayoutParams.MATCH_PARENT);
            bottomView.setLayoutParams(params);
            bottomView.setTag(lastCount);
            bottomView.setOnClickListener(bottomClickListener);
            tabView.addView(bottomView);

            catePagers += sticker.getPagerCount();
        }

        emojiPagerAdapter = new EmojiPagerAdapter(context, stickPagerBeens);
        emojiPager.setAdapter(emojiPagerAdapter);
        emojiPager.setOffscreenPageLimit(3);
        emojiPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

        emojiPager.setCurrentItem(0);
        ((BottomCateView) tabView.getChildAt(0)).selectState(true);

        StickPagerBean stickbean = stickPagerBeens.get(0);
        updateNumLayout(stickbean.getPosition(), stickbean.getCountCate());
    }

    private void updateNumLayout(int posi, int count) {
        int hasCount = pagerNumLayout.getChildCount();
        int maxNum = Math.max(hasCount, count);

        ImageView img = null;
        for (int i = 0; i < maxNum; i++) {
            if (count <= hasCount) {
                if (i >= count) {
                    img = (ImageView) pagerNumLayout.getChildAt(i);
                    img.setVisibility(View.GONE);
                    continue;
                } else {
                    img = (ImageView) pagerNumLayout.getChildAt(i);
                }
            } else {
                if (i < hasCount) {
                    img = (ImageView) pagerNumLayout.getChildAt(i);
                } else {
                    img = new ImageView(context);
                    img.setBackgroundResource(R.drawable.sec_dot);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(15, 15);
                    img.setLayoutParams(params);
                    params.setMargins(8, 8, 8, 8);
                    pagerNumLayout.addView(img);
                }
            }

            img.setSelected(posi == i);
            img.setVisibility(View.VISIBLE);
        }
    }

    private void updateBottomTab(int posi) {
        int hasCount = tabView.getChildCount();
        BottomCateView img = null;
        for (int i = 0; i < hasCount; i++) {
            img = (BottomCateView) tabView.getChildAt(i);
            img.selectState(i == posi);
        }
    }

    View.OnClickListener bottomClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int posi = (int) v.getTag();
            emojiPager.setCurrentItem(posi);
        }
    };

    public void setiEmojiClickListener(IEmojiClickListener iEmojiClickListener) {
        emojiPagerAdapter.setiEmojiClickListener(iEmojiClickListener);
    }
}