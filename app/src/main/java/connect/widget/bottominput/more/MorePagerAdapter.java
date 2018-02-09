package connect.widget.bottominput.more;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import connect.activity.chat.bean.BaseAction;
import connect.activity.chat.bean.RecExtBean;
import connect.ui.activity.R;
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
                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.HIDEPANEL);
                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.RECENT_ALBUM);
                        break;
                    case R.string.Chat_Sight:
                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.TAKE_PHOTO);
                        break;
                    case R.string.Wallet_Transfer:
                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.TRANSFER);
                        break;
                    case R.string.Wallet_Packet:
                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.REDPACKET);
                        break;
                    case R.string.Wallet_Receipt:
                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.PAYMENT);
                        break;
                    case R.string.Chat_Name_Card:
                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.NAMECARD);
                        break;
                    case R.string.Chat_Loc:
                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MAP_LOCATION);
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

    public interface OnItemClickListener{
        void itemClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView time;
        ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            time = (TextView) itemView.findViewById(R.id.txt);
            img = (ImageView) itemView.findViewById(R.id.img);
        }
    }
}