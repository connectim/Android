package connect.activity.home.adapter;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.activity.chat.bean.Talker;
import connect.activity.home.bean.GroupRecBean;
import connect.activity.home.bean.HomeAction;
import connect.activity.home.bean.HttpRecBean;
import connect.activity.home.bean.RoomAttrBean;
import connect.activity.home.view.ShowTextView;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.ui.activity.R;
import connect.utils.FileUtil;
import connect.utils.TimeUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.system.SystemDataUtil;
import connect.widget.MaterialBadgeTextView;
import connect.widget.SideScrollView;
import protos.Connect;

import static connect.widget.SideScrollView.SideScrollListener;

/**
 * Created by pujin on 2016/11/25.
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationHolder> {

    private RecyclerView recyclerView;
    private SideScrollView lastOpenScrollView = null;
    private LayoutInflater inflater;
    private List<RoomAttrBean> roomAttrBeanList = new ArrayList<>();

    public ConversationAdapter(Activity activity, RecyclerView recyclerView) {
        inflater = LayoutInflater.from(activity);
        this.recyclerView = recyclerView;
    }

    public void setData(List<RoomAttrBean> entities) {
        this.roomAttrBeanList = entities;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return roomAttrBeanList.size();
    }

    @Override
    public ConversationHolder onCreateViewHolder(ViewGroup parent, int arg1) {
        View view = inflater.inflate(R.layout.item_fm_chatlist, parent, false);
        ConversationHolder holder = new ConversationHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ConversationHolder holder, final int position) {
        final RoomAttrBean roomAttr = roomAttrBeanList.get(position);
        holder.directTxt.showText(roomAttr.getAt(),roomAttr.getDraft(), TextUtils.isEmpty(roomAttr.getContent()) ? "" : roomAttr.getContent());
        try {
            long sendtime = roomAttr.getTimestamp();
            holder.timeTxt.setText(0 == sendtime ? "" : TimeUtil.getMsgTime(TimeUtil.getCurrentTimeInLong(), sendtime));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (roomAttr.getRoomtype() == 2) {
            holder.nameTxt.setText(inflater.getContext().getString(R.string.app_name));
            GlideUtil.loadAvatarRound(holder.headImg, R.mipmap.connect_logo);
            holder.bottomNotify.setVisibility(View.GONE);
        } else if (roomAttr.getRoomtype() == Connect.ChatType.PRIVATE_VALUE || roomAttr.getRoomtype() == Connect.ChatType.GROUPCHAT_VALUE) {
            String showName = TextUtils.isEmpty(roomAttr.getName()) ? "" : roomAttr.getName();
            String showAvatar = TextUtils.isEmpty(roomAttr.getAvatar()) ? "" : roomAttr.getAvatar();

            holder.nameTxt.setText(showName);
            GlideUtil.loadAvatarRound(holder.headImg, showAvatar);
            holder.bottomNotify.setVisibility(View.VISIBLE);
        }

        if (0 == roomAttr.getStranger() || SharedPreferenceUtil.getInstance().getUser().getPubKey().equals(roomAttr.getRoomid())) {//not stranger
            holder.stangerTxt.setVisibility(View.GONE);
        } else {
            holder.stangerTxt.setVisibility(View.VISIBLE);
            holder.stangerTxt.setText(inflater.getContext().getString(R.string.Link_Stranger));
        }

        if (roomAttr.getTop() == 1) {
            holder.conTop.setVisibility(View.VISIBLE);
            holder.contentLayout.setBackgroundResource(R.color.color_f1f1f1);
        } else {
            holder.conTop.setVisibility(View.GONE);
            holder.contentLayout.setBackgroundResource(R.color.color_white);
        }

        if (Integer.valueOf(1).equals(roomAttr.getDisturb())) {
            holder.conNotify.setVisibility(View.VISIBLE);
            holder.bottomNotify.setSelected(true);
        } else {
            holder.conNotify.setVisibility(View.GONE);
            holder.bottomNotify.setSelected(false);
        }

        holder.badgeTxt.setBadgeCount(roomAttr.getDisturb(), roomAttr.getUnread());

        holder.contentLayout.getLayoutParams().width = SystemDataUtil.getScreenWidth();
        holder.contentLayout.setTag(holder.itemView);
        holder.contentLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SideScrollView scrollView = (SideScrollView) v.getTag();
                if (menuIsOpen(scrollView) || menuIsOpen(lastOpenScrollView)) {
                    closeMenu(scrollView);
                    closeMenu();
                } else {
                    closeMenu();

                    Talker talker = new Talker(Connect.ChatType.forNumber(roomAttr.getRoomtype()), roomAttr.getRoomid());
                    HomeAction.getInstance().sendEvent(HomeAction.HomeType.TOCHAT, talker);
                }
            }
        });
        holder.bottomTrash.setTag(holder.itemView);
        holder.bottomTrash.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SideScrollView scrollView = (SideScrollView) v.getTag();
                scrollView.closeMenu();

                String roomid = roomAttr.getRoomid();
                ConversionHelper.getInstance().deleteRoom(roomid);
                MessageHelper.getInstance().deleteRoomMsg(roomid);
                FileUtil.deleteContactFile(roomid);

                roomAttrBeanList.remove(roomAttr);
                notifyItemRemoved(position);
            }
        });
        holder.bottomNotify.setTag(holder.itemView);
        holder.bottomNotify.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SideScrollView scrollView = (SideScrollView) v.getTag();
                closeMenu(scrollView);

                ConversationHolder index = getItemHolder(position);
                if (index != null) {
                    boolean select = !(index.bottomNotify.isSelected());
                    index.bottomNotify.setSelected(select);
                    index.conNotify.setVisibility(select ? View.VISIBLE : View.GONE);

                    String roomid = roomAttrBeanList.get(position).getRoomid();
                    int unRead = roomAttrBeanList.get(position).getUnread();
                    index.badgeTxt.setBadgeCount(select ? 1 : 0, unRead);
                    int disturb = select ? 1 : 0;
                    ConversionSettingHelper.getInstance().updateDisturb(roomid, disturb);

                    roomAttr.setDisturb(disturb);
                    if (roomAttrBeanList.get(position).getRoomtype() == Connect.ChatType.GROUPCHAT_VALUE) {
                        GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupNotificaton, roomid, disturb);
                    }
                }
            }
        });
    }

    public ConversationHolder getItemHolder(int position) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
        if (position - firstItemPosition >= 0) {
            View view = recyclerView.getChildAt(position - firstItemPosition);
            if (null != recyclerView.getChildViewHolder(view)) {
                return (ConversationHolder) recyclerView.getChildViewHolder(view);
            }
        }
        return null;
    }

    class ConversationHolder extends RecyclerView.ViewHolder {

        private ImageView headImg;
        private TextView nameTxt;
        private ShowTextView directTxt;
        private TextView timeTxt;

        private TextView stangerTxt;
        private ImageView conTop;
        private ImageView conPri;
        private ImageView conNotify;
        private ImageView botNotify;
        private MaterialBadgeTextView badgeTxt;

        private RelativeLayout contentLayout;
        private LinearLayout bottomLayout;
        private RelativeLayout bottomTrash;
        private RelativeLayout bottomNotify;

        public ConversationHolder(View itemView) {
            super(itemView);
            contentLayout = (RelativeLayout) itemView.findViewById(R.id.content_layout);
            bottomLayout= (LinearLayout) itemView.findViewById(R.id.bottom_layout);
            bottomTrash = (RelativeLayout) itemView.findViewById(R.id.bottom_trash);
            bottomNotify = (RelativeLayout) itemView.findViewById(R.id.bottom_notify);

            headImg = (ImageView) itemView.findViewById(R.id.roundimg_head);
            nameTxt = (TextView) itemView.findViewById(R.id.usernameText);
            directTxt = (ShowTextView) itemView.findViewById(R.id.directTxtView);

            timeTxt = (TextView) contentLayout.findViewById(R.id.txt1);
            stangerTxt = (TextView) contentLayout.findViewById(R.id.txt2);
            conTop = (ImageView) contentLayout.findViewById(R.id.top);
            conPri = (ImageView) contentLayout.findViewById(R.id.privacy);
            conNotify = (ImageView) contentLayout.findViewById(R.id.notify);
            conNotify = (ImageView) contentLayout.findViewById(R.id.notify);
            botNotify = (ImageView) bottomNotify.findViewById(R.id.notify);

            badgeTxt = (MaterialBadgeTextView) contentLayout.findViewById(R.id.badgetv);
            ((SideScrollView) itemView).setSideScrollListener(sideScrollListener);
        }
    }

    public void closeMenu(SideScrollView scrollView) {
        if (scrollView != null) {
            scrollView.closeMenu();
        }
    }

    public void closeMenu() {
        closeMenu(lastOpenScrollView);
        lastOpenScrollView = null;
    }

    public Boolean menuIsOpen(SideScrollView scrollView) {
        return scrollView != null && scrollView.isOpen();
    }

    private SideScrollListener sideScrollListener = new SideScrollListener() {

        @Override
        public void onMenuIsOpen(View view) {
            lastOpenScrollView = (SideScrollView) view;
        }

        @Override
        public void onDownOrMove(SideScrollView slidingButtonView) {
            if (menuIsOpen(lastOpenScrollView)) {
                if (lastOpenScrollView != slidingButtonView) {
                    closeMenu();
                }
            }
        }
    };
}

