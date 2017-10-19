package connect.activity.chat.adapter;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connect.activity.base.BaseListener;
import connect.activity.chat.bean.ItemViewType;
import connect.activity.chat.bean.LinkMessageRow;
import instant.bean.MsgDirect;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.row.MsgBaseRow;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.MessageEntity;
import connect.ui.activity.R;
import connect.utils.FileUtil;
import connect.utils.TimeUtil;
import instant.bean.ChatMsgEntity;
import protos.Connect;

/**
 * Created by gtq on 2016/11/23.
 */
public class ChatAdapter extends RecyclerView.Adapter<MsgBaseHolder> {
    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    protected List<ChatMsgEntity> msgEntities = new ArrayList<>();

    private Map<String, ChatMsgEntity> msgEntityMap = new HashMap<>();

    public ChatAdapter(Activity activity, RecyclerView recycler, LinearLayoutManager manager) {
        this.inflater = LayoutInflater.from(activity);
        this.recyclerView = recycler;
        this.layoutManager = manager;
    }

    public List<ChatMsgEntity> getMsgEntities() {
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
        ChatMsgEntity msgExtEntity = msgEntities.get(position);
        MsgDirect dirct = msgExtEntity.parseDirect();
        return msgExtEntity.getMessageType() * dirct.dirct;
    }

    @Override
    public MsgBaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemViewType itemType = ItemViewType.toItemViewType(viewType);
        MsgBaseRow baseRow = null;

        if (itemType == null) {
            itemType = ItemViewType.NOTICE_FROM;
            baseRow = LinkMessageRow.NOTICE.msgBaseRow;
        } else {
            baseRow = itemType.messageRow.msgBaseRow;
        }
        return baseRow.buildRowView(inflater, MsgDirect.toDirect(itemType.direct));
    }

    @Override
    public void onBindViewHolder(MsgBaseHolder holder, int position) {
        ChatMsgEntity msgExtEntity = msgEntities.get(position);

        long lasttime = 0;
        if (position == 0) {
            MessageEntity lastMsgEntity = MessageHelper.getInstance().loadMsgLessMsgid(msgExtEntity.getMessage_id());
            if (lastMsgEntity != null) {
                lasttime = lastMsgEntity.getCreatetime();
            }
        } else {
            ChatMsgEntity lastMsgEntity = msgEntities.get(position - 1);
            lasttime = lastMsgEntity.getCreatetime();
        }

        long curtime = msgExtEntity.getCreatetime();
        try {
            holder.buildMsgTime(lasttime, curtime);
            holder.buildRowData(holder, msgExtEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler itemsUpdateHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                BaseListener listener = (BaseListener) msg.obj;
                listener.Success(0);
            } else {
                Message message = new Message();
                message.what = 50;
                message.obj = msg.obj;
                itemsUpdateHandler.sendMessageDelayed(message, 100);
            }
        }
    };

    public void insertItem(final ChatMsgEntity t) {
        BaseListener listener = new BaseListener() {
            @Override
            public void Success(Object ts) {
                int posi = msgEntities.size();
                msgEntities.add(posi, t);
                notifyItemInserted(posi);

                msgEntityMap.put(t.getMessage_id(), t);
            }

            @Override
            public void fail(Object... objects) {

            }
        };

        Message message = new Message();
        message.what = 50;
        message.obj = listener;
        itemsUpdateHandler.sendMessage(message);
    }

    public void insertItems(final List<ChatMsgEntity> entities) {
        BaseListener listener = new BaseListener() {
            @Override
            public void Success(Object ts) {
                msgEntities.addAll(0, entities);
                notifyDataSetChanged();

                for (ChatMsgEntity entity : entities) {
                    msgEntityMap.put(entity.getMessage_id(), entity);
                }
            }

            @Override
            public void fail(Object... objects) {

            }
        };

        Message message = new Message();
        message.what = 50;
        message.obj = listener;
        itemsUpdateHandler.sendMessage(message);
    }

    public void removeItem(final ChatMsgEntity t) {
        BaseListener listener = new BaseListener() {
            @Override
            public void Success(Object ts) {
                int posi = msgEntities.lastIndexOf(t);
                if (posi >= 0) {
                    msgEntities.remove(posi);
                    notifyItemRemoved(posi);

                    msgEntityMap.remove(t.getMessage_id());
                }
            }

            @Override
            public void fail(Object... objects) {

            }
        };

        Message message = new Message();
        message.what = 50;
        message.obj = listener;
        itemsUpdateHandler.sendMessage(message);
    }

    public void updateItemSendState(String msgid, int state) {
        ChatMsgEntity msgExtEntity = msgEntityMap.get(msgid);
        if (msgExtEntity != null) {
            msgExtEntity.setSend_status(state);
        }
    }

    public void showImgMsgs(final BaseListener listener) {
        new AsyncTask<List<ChatMsgEntity>, Void, ArrayList<String>>() {
            @Override
            protected ArrayList<String> doInBackground(List<ChatMsgEntity>... params) {
                ArrayList<String> imgList = new ArrayList<>();
                for (int i = 0; i < msgEntities.size(); i++) {
                    ChatMsgEntity index = msgEntities.get(i);

                    LinkMessageRow msgType = LinkMessageRow.toMsgType(index.getMessageType());
                    if (msgType == LinkMessageRow.Photo) {
                        try {
                            Connect.PhotoMessage photoMessage = Connect.PhotoMessage.parseFrom(index.getContents());
                            String thumb = photoMessage.getThum();
                            String path = FileUtil.isLocalFile(thumb) ? thumb : FileUtil.newContactFileName(index.getMessage_ower(), index.getMessage_id(), FileUtil.FileType.IMG);
                            imgList.add(path);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return imgList;
            }

            @Override
            protected void onPostExecute(ArrayList<String> strings) {
                super.onPostExecute(strings);
                listener.Success(strings);
            }
        }.execute(msgEntities);
    }

    public void unReadVoice(final String msgid) {
        new AsyncTask<List<ChatMsgEntity>, Void, Integer>() {
            @Override
            protected Integer doInBackground(List<ChatMsgEntity>... params) {
                int holdPosi = -1;
                ChatMsgEntity msgExtEntity = msgEntityMap.get(msgid);
                if (msgExtEntity != null) {
                    List<ChatMsgEntity> msgEntities = params[0];
                    int readPosi = msgEntities.indexOf(msgExtEntity);
                    for (int i = readPosi; i < msgEntities.size(); i++) {
                        msgExtEntity = msgEntities.get(i);//Ergodic voice list

                        LinkMessageRow msgType = LinkMessageRow.toMsgType(msgExtEntity.getMessageType());
                        if (msgType == LinkMessageRow.Voice && msgExtEntity.getRead_time() == 0) {
                            holdPosi = i;
                        }
                    }
                }
                return holdPosi;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                if (integer != -1) {
                    MsgBaseHolder holder = viewHoldByPosition(integer);
                    if (holder != null) {
                        holder.itemView.findViewById(R.id.voicemsg).performClick();
                    }
                }
            }
        }.execute(msgEntities);
    }

    /**
     * The message has been read
     *
     * @param msgid
     */
    public void hasReadBurnMsg(String msgid) {
        ChatMsgEntity msgEntity = msgEntityMap.get(msgid);
        long burntime = TimeUtil.getCurrentTimeInLong();
        if (msgEntity != null) {
            msgEntity.setRead_time(burntime);
        }

        //Modify read time
        ChatMsgEntity messageEntity = MessageHelper.getInstance().loadMsgByMsgid(msgid);
        if (messageEntity != null) {
            messageEntity.setSnap_time(burntime);
            MessageHelper.getInstance().updateMsg(MessageEntity.chatMsgToMessageEntity(messageEntity));
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
}
