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
import connect.utils.system.SystemDataUtil;
import connect.widget.SideScrollView;

/**
 * Contact adapter.
 */
public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private ArrayList<ContactBean> mData = new ArrayList<>();
    private OnItemChildListener onSideMenuListener;
    /** The location of the sideBar started sliding */
    private int startPosition = 0;
    /** Add friend request */
    private final int STATUS_REQUEST = 100;
    /** Friends / group */
    private final int STATUS_FRIEND = 101;
    /** Number of friends */
    private final int STATUS_FRIEND_COUNT = 102;
    /** Connect robot */
    private final int STATUS_FRIEND_CONNECT = 103;

    /** Update the data in the contacts list */
    private ContactListManage contactManage = new ContactListManage();
    private List<ContactBean> listRequest;
    private List<ContactBean> groupList;
    private HashMap<String, List<ContactBean>> friendMap;
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
        View view = null;
        RecyclerView.ViewHolder holder = null;
        if (viewType == STATUS_REQUEST) {
            view = inflater.inflate(R.layout.item_contact_list_request, parent, false);
            holder = new RequestHolder(view);
        } else if (viewType == STATUS_FRIEND) {
            view = inflater.inflate(R.layout.item_contact_list_friend, parent, false);
            holder = new FriendHolder(view);
        } else if (viewType == STATUS_FRIEND_CONNECT) {
            view = inflater.inflate(R.layout.item_contact_list_friend_connect, parent, false);
            holder = new ConnectHolder(view);
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
            case STATUS_REQUEST:
                if (TextUtils.isEmpty(currBean.getAvatar())) {
                    // No friend requests
                    ((RequestHolder) holder).contentLayout.setBackground(BaseApplication.getInstance()
                            .getResources().getDrawable(R.drawable.selector_list_item_bg));
                    ((RequestHolder) holder).avatar.setImageResource(R.mipmap.contract_new_friend3x);
                    ((RequestHolder) holder).name.setText(R.string.Link_New_friend);
                    ((RequestHolder) holder).tips.setText("");
                    ((RequestHolder) holder).count.setVisibility(View.GONE);
                } else {
                    // Have a friend request
                    ((RequestHolder) holder).contentLayout.setBackgroundColor(BaseApplication.getInstance()
                            .getResources().getColor(R.color.color_c8ccd5));
                    GlideUtil.loadAvatarRound(((RequestHolder) holder).avatar, currBean.getAvatar());
                    ((RequestHolder) holder).name.setText(currBean.getName());
                    ((RequestHolder) holder).tips.setText(currBean.getTips());
                    ((RequestHolder) holder).count.setVisibility(View.VISIBLE);
                    ((RequestHolder) holder).count.setText(currBean.getCount() + "");
                }
                break;
            case STATUS_FRIEND:
                String currLetter = contactManage.checkShowFriendTop(currBean, mData.get(position - 1));
                if (TextUtils.isEmpty(currLetter)) {
                    ((FriendHolder) holder).topTv.setVisibility(View.GONE);
                    ((FriendHolder) holder).lineView.setVisibility(View.VISIBLE);
                } else {
                    ((FriendHolder) holder).topTv.setVisibility(View.VISIBLE);
                    ((FriendHolder) holder).lineView.setVisibility(View.GONE);
                    ((FriendHolder) holder).topTv.setCompoundDrawables(null, null, null, null);
                    switch (currBean.getStatus()) {
                        case 2: // group
                            Drawable draGroup = activity.getResources().getDrawable(R.mipmap.contract_group_chat3x);
                            draGroup.setBounds(0, 0, draGroup.getMinimumWidth(), draGroup.getMinimumHeight());
                            ((FriendHolder) holder).topTv.setCompoundDrawables(draGroup, null, null, null);
                            ((FriendHolder) holder).topTv.setText(R.string.Link_Group);
                            break;
                        case 3: // Common friends
                            Drawable draCommon = activity.getResources().getDrawable(R.mipmap.contract_favorite13x);
                            draCommon.setBounds(0, 0, draCommon.getMinimumWidth(), draCommon.getMinimumHeight());
                            ((FriendHolder) holder).topTv.setCompoundDrawables(draCommon, null, null, null);
                            ((FriendHolder) holder).topTv.setText(R.string.Link_Favorite_Friend);
                            break;
                        case 4:
                            ((FriendHolder) holder).topTv.setText(currLetter);
                            break;
                        default:
                            break;
                    }
                }
                GlideUtil.loadAvatarRound(((FriendHolder) holder).avater, currBean.getAvatar());
                ((FriendHolder) holder).name.setText(currBean.getName());
                break;
            case STATUS_FRIEND_CONNECT:
                String connectLetter = contactManage.checkShowFriendTop(currBean, mData.get(position - 1));
                if (TextUtils.isEmpty(connectLetter)) {
                    ((ConnectHolder) holder).topTv.setVisibility(View.GONE);
                    ((ConnectHolder) holder).lineView.setVisibility(View.VISIBLE);
                } else {
                    ((ConnectHolder) holder).topTv.setVisibility(View.VISIBLE);
                    ((ConnectHolder) holder).lineView.setVisibility(View.GONE);
                    ((ConnectHolder) holder).topTv.setCompoundDrawables(null, null, null, null);
                    ((ConnectHolder) holder).topTv.setText(connectLetter);
                }
                break;
            case STATUS_FRIEND_COUNT:
                ((CountHolder) holder).bottomCount.setText(currBean.getName());
                break;
            default:
                break;
        }
        if (onSideMenuListener != null && holder.itemView.findViewById(R.id.content_layout) != null) {
            holder.itemView.findViewById(R.id.content_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSideMenuListener.itemClick(position, mData.get(position));
                }
            });
        }
        if (onSideMenuListener != null && holder.itemView.findViewById(R.id.bottom_set_img) != null) {
            holder.itemView.findViewById(R.id.bottom_set_img) .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSideMenuListener.setFriend(position, mData.get(position));
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        int status = mData.get(position).getStatus();
        if (status == 1) {
            return STATUS_REQUEST;
        } else if (status == 2 || status == 3 || status == 4) {
            return STATUS_FRIEND;
        } else if(status == 6){
            return STATUS_FRIEND_CONNECT;
        } else {
            return STATUS_FRIEND_COUNT;
        }
    }
    class RequestHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView avatar;
        TextView tips;
        TextView count;
        RelativeLayout contentLayout;

        public RequestHolder(View itemView) {
            super(itemView);
            contentLayout = (RelativeLayout) itemView.findViewById(R.id.new_friend_layout);
            avatar = (ImageView) itemView.findViewById(R.id.avatar_img);
            name = (TextView) itemView.findViewById(R.id.name_tv);
            tips = (TextView) itemView.findViewById(R.id.tips_tv);
            count = (TextView) itemView.findViewById(R.id.count_tv);
        }
    }
    class FriendHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView avater;
        TextView topTv;
        SideScrollView sideView;
        RelativeLayout contentLayout;
        ImageView bottomSetImg;
        View lineView;

        public FriendHolder(View itemView) {
            super(itemView);
            topTv = (TextView) itemView.findViewById(R.id.top_tv);
            sideView = (SideScrollView) itemView.findViewById(R.id.side_scroll_view);
            bottomSetImg = (ImageView) itemView.findViewById(R.id.bottom_set_img);
            contentLayout = (RelativeLayout) itemView.findViewById(R.id.content_layout);
            avater = (ImageView) itemView.findViewById(R.id.avatar_rimg);
            name = (TextView) itemView.findViewById(R.id.name_tv);
            contentLayout.getLayoutParams().width = SystemDataUtil.getScreenWidth();
            sideView.setSideScrollListener(sideScrollListener);
            lineView = itemView.findViewById(R.id.line_view);
        }
    }
    class ConnectHolder extends RecyclerView.ViewHolder{

        TextView topTv;
        RelativeLayout contentLayout;
        View lineView;

        public ConnectHolder(View itemView) {
            super(itemView);
            contentLayout = (RelativeLayout)itemView.findViewById(R.id.content_layout);
            topTv = (TextView)itemView.findViewById(R.id.top_tv);
            lineView = itemView.findViewById(R.id.line_view);
        }
    }

    class CountHolder extends RecyclerView.ViewHolder{

        TextView bottomCount;

        public CountHolder(View itemView) {
            super(itemView);
            bottomCount = (TextView)itemView.findViewById(R.id.friend_count_tv);
        }
    }
    public void closeMenu() {
        if (sideScrollView != null) {
            sideScrollView.closeMenu();
            sideScrollView = null;
        }
    }
    public Boolean menuIsOpen() {
        return sideScrollView != null;
    }
    private SideScrollView sideScrollView;
    private SideScrollView.SideScrollListener sideScrollListener = new SideScrollView.SideScrollListener() {

        @Override
        public void onMenuIsOpen(View view) {
            sideScrollView = (SideScrollView) view;
        }

        @Override
        public void onDownOrMove(SideScrollView slidingButtonView) {
            if (menuIsOpen()) {
                if (sideScrollView != slidingButtonView) {
                    closeMenu();
                }
            }
        }
    };

    /**
     * Update the friends list.
     *
     * @param updateType update type
     */
    public void updateContact(final String updateType) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                switch (updateType){
                    case updateTypeContact:
                        listRequest = contactManage.getContactRequest();
                        groupList = contactManage.getGroupData();
                        friendMap = contactManage.getFriendList();
                        break;
                    case updateTypeRequest:
                        listRequest = contactManage.getContactRequest();
                        break;
                    case updateTypeGroup:
                        groupList = contactManage.getGroupData();
                        break;
                    case updateTypeFriend:
                        friendMap = contactManage.getFriendList();
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
                final int friendSize = friendMap.get("friend").size() + friendMap.get("favorite").size();
                final int groupSize = groupList.size();
                finalList.addAll(listRequest);
                finalList.addAll(friendMap.get("favorite"));
                finalList.addAll(groupList);
                finalList.addAll(friendMap.get("friend"));
                startPosition = finalList.size() - friendMap.get("friend").size();

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

    public void setOnSideMenuListener(OnItemChildListener onSideMenuListener) {
        this.onSideMenuListener = onSideMenuListener;
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

        void setFriend(int position, ContactBean entity);
    }
}
