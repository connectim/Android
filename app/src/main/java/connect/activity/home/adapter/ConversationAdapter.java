package connect.activity.home.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import connect.activity.home.bean.ConversationAction;
import connect.activity.home.bean.RoomAttrBean;
import connect.activity.home.view.ShowTextView;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ConversionEntity;
import connect.ui.activity.R;
import connect.utils.FileUtil;
import connect.utils.TimeUtil;
import connect.utils.dialog.DialogUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.MaterialBadgeTextView;
import protos.Connect;

/**
 * Created by pujin on 2016/11/25.
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationHolder> {

    private Context context;
    private ItemListener itemListener;
    private List<RoomAttrBean> roomAttrBeanList = new ArrayList<>();

    private ItemClickListener itemClickListener = new ItemClickListener();
    private ItemLongClickListener longClickListener = new ItemLongClickListener();

    public void setData(List<RoomAttrBean> entities) {
        this.roomAttrBeanList.clear();
        this.roomAttrBeanList.addAll(entities);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return roomAttrBeanList.size();
    }

    @Override
    public ConversationHolder onCreateViewHolder(ViewGroup parent, int arg1) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_fm_chatlist, parent, false);
        ConversationHolder holder = new ConversationHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ConversationHolder holder, final int position) {
        final RoomAttrBean roomAttr = roomAttrBeanList.get(position);
        holder.directTxt.showText(roomAttr.getUnreadAt(), roomAttr.getUnreadAttention(), roomAttr.getDraft(), TextUtils.isEmpty(roomAttr.getContent()) ? "" : roomAttr.getContent());
        try {
            long sendtime = roomAttr.getTimestamp();
            holder.timeTxt.setText(0 == sendtime ? "" : TimeUtil.getMsgTime(TimeUtil.getCurrentTimeInLong(), sendtime));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Integer.valueOf(1).equals(roomAttr.getDisturb())) {
            holder.notifyImg.setVisibility(View.VISIBLE);
        } else {
            holder.notifyImg.setVisibility(View.GONE);
        }
        if (roomAttr.getRoomtype() == Connect.ChatType.CONNECT_SYSTEM_VALUE) {
            holder.nameTxt.setText(context.getString(R.string.app_name));
            GlideUtil.loadAvatarRound(holder.headImg, R.mipmap.connect_logo);
            holder.badgeTxt.setBadgeCount(roomAttr.getDisturb(), roomAttr.getUnread());
        } else if (roomAttr.getRoomtype() == Connect.ChatType.SUBSCRIBER_VALUE) {
            holder.nameTxt.setText(context.getString(R.string.Chat_Subscriber));
            GlideUtil.loadAvatarRound(holder.headImg, R.mipmap.chat_rss_subscribe);
            holder.badgeTxt.setBadgeCount(1, roomAttr.getUnread());
        } else if (roomAttr.getRoomtype() == Connect.ChatType.PRIVATE_VALUE ||
                roomAttr.getRoomtype() == Connect.ChatType.GROUPCHAT_VALUE ||
                roomAttr.getRoomtype() == Connect.ChatType.GROUP_DISCUSSION_VALUE) {
            String showName = TextUtils.isEmpty(roomAttr.getName()) ? "" : roomAttr.getName();
            if (showName.length() > 15) {
                showName = showName.subSequence(0, 15) + "...";
            }
            String showAvatar = TextUtils.isEmpty(roomAttr.getAvatar()) ? "" : roomAttr.getAvatar();

            holder.nameTxt.setText(showName);
            GlideUtil.loadAvatarRound(holder.headImg, showAvatar);
            holder.badgeTxt.setBadgeCount(roomAttr.getDisturb(), roomAttr.getUnread());
        }

        if (roomAttr.getTop() == 1) {
            holder.topImg.setVisibility(View.VISIBLE);
            holder.itemRelative.setBackgroundResource(R.color.color_f1f1f1);
        } else {
            holder.topImg.setVisibility(View.GONE);
            holder.itemRelative.setBackgroundResource(R.color.color_white);
        }

        holder.itemRelative.setTag(R.id.position, position);
        holder.itemRelative.setTag(R.id.roomid, roomAttr.getRoomid());
        holder.itemRelative.setTag(R.id.roomtype, roomAttr.getRoomtype());
        holder.itemRelative.setTag(R.id.roomtop,roomAttr.getTop());
        holder.itemRelative.setOnClickListener(itemClickListener);
        holder.itemRelative.setOnLongClickListener(longClickListener);
    }

    private class ItemClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String roomIdentify = (String) view.getTag(R.id.roomid);
            int roomType = (int) view.getTag(R.id.roomtype);

            itemListener.itemClick(Connect.ChatType.forNumber(roomType), roomIdentify);
        }
    }

    private class ItemLongClickListener implements View.OnLongClickListener {

        boolean isDeleteAble = true;
        private int position;
        private String roomId;
        private int top;

        @Override
        public boolean onLongClick(View view) {
            position = (int) view.getTag(R.id.position);
            roomId = (String) view.getTag(R.id.roomid);
            top = (int) view.getTag(R.id.roomtop);

            String[] strings = new String[]{
                    (top <= 0 ? context.getResources().getString(R.string.Chat_Message_Top) :
                            context.getResources().getString(R.string.Chat_Message_Top_Remove)
                    ),
                    context.getResources().getString(R.string.Chat_Conversation_Del)
            };
            DialogUtil.showItemListView(context, Arrays.asList(strings), new DialogUtil.DialogListItemClickListener() {

                @Override
                public void confirm(int position) {
                    switch (position) {
                        case 0:
                            topMessage();
                            break;
                        case 1:
                            deleteMessage();
                            break;
                    }
                }
            });
            return false;
        }

        protected void topMessage() {
            ConversionEntity conversionEntity = ConversionHelper.getInstance().loadRoomEnitity(roomId);
            if (conversionEntity == null) {
                conversionEntity = new ConversionEntity();
                conversionEntity.setIdentifier(roomId);
            }
            int top = (null == conversionEntity.getTop() || conversionEntity.getTop() == 0) ? 1 : 0;
            conversionEntity.setTop(top);
            ConversionHelper.getInstance().insertRoomEntity(conversionEntity);

            ConversationAction.conversationAction.sendEvent(ConversationAction.ConverType.LOAD_MESSAGE);
        }

        protected void deleteMessage() {
            ConversionHelper.getInstance().deleteRoom(roomId);
            MessageHelper.getInstance().deleteRoomMsg(roomId);
            FileUtil.deleteContactFile(roomId);
            ConversationAction.conversationAction.sendEvent(ConversationAction.ConverType.LOAD_UNREAD);

            if (isDeleteAble) {
                isDeleteAble = false;
                roomAttrBeanList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                            isDeleteAble = true;//可点击按钮
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    class ConversationHolder extends RecyclerView.ViewHolder {

        private RelativeLayout itemRelative;

        private MaterialBadgeTextView badgeTxt;
        private ImageView headImg;

        private TextView nameTxt;
        private ShowTextView directTxt;

        private ImageView topImg;
        private ImageView notifyImg;
        private TextView timeTxt;

        public ConversationHolder(View itemView) {
            super(itemView);
            itemRelative = (RelativeLayout) itemView.findViewById(R.id.relative_item);
            headImg = (ImageView) itemView.findViewById(R.id.roundimg_head);
            nameTxt = (TextView) itemView.findViewById(R.id.usernameText);
            directTxt = (ShowTextView) itemView.findViewById(R.id.directTxtView);

            timeTxt = (TextView) itemView.findViewById(R.id.txt1);
            topImg = (ImageView) itemView.findViewById(R.id.top);
            notifyImg = (ImageView) itemView.findViewById(R.id.notify);

            badgeTxt = (MaterialBadgeTextView) itemView.findViewById(R.id.badgetv);
        }
    }

    public void setConversationListener(ItemListener itemListener) {
        this.itemListener = itemListener;
    }

    public interface ItemListener{
        void itemClick(Connect.ChatType chatType,String identify);
    }
}

