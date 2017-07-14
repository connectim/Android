package connect.activity.contact.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.ui.activity.R;
import connect.activity.contact.model.ContactListManage;
import connect.activity.home.bean.ContactBean;
import connect.activity.base.BaseApplication;
import connect.utils.PinyinUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.system.SystemDataUtil;
import connect.widget.SideScrollView;
import connect.widget.roundedimageview.RoundedImageView;

/**
 *
 * Created by Administrator on 2016/12/26.
 */
public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ContactBean> mData = new ArrayList<>();
    private OnItemChildListence onSideMenuListence;
    private int startPosition = 0;
    private final int STATUS_REQUEST = 100;
    private final int STATUS_FRIEND = 101;
    private final int STATUS_FRIEND_COUNT = 102;
    private final int STATUS_FRIEND_CONNECT = 103;
    private ContactListManage contactManage = new ContactListManage();

    private Activity activity;

    public ContactAdapter(Activity activity) {
        this.activity = activity;
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
                if (TextUtils.isEmpty(currBean.getAvatar())) { // No friend requests
                    ((RequestHolder) holder).contentLayout.setBackground(BaseApplication.getInstance()
                            .getResources().getDrawable(R.drawable.selector_list_item_bg));
                    ((RequestHolder) holder).avater.setImageResource(R.mipmap.contract_new_friend3x);
                    ((RequestHolder) holder).name.setText(R.string.Link_New_friend);
                    ((RequestHolder) holder).tips.setText("");
                    ((RequestHolder) holder).count.setVisibility(View.GONE);
                } else { // Have a friend request
                    ((RequestHolder) holder).contentLayout.setBackgroundColor(BaseApplication.getInstance()
                            .getResources().getColor(R.color.color_c8ccd5));
                    GlideUtil.loadImage(((RequestHolder) holder).avater, currBean.getAvatar());
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
                } else {
                    ((FriendHolder) holder).topTv.setVisibility(View.VISIBLE);
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
                GlideUtil.loadAvater(((FriendHolder) holder).avater, currBean.getAvatar());
                ((FriendHolder) holder).name.setText(currBean.getName());
                break;
            case STATUS_FRIEND_CONNECT:
                String connectLetter = contactManage.checkShowFriendTop(currBean, mData.get(position - 1));
                if (TextUtils.isEmpty(connectLetter)) {
                    ((ConnectHolder) holder).topTv.setVisibility(View.GONE);
                } else {
                    ((ConnectHolder) holder).topTv.setVisibility(View.VISIBLE);
                    ((ConnectHolder) holder).topTv.setCompoundDrawables(null, null, null, null);
                    ((ConnectHolder) holder).topTv.setText(connectLetter);
                }
                break;
            case STATUS_FRIEND_COUNT:
                ((CountHolder) holder).bottomCount.setText(currBean.getName());
                break;
        }

        if (onSideMenuListence != null && holder.itemView.findViewById(R.id.content_layout) != null) {
            holder.itemView.findViewById(R.id.content_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSideMenuListence.itemClick(position, mData.get(position));
                }
            });
        }
        if (onSideMenuListence != null && holder.itemView.findViewById(R.id.bottom_set_img) != null) {
            holder.itemView.findViewById(R.id.bottom_set_img) .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSideMenuListence.setFriend(position, mData.get(position));
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
        }else{
            return STATUS_FRIEND_COUNT;
        }
    }

    class RequestHolder extends RecyclerView.ViewHolder {

        TextView name;
        RoundedImageView avater;
        TextView tips;
        TextView count;
        RelativeLayout contentLayout;

        public RequestHolder(View itemView) {
            super(itemView);
            contentLayout = (RelativeLayout) itemView.findViewById(R.id.new_friend_layout);
            avater = (RoundedImageView) itemView.findViewById(R.id.avatar_img);
            name = (TextView) itemView.findViewById(R.id.name_tv);
            tips = (TextView) itemView.findViewById(R.id.tips_tv);
            count = (TextView) itemView.findViewById(R.id.count_tv);
        }
    }

    class FriendHolder extends RecyclerView.ViewHolder {

        TextView name;
        RoundedImageView avater;
        TextView topTv;
        SideScrollView sideView;
        RelativeLayout contentLayout;
        ImageView bottomSetImg;

        public FriendHolder(View itemView) {
            super(itemView);
            topTv = (TextView) itemView.findViewById(R.id.top_tv);
            sideView = (SideScrollView) itemView.findViewById(R.id.side_scroll_view);
            bottomSetImg = (ImageView) itemView.findViewById(R.id.bottom_set_img);
            contentLayout = (RelativeLayout) itemView.findViewById(R.id.content_layout);
            avater = (RoundedImageView) itemView.findViewById(R.id.avatar_rimg);
            name = (TextView) itemView.findViewById(R.id.name_tv);
            contentLayout.getLayoutParams().width = SystemDataUtil.getScreenWidth();
            sideView.setSideScrollListener(sideScrollListener);
        }
    }

    class ConnectHolder extends RecyclerView.ViewHolder{

        TextView topTv;
        RelativeLayout contentLayout;

        public ConnectHolder(View itemView) {
            super(itemView);
            contentLayout = (RelativeLayout)itemView.findViewById(R.id.content_layout);
            topTv = (TextView)itemView.findViewById(R.id.top_tv);
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

    public int getPositionForSection(char selectchar) {
        if(mData.size() - startPosition == 0)
            return -1;
        for (int i = startPosition; i < mData.size()-1; i++) {
            ContactBean entity = mData.get(i);
            String showName = entity.getName();
            String firstChar = PinyinUtil.chatToPinyin(showName.charAt(0));
            if (firstChar.charAt(0) == selectchar) {
                return i;
            }
        }
        return -1;
    }

    public void setStartPosition(int count) {
        this.startPosition = count;
    }

    public void setOnSideMenuListence(OnItemChildListence onSideMenuListence) {
        this.onSideMenuListence = onSideMenuListence;
    }

    public interface OnItemChildListence {
        void itemClick(int position, ContactBean entity);

        void setFriend(int position, ContactBean entity);
    }
}
