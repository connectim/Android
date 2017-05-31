package connect.ui.activity.contact.adapter;

import android.graphics.drawable.Drawable;
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

import connect.db.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.ui.activity.contact.model.ContactListManage;
import connect.ui.activity.home.bean.ContactBean;
import connect.ui.base.BaseApplication;
import connect.utils.PinyinUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.system.SystemDataUtil;
import connect.view.SideScrollView;
import connect.view.roundedimageview.RoundedImageView;

/**
 *
 * Created by Administrator on 2016/12/26.
 */
public class ContactAdapter extends BaseAdapter {

    private ArrayList<ContactBean> mData = new ArrayList<>();
    private OnItemChildListence onSideMenuListence;
    private int startPosition = 0;
    private final int STATUS_REQUEST = 100;
    private final int STATUS_FRIEND = 101;
    private final int STATUS_FRIEND_COUNT = 102;
    private final int STATUS_FRIEND_CONNECT = 103;
    private ContactListManage contactManage = new ContactListManage();

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
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);
        int tmp = 0;
        if (convertView != null) {
            tmp = (Integer) convertView.getTag(R.id.status_key);
        }
        if (convertView == null || tmp != type) {
            holder = new ViewHolder();
            switch (type) {
                case STATUS_REQUEST:
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_list_request, parent, false);
                    holder.contentLayout = (RelativeLayout) convertView.findViewById(R.id.new_friend_layout);
                    holder.avater = (RoundedImageView)convertView.findViewById(R.id.avatar_img);
                    holder.name = (TextView)convertView.findViewById(R.id.name_tv);
                    holder.tips = (TextView)convertView.findViewById(R.id.tips_tv);
                    holder.count = (TextView)convertView.findViewById(R.id.count_tv);
                    break;
                case STATUS_FRIEND:
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_list_friend, parent, false);
                    holder.topTv = (TextView)convertView.findViewById(R.id.top_tv);
                    holder.sideView = (SideScrollView)convertView.findViewById(R.id.side_scroll_view);
                    holder.bottomSetImg = (ImageView)convertView.findViewById(R.id.bottom_set_img);
                    holder.contentLayout = (RelativeLayout)convertView.findViewById(R.id.content_layout);
                    holder.avater = (RoundedImageView)convertView.findViewById(R.id.avatar_rimg);
                    holder.name = (TextView)convertView.findViewById(R.id.name_tv);

                    holder.contentLayout.getLayoutParams().width = SystemDataUtil.getScreenWidth();
                    holder.sideView.setSideScrollListener(sideScrollListener);
                    break;
                case STATUS_FRIEND_CONNECT:
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_list_friend_connect, parent, false);
                    holder.contentLayout = (RelativeLayout)convertView.findViewById(R.id.content_layout);
                    holder.topTv = (TextView)convertView.findViewById(R.id.top_tv);
                    break;
                case STATUS_FRIEND_COUNT:
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_list_count, parent, false);
                    holder.bottomCount = (TextView)convertView.findViewById(R.id.friend_count_tv);
                    break;
                default:
                    break;
            }
            if (convertView != null) {
                convertView.setTag(holder);
                convertView.setTag(R.id.status_key, type);
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ContactBean currBean = mData.get(position);
        switch (type){
            case STATUS_REQUEST:
                if(TextUtils.isEmpty(currBean.getAddress())){ // No friend requests
                    holder.contentLayout.setBackground(BaseApplication.getInstance()
                            .getResources().getDrawable(R.drawable.selector_list_item_bg));
                    holder.avater.setImageResource(R.mipmap.contract_new_friend3x);
                    holder.name.setText(R.string.Link_New_friend);
                    holder.tips.setText("");
                    holder.count.setVisibility(View.GONE);
                }else{ // Have a friend request
                    holder.contentLayout.setBackgroundColor(BaseApplication.getInstance()
                            .getResources().getColor(R.color.color_c8ccd5));
                    GlideUtil.loadImage(holder.avater, currBean.getAvatar());
                    holder.name.setText(currBean.getName());
                    holder.tips.setText(currBean.getTips());
                    holder.count.setVisibility(View.VISIBLE);
                    holder.count.setText(currBean.getCount());
                }
                break;
            case STATUS_FRIEND:
                String currLetter = contactManage.checkShowFriendTop(currBean,mData.get(position-1));
                if(TextUtils.isEmpty(currLetter)){
                    holder.topTv.setVisibility(View.GONE);
                }else{
                    holder.topTv.setVisibility(View.VISIBLE);
                    holder.topTv.setCompoundDrawables(null,null,null,null);
                    switch (currBean.getStatus()){
                        case 2: // group
                            Drawable draGroup = parent.getContext().getResources().getDrawable(R.mipmap.contract_group_chat3x);
                            draGroup.setBounds(0, 0, draGroup.getMinimumWidth(), draGroup.getMinimumHeight());
                            holder.topTv.setCompoundDrawables(draGroup,null,null,null);
                            holder.topTv.setText(R.string.Link_Group);
                            break;
                        case 3: // Common friends
                            Drawable draCommon = parent.getContext().getResources().getDrawable(R.mipmap.contract_favorite13x);
                            draCommon.setBounds(0, 0, draCommon.getMinimumWidth(), draCommon.getMinimumHeight());
                            holder.topTv.setCompoundDrawables(draCommon,null,null,null);
                            holder.topTv.setText(R.string.Link_Favorite_Friend);
                            break;
                        case 4:
                            holder.topTv.setText(currLetter);
                            break;
                        default:
                            break;
                    }
                }
                GlideUtil.loadAvater(holder.avater,currBean.getAvatar());
                holder.name.setText(currBean.getName());
                break;
            case STATUS_FRIEND_CONNECT:
                String connectLetter = contactManage.checkShowFriendTop(currBean,mData.get(position-1));
                if(TextUtils.isEmpty(connectLetter)){
                    holder.topTv.setVisibility(View.GONE);
                }else{
                    holder.topTv.setVisibility(View.VISIBLE);
                    holder.topTv.setCompoundDrawables(null,null,null,null);
                    holder.topTv.setText(connectLetter);
                }
                break;
            case STATUS_FRIEND_COUNT:
                holder.bottomCount.setText(currBean.getName());
                break;
            default:
                break;
        }
        if (onSideMenuListence != null && holder.contentLayout != null) {
            holder.contentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSideMenuListence.itemClick(position, mData.get(position));
                }
            });
        }
        if (onSideMenuListence != null && holder.bottomSetImg != null) {
            holder.bottomSetImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSideMenuListence.setFriend(position, mData.get(position));
                }
            });
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        RoundedImageView avater;
        TextView tips;
        TextView count;

        TextView topTv;
        SideScrollView sideView;
        RelativeLayout contentLayout;
        ImageView bottomSetImg;
        TextView bottomCount;
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
