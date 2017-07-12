package connect.activity.chat.model.more;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.List;

import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.bean.ConversionSettingEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.activity.chat.bean.BaseAction;
import connect.activity.chat.bean.BurnNotice;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.RoomSession;
import connect.utils.DialogUtil;
import connect.utils.system.SystemUtil;

/**
 * Created by gtq on 2016/11/24.
 */
public class MorePagerAdapter extends PagerAdapter {

    private final int ITEM_COUNT_PER_GRID_VIEW = 8;
    private final int gridViewCount;

    private List<BaseAction> actionList;
    private Context context;
    private ViewPager viewPager;

    public MorePagerAdapter(ViewPager viewPager, List<BaseAction> actions) {
        this.context = viewPager.getContext();
        this.viewPager = viewPager;
        this.actionList = actions;
        this.gridViewCount = (actions.size() + ITEM_COUNT_PER_GRID_VIEW - 1) / ITEM_COUNT_PER_GRID_VIEW;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        int end = (position + 1) * ITEM_COUNT_PER_GRID_VIEW > actionList.size() ? actionList
                .size() : (position + 1) * ITEM_COUNT_PER_GRID_VIEW;
        List<BaseAction> subBaseActions = actionList.subList(position
                * ITEM_COUNT_PER_GRID_VIEW, end);

        GridView gridView = new GridView(context);

        gridView.setNumColumns(4);
        container.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
                layoutParams.height = SystemUtil.dipToPx(180);
                viewPager.setLayoutParams(layoutParams);
            }
        });

        gridView.setAdapter(new MGridAdapter(context, subBaseActions));
        gridView.setSelector(R.color.transparent);
        gridView.setHorizontalSpacing(0);
        gridView.setVerticalSpacing(0);
        gridView.setGravity(Gravity.CENTER);
        gridView.setTag(Integer.valueOf(position));
        gridView.setOnItemClickListener(new GridView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BaseAction baseAction = (BaseAction) parent.getItemAtPosition(position);
                switch (baseAction.getTitleId()) {
                    case R.string.Chat_Photo:
                        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.HIDEPANEL);
                        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.RECENT_ALBUM);
                        break;
                    case R.string.Chat_Sight:
                        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.TAKE_PHOTO);
                        break;
                    case R.string.Wallet_Transfer:
                        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.TRANSFER);
                        break;
                    case R.string.Wallet_Packet:
                        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.REDPACKET);
                        break;
                    case R.string.Wallet_Receipt:
                        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.GATHER);
                        break;
                    case R.string.Chat_Read_Burn:
                        String roomkey = RoomSession.getInstance().getRoomKey();
                        ConversionSettingEntity chatSetEntity = ConversionSettingHelper.getInstance().loadSetEntity(roomkey);

                        long burncount = (null == chatSetEntity || null == chatSetEntity.getSnap_time()) ? 0 : chatSetEntity.getSnap_time();
                        DialogUtil.showBurnDialog(context, burncount, onTimerListener);
                        break;
                    case R.string.Chat_Name_Card:
                        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.NAMECARD);
                        break;
                    case R.string.Chat_Loc:
                        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.MAP_LOCATION);
                        break;
                }
            }
        });

        container.addView(gridView);
        return gridView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // TODO
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return gridViewCount;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    private OnTimerListener onTimerListener = new OnTimerListener() {
        @Override
        public void itemTimerClick(long time) {
            String roomkey = RoomSession.getInstance().getRoomKey();
            ConversionSettingEntity chatSetEntity = ConversionSettingHelper.getInstance().loadSetEntity(roomkey);
            if (null == chatSetEntity) {
                chatSetEntity = new ConversionSettingEntity();
                chatSetEntity.setIdentifier(roomkey);
                chatSetEntity.setSnap_time(0L);
            }

            if (!Long.valueOf(time).equals(chatSetEntity.getSnap_time())) {
                BurnNotice.sendBurnMsg(BurnNotice.BurnType.BURN_START, time);
                MsgSend.sendOuterMsg(MsgType.Self_destruct_Notice, time);
                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.BURNSTATE, time == 0 ? 0 : 1);

                chatSetEntity.setSnap_time(time);
                ConversionSettingHelper.getInstance().insertSetEntity(chatSetEntity);
            }
        }
    };

    public interface OnTimerListener {
        void itemTimerClick(long time);
    }
}