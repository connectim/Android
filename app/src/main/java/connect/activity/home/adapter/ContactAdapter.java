package connect.activity.home.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.contact.model.ContactListManage;
import connect.activity.home.bean.ContactBean;
import connect.ui.activity.R;
import connect.utils.PinyinUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.DepartmentAvatar;

/**
 * Contact adapter.
 */
public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private ArrayList<ContactBean> mData = new ArrayList<>();
    private OnItemChildListener onItemChildListener;
    private ContactListManage contactManage = new ContactListManage();
    private List<ContactBean> groupList;
    private ArrayList<ContactBean> friendList;
    /** The location of the sideBar started sliding */
    private int startPosition = 0;
    /** Friends / group */
    private final int STATUS_FRIEND = 101;
    /** Number of friends */
    private final int STATUS_FRIEND_COUNT = 102;
    /** Connect robot */
    private final int STATUS_FRIEND_CONNECT = 103;
    /** Connect robot */
    private final int STATUS_FRIEND_TITLE = 104;
    /** Update all contact */
    public final String updateTypeContact = "contact";
    /** Update friend request */
    public final String updateTypeRequest = "request";
    /** Update group */
    public final String updateTypeGroup = "group";
    /** Update friend */
    public final String updateTypeFriend = "friend";

    public ContactAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view;
        RecyclerView.ViewHolder holder = null;
        if (viewType == STATUS_FRIEND) {
            view = inflater.inflate(R.layout.item_contact_list_friend, parent, false);
            holder = new FriendHolder(view);
        } else if (viewType == STATUS_FRIEND_CONNECT) {
            view = inflater.inflate(R.layout.item_contact_list_friend_connect, parent, false);
            holder = new ConnectHolder(view);
        } else if (viewType == STATUS_FRIEND_TITLE) {
            view = inflater.inflate(R.layout.item_contact_list_friend_title, parent, false);
            holder = new TitleHolder(view);
        } else if (viewType == STATUS_FRIEND_COUNT) {
            view = inflater.inflate(R.layout.item_contact_list_count, parent, false);
            holder = new CountHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        int type = getItemViewType(position);
        ContactBean currBean = mData.get(position);
        switch (type) {
            case STATUS_FRIEND:
                if(currBean.getStatus() == 7){
                    // 组织架构
                    ((FriendHolder) holder).topTv.setVisibility(View.GONE);
                    ((FriendHolder) holder).lineView.setVisibility(View.VISIBLE);

                    ((FriendHolder) holder).avatar.setImageResource(R.mipmap.department);
                    ((FriendHolder) holder).name.setText(R.string.Chat_Organizational_structure);
                    ((FriendHolder) holder).ouTv.setVisibility(View.GONE);
                }else{
                    String currLetter = contactManage.checkShowFriendTop(currBean, mData.get(position - 1));
                    if (TextUtils.isEmpty(currLetter)) {
                        ((FriendHolder) holder).topTv.setVisibility(View.GONE);
                        ((FriendHolder) holder).lineView.setVisibility(View.GONE);
                    } else {
                        ((FriendHolder) holder).topTv.setVisibility(View.VISIBLE);
                        ((FriendHolder) holder).lineView.setVisibility(View.VISIBLE);
                        ((FriendHolder) holder).topTv.setCompoundDrawables(null, null, null, null);
                        switch (currBean.getStatus()) {
                            case 2: // group
                                Drawable draGroup = activity.getResources().getDrawable(R.mipmap.contract_group_chat3x);
                                draGroup.setBounds(0, 0, draGroup.getMinimumWidth(), draGroup.getMinimumHeight());
                                ((FriendHolder) holder).topTv.setCompoundDrawables(draGroup, null, null, null);
                                ((FriendHolder) holder).topTv.setText(R.string.Link_Group);
                                break;
                            case 4:
                                ((FriendHolder) holder).topTv.setText(currLetter);
                                break;
                            default:
                                break;
                        }
                    }
                    if(currBean.getStatus() == 3 || currBean.getStatus() == 4){
                        ((FriendHolder) holder).ouTv.setVisibility(View.VISIBLE);
                        ((FriendHolder) holder).lineTv.setVisibility(View.VISIBLE);
                        ((FriendHolder) holder).ouTv.setText(currBean.getOu());
                    }else{
                        ((FriendHolder) holder).ouTv.setVisibility(View.GONE);
                        ((FriendHolder) holder).lineTv.setVisibility(View.GONE);
                    }

                    GlideUtil.loadAvatarRound(((FriendHolder) holder).avatar, currBean.getAvatar());
                    ((FriendHolder) holder).name.setText(currBean.getName());
                }
                break;
            case STATUS_FRIEND_CONNECT:
                // 机器人
                String connectLetter = contactManage.checkShowFriendTop(currBean, mData.get(position - 1));
                if (TextUtils.isEmpty(connectLetter)) {
                    ((ConnectHolder) holder).topTv.setVisibility(View.GONE);
                    ((ConnectHolder) holder).lineView.setVisibility(View.GONE);
                } else {
                    ((ConnectHolder) holder).topTv.setVisibility(View.VISIBLE);
                    ((ConnectHolder) holder).lineView.setVisibility(View.VISIBLE);
                    ((ConnectHolder) holder).topTv.setCompoundDrawables(null, null, null, null);
                    ((ConnectHolder) holder).topTv.setText(connectLetter);
                }
                GlideUtil.loadAvatarRound(((ConnectHolder) holder).avatarImg, R.mipmap.connect_logo);
                break;
            case STATUS_FRIEND_COUNT:
                ((CountHolder) holder).bottomCount.setText(currBean.getName());
                break;
            case STATUS_FRIEND_TITLE:
                // 常用联系人
                break;
            default:
                break;
        }

        if(onItemChildListener != null && holder.itemView.findViewById(R.id.content_layout) != null){
            holder.itemView.findViewById(R.id.content_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemChildListener.itemClick(position, mData.get(position));
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        int status = mData.get(position).getStatus();
        if (status == 2 || status == 3 || status == 4 || status == 7) {
            return STATUS_FRIEND;
        } else if(status == 6){
            return STATUS_FRIEND_CONNECT;
        } else if(status == 8){
            return STATUS_FRIEND_TITLE;
        }else {
            return STATUS_FRIEND_COUNT;
        }
    }

    class FriendHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView avatar;
        TextView topTv;
        RelativeLayout contentLayout;
        View lineView;
        TextView ouTv;
        TextView lineTv;

        public FriendHolder(View itemView) {
            super(itemView);
            topTv = (TextView) itemView.findViewById(R.id.top_tv);
            contentLayout = (RelativeLayout) itemView.findViewById(R.id.content_layout);
            avatar = (ImageView) itemView.findViewById(R.id.avatar_rimg);
            name = (TextView) itemView.findViewById(R.id.name_tv);
            lineView = itemView.findViewById(R.id.line_view);
            ouTv = (TextView)itemView.findViewById(R.id.ou_tv);
            lineTv = (TextView)itemView.findViewById(R.id.line_tv);
        }
    }

    class ConnectHolder extends RecyclerView.ViewHolder{

        TextView topTv;
        RelativeLayout contentLayout;
        View lineView;
        ImageView avatarImg;
        TextView nameTv;

        public ConnectHolder(View itemView) {
            super(itemView);
            contentLayout = (RelativeLayout)itemView.findViewById(R.id.content_layout);
            topTv = (TextView)itemView.findViewById(R.id.top_tv);
            lineView = itemView.findViewById(R.id.line_view);
            avatarImg = (ImageView)itemView.findViewById(R.id.avatar_img);
            nameTv = (TextView)itemView.findViewById(R.id.name_tv);
        }
    }

    class CountHolder extends RecyclerView.ViewHolder{

        TextView bottomCount;

        public CountHolder(View itemView) {
            super(itemView);
            bottomCount = (TextView)itemView.findViewById(R.id.friend_count_tv);
        }
    }

    class TitleHolder extends RecyclerView.ViewHolder{

        public TitleHolder(View itemView) {
            super(itemView);
        }
    }


    /**
     * Update the friends list.
     */
    public void updateContact(final String updateType) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                switch (updateType){
                    case updateTypeContact:
                        //listRequest = contactManage.getContactRequest();
                        groupList = contactManage.getGroupData();
                        friendList = contactManage.getFriendList();
                        break;
                    case updateTypeRequest:
                        //listRequest = contactManage.getContactRequest();
                        break;
                    case updateTypeGroup:
                        groupList = contactManage.getGroupData();
                        break;
                    case updateTypeFriend:
                        friendList = contactManage.getFriendList();
                        break;
                    default:
                        break;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                ArrayList<ContactBean> finalList = new ArrayList<>();
                final int friendSize = friendList.size();
                final int groupSize = groupList.size();
                //finalList.addAll(listRequest);
                ContactBean contactBean = new ContactBean();
                contactBean.setStatus(7);
                finalList.add(contactBean);
                if(friendSize > 0){
                    ContactBean contactBean1 = new ContactBean();
                    contactBean1.setStatus(8);
                    finalList.add(contactBean1);
                }
                finalList.addAll(groupList);
                finalList.addAll(friendList);
                startPosition = finalList.size() - friendSize;

                String bottomTxt = "";
                if (friendSize > 0 && groupSize == 0) {
                    bottomTxt = BaseApplication.getInstance().getString(R.string.Link_contact_count, friendSize, "", "");
                } else if (friendSize == 0 && groupSize > 0) {
                    bottomTxt = BaseApplication.getInstance().getString(R.string.Link_group_count, groupSize);
                } else if (friendSize > 0 && groupSize > 0) {
                    bottomTxt = String.format(BaseApplication.getInstance().getString(R.string.Link_contact_count_group_count), friendSize, groupSize);
                }
                setDataNotify(finalList, bottomTxt);
            }
        }.execute();
    }

    public void setDataNotify(List<ContactBean> list, String bottomTxt) {
        mData.clear();
        mData.addAll(list);
        if(!TextUtils.isEmpty(bottomTxt)){
            ContactBean contactBean = new ContactBean();
            contactBean.setName(bottomTxt);
            contactBean.setStatus(5);
            mData.add(contactBean);
        }
        notifyDataSetChanged();
    }

    public void setOnSideMenuListener(OnItemChildListener onItemChildListener) {
        this.onItemChildListener = onItemChildListener;
    }

    public int getPositionForSection(char selectChar) {
        if(mData.size() - startPosition == 0)
            return -1;
        for (int i = startPosition; i < mData.size()-1; i++) {
            ContactBean entity = mData.get(i);
            String showName = entity.getName();
            String firstChar = PinyinUtil.chatToPinyin(showName.charAt(0));
            if (firstChar.charAt(0) == selectChar) {
                return i;
            }
        }
        return -1;
    }

    public interface OnItemChildListener {
        void itemClick(int position, ContactBean entity);
    }
}
