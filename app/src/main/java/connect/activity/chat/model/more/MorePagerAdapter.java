package connect.activity.chat.model.more;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import connect.activity.contact.adapter.FriendRecordAdapter;
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
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import protos.Connect;

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
                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.GATHER);
                        break;
                    case R.string.Chat_Read_Burn:
                        String roomkey = RoomSession.getInstance().getRoomKey();
                        ConversionSettingEntity chatSetEntity = ConversionSettingHelper.getInstance().loadSetEntity(roomkey);

                        long burncount = (null == chatSetEntity || null == chatSetEntity.getSnap_time()) ? 0 : chatSetEntity.getSnap_time();
                        showBurnDialog(context, burncount, onTimerListener);
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
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.BURNSTATE, time == 0 ? 0 : 1);

                chatSetEntity.setSnap_time(time);
                ConversionSettingHelper.getInstance().insertSetEntity(chatSetEntity);
            }
        }
    };

    public interface OnTimerListener {
        void itemTimerClick(long time);
    }

    /**
     * burn message dialog
     */
    public Dialog showBurnDialog(final Context mContext, final long sectime, final MorePagerAdapter.OnTimerListener listener) {
        final Dialog dialog = new Dialog(mContext, R.style.Dialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_burn, null);
        dialog.setContentView(view);

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linearlayout);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(linearLayoutManager);

        ViewGroup.LayoutParams layoutParams = null;
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(layoutParams);

        final String[] strings = mContext.getResources().getStringArray(R.array.destruct_timer);
        final int[] destimes = mContext.getResources().getIntArray(R.array.destruct_timer_long);

        BaseAdapter baseAdapter=new BaseAdapter(mContext,sectime,strings,destimes);
        recyclerView.setAdapter(baseAdapter);
        baseAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void itemClick(int position) {
                listener.itemTimerClick(destimes[position]);
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Window mWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = mWindow.getAttributes();
        lp.width = SystemDataUtil.getScreenWidth();
        mWindow.setGravity(Gravity.BOTTOM);
        mWindow.setWindowAnimations(R.style.DialogAnim);
        mWindow.setAttributes(lp);
        dialog.show();
        return dialog;
    }


    public interface OnItemClickListener{
        void itemClick(int position);
    }


    class BaseAdapter extends RecyclerView.Adapter<MorePagerAdapter.ViewHolder>{

        private Context context;
        private long sectime;
        private String[] strings;
        private int[] destimes;

        public BaseAdapter(Context context,long sectime,String[] strings,int[] destimes){
            this.context=context;
            this.strings=strings;
            this.sectime=sectime;
            this.destimes=destimes;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.item_dialog_burn, parent, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.time.setText(strings[position]);
            if (position == strings.length - 1) {
                holder.time.setTextColor(context.getResources().getColor(R.color.color_blue));
            } else {
                holder.time.setTextColor(context.getResources().getColor(R.color.color_black));
            }

            if (sectime == destimes[position]) {
                holder.img.setVisibility(View.VISIBLE);
            } else {
                holder.img.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.itemClick(position);
                }
            });
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return strings.length;
        }

        private OnItemClickListener itemClickListener;

        public void setItemClickListener(OnItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }
    };

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