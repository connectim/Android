package connect.ui.activity.chat.adapter;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.bean.MessageEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.ItemViewType;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.model.ChatMsgUtil;
import connect.ui.activity.chat.view.holder.MsgBaseHolder;
import connect.ui.activity.chat.view.row.MsgBaseRow;
import connect.utils.FileUtil;
import connect.utils.TimeUtil;

/**
 *
 * Created by gtq on 2016/11/23.
 */
public class ChatAdapter extends RecyclerView.Adapter<MsgBaseHolder> {
    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    protected List<MsgEntity> msgEntities = new ArrayList<>();

    public ChatAdapter(Activity activity, RecyclerView recycler, LinearLayoutManager manager) {
        this.inflater = LayoutInflater.from(activity);
        this.recyclerView = recycler;
        this.layoutManager = manager;
    }

    public void setDatas(List<MsgEntity> entities) {
        this.msgEntities = entities;
        notifyDataSetChanged();
    }

    public List<MsgEntity> getMsgEntities() {
        return msgEntities;
    }

    @Override
    public int getItemCount() {
        return msgEntities.size();
    }

    /**
     * direct -1:From 1:To
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        MsgDefinBean definBean = msgEntities.get(position).getMsgDefinBean();
        MsgDirect dirct = ChatMsgUtil.parseMsgDirect(definBean);
        return definBean.getType() * dirct.dirct;
    }

    @Override
    public MsgBaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemViewType itemType = ItemViewType.toItemViewType(viewType);
        MsgBaseRow baseRow = null;

        if (itemType == null) {
            itemType = ItemViewType.NOTICE_FROM;
            baseRow = MsgType.NOTICE.msgBaseRow;
        } else {
            baseRow = itemType.msgType.msgBaseRow;
        }
        return baseRow.buildRowView(inflater, MsgDirect.toDirect(itemType.direct));
    }

    @Override
    public void onBindViewHolder(MsgBaseHolder holder, int position) {
        long curtime = msgEntities.get(position).getMsgDefinBean().getSendtime();

        long nexttime = 0;
        if (position == msgEntities.size() - 1) {//Slide to the last
            if (position == 0) {//just only one message
                nexttime = curtime - 4 * 60 * 1000;
            } else {//
                nexttime = msgEntities.get(position - 1).getMsgDefinBean().getSendtime();
            }
        } else {
            nexttime = msgEntities.get(position + 1).getMsgDefinBean().getSendtime();
        }

        holder.buildMsgTime(curtime, nexttime);
        holder.buildRowData(holder, msgEntities.get(position));
    }

    public void insertItem(MsgEntity t) {
        int posi = msgEntities.size();
        msgEntities.add(posi, t);
        notifyItemInserted(posi);
    }

    public void insertMoreItems(List<MsgEntity> entities) {
        msgEntities.addAll(0, entities);
        notifyDataSetChanged();
    }

    public void removeItem(MsgEntity t) {
        int posi = msgEntities.lastIndexOf(t);
        if (posi >= 0) {
            msgEntities.remove(posi);
            notifyItemRemoved(posi);
        }
    }

    public void updateItemSendState(String msgid, int state) {
        for (int i = 0; i < msgEntities.size(); i++) {
            MsgEntity chatBean = msgEntities.get(i);
            if (chatBean.getMsgDefinBean().getMessage_id().equals(msgid)) {
                chatBean.setSendstate(state);
                break;
            }
        }
    }

    public ArrayList<String> showImgMsgs() {
        ArrayList<String> imgList = new ArrayList<>();
        for (int i = 0; i < msgEntities.size(); i++) {
            MsgEntity index = msgEntities.get(i);
            MsgDefinBean definBean = index.getMsgDefinBean();
            if (definBean.getType() == MsgType.Photo.type) {
                String thumb = definBean.getContent();
                String path = FileUtil.islocalFile(thumb) ? thumb : FileUtil.newContactFileName(index.getPubkey(), definBean.getMessage_id(), FileUtil.FileType.IMG);
                imgList.add(path);
            }
        }
        return imgList;
    }

    public void unReadVoice(String msgid) {
        boolean startUnrRead = false;
        for (int i = 0; i < msgEntities.size(); i++) {
            MsgEntity index = msgEntities.get(i);
            if (index.getMsgDefinBean().getMessage_id().equals(msgid)) {
                startUnrRead = true;
                continue;
            }

            if (startUnrRead) {
                MsgDefinBean definBean = index.getMsgDefinBean();
                if (definBean.getType() == MsgType.Voice.type && index.getReadstate() == 0) {
                    MsgBaseHolder holder = viewHoldByPosition(i);
                    if (holder != null) {
                        holder.itemView.findViewById(R.id.voicemsg).performClick();
                        break;
                    }
                }
            }
        }
    }

    /**
     * The message has been read
     * @param msgid
     */
    public void hasReadBurnMsg(String msgid) {
        for (int i = 0; i < msgEntities.size(); i++) {
            MsgEntity index = msgEntities.get(i);
            if (!"Connect".equals(index.getPubkey())) {
                if (index.getMsgDefinBean().getMessage_id().equals(msgid)) {
                    long burntime = TimeUtil.getCurrentTimeInLong();
                    ((MsgEntity) index).setBurnstarttime(burntime);

                    //Modify read time
                    MessageEntity msgEntity = MessageHelper.getInstance().loadMsgByMsgid(msgid);
                    if (msgEntity != null) {
                        msgEntity.setSnap_time(burntime);
                        MessageHelper.getInstance().updateMsg(msgEntity);
                    }
                }
            }
        }
    }

    public MsgBaseHolder viewHoldByPosition(int position) {
        int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
        MsgBaseHolder viewHolder = null;
        if (position - firstItemPosition >= 0) {
            View view = recyclerView.getChildAt(position - firstItemPosition);
            if (null != recyclerView.getChildViewHolder(view)) {
                viewHolder = (MsgBaseHolder) recyclerView.getChildViewHolder(view);
            }
        }
        return viewHolder;
    }

    public void clearHistory() {
        this.msgEntities.clear();
        notifyDataSetChanged();
    }

    public MsgEntity firstEntity() {
        if (msgEntities.size() == 0) return null;
        return msgEntities.get(0);
    }

    public MsgEntity lastEntity() {
        if (msgEntities.size() == 0) return null;
        return msgEntities.get(getItemCount() - 1);
    }
}
