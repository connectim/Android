package connect.ui.activity.chat.model.more;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import connect.ui.activity.R;
import connect.ui.activity.chat.ChatActivity;
import connect.ui.activity.chat.bean.BaseAction;
import connect.ui.activity.chat.bean.RoomSession;

import java.util.ArrayList;
import java.util.List;

/**
 * more panel
 * Created by gtq on 2016/11/24.
 */
public class MorePanel {

    private int roomType;
    private View view;

    public void init() {
        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.more_pagerview);
        final ViewGroup indicator = (ViewGroup) view.findViewById(R.id.more_indicator);

        List<BaseAction> actionList = new ArrayList<>();

        actionList.add(new BaseAction(R.mipmap.chat_bar_picture, R.string.Chat_Photo));
        actionList.add(new BaseAction(R.mipmap.chat_bar_camera, R.string.Chat_Sight));

        if (roomType == 2) {

        } else {
            actionList.add(new BaseAction(R.mipmap.chat_bar_trasfer, R.string.Wallet_Transfer));
            actionList.add(new BaseAction(R.mipmap.chat_bar_redbag, R.string.Wallet_Packet));
            actionList.add(new BaseAction(R.mipmap.message_send_payment2x, R.string.Wallet_Receipt));
            if (roomType == 0) {
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

    public void setView(View view) {
        this.view = view;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
        init();
    }
}