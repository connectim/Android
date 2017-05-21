package connect.ui.activity.contact.adapter;

import android.content.Context;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.db.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.ui.base.BaseApplication;
import connect.utils.PinyinUtil;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import connect.utils.glide.GlideUtil;
import connect.view.SideScrollView;
import connect.view.roundedimageview.RoundedImageView;

/**
 *
 * Created by Administrator on 2016/12/26.
 */
public class ContactAdapter extends BaseAdapter {

    private ArrayList<ContactEntity> mData = new ArrayList<>();
    private OnItemChildListence onSideMenuListence;
    private int startPosition = 0;
    private String bottomTxt;

    public void setDataNotify(List<ContactEntity> list,String bottomTxt) {
        this.bottomTxt = bottomTxt;
        mData.clear();
        mData.addAll(list);
        if(!bottomTxt.equals("")){
            mData.add(new ContactEntity());
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_friend, parent, false);
            holder = new ViewHolder(convertView);
            holder.contentLayout.getLayoutParams().width = SystemDataUtil.getScreenWidth();
            holder.sideScrollView.setSideScrollListener(sideScrollListener);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position == 0) {
            showRequest(holder,position,parent.getContext());
            return convertView;
        }

        if(mData.size()-1 == position && TextUtils.isEmpty(mData.get(position).getUsername())){
            holder.friendCountTv.setVisibility(View.VISIBLE);
            holder.topTv.setVisibility(View.GONE);
            holder.newFriendLayout.setVisibility(View.GONE);
            holder.sideScrollView.setVisibility(View.GONE);
            holder.friendCountTv.setText(bottomTxt);
            return convertView;
        }

        holder.newFriendLayout.setVisibility(View.GONE);
        holder.friendCountTv.setVisibility(View.GONE);
        holder.sideScrollView.setVisibility(View.VISIBLE);

        ContactEntity entity = mData.get(position);
        ContactEntity lastEntity = mData.get(position - 1);
        if(entity.getSource() != null && entity.getSource() == -1){
            entity.setUsername(parent.getContext().getString(R.string.app_name));
            entity.setPub_key("asdasd");
            entity.setAddress("asdasd");
        }

        String curName = TextUtils.isEmpty(entity.getRemark()) ? entity.getUsername() : entity.getRemark();
        showTop(holder,entity,lastEntity,parent.getContext(),curName);
        if(entity.getSource() != null && entity.getSource() == -1){
            showConnect(holder,position);
            return convertView;
        }

        GlideUtil.loadAvater(holder.avatarRimg,entity.getAvatar());
        holder.nameTv.setText(curName);
        if (onSideMenuListence != null) {
            holder.contentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSideMenuListence.itemClick(position, mData.get(position));
                }
            });
            holder.bottomSetImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSideMenuListence.setFriend(position, mData.get(position));
                }
            });
        }
        return convertView;
    }

    class ViewHolder {
        @Bind(R.id.top_tv)
        TextView topTv;
        @Bind(R.id.bottom_set_img)
        ImageView bottomSetImg;
        @Bind(R.id.avatar_rimg)
        RoundedImageView avatarRimg;
        @Bind(R.id.name_tv)
        TextView nameTv;
        @Bind(R.id.content_layout)
        RelativeLayout contentLayout;
        @Bind(R.id.side_scroll_view)
        SideScrollView sideScrollView;
        @Bind(R.id.new_friend_layout_avater)
        RoundedImageView newFriendLayoutAvater;
        @Bind(R.id.new_friend_name)
        TextView newFriendName;
        @Bind(R.id.tips_tv)
        TextView tipsTv;
        @Bind(R.id.count_tv)
        TextView countTv;
        @Bind(R.id.new_friend_layout)
        RelativeLayout newFriendLayout;
        @Bind(R.id.friend_count_tv)
        TextView friendCountTv;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private void showRequest(ViewHolder holder, final int position,Context context){
        holder.topTv.setVisibility(View.VISIBLE);
        holder.newFriendLayout.setVisibility(View.VISIBLE);
        holder.sideScrollView.setVisibility(View.GONE);
        holder.friendCountTv.setVisibility(View.GONE);
        ContactEntity friendEntity = mData.get(position);

        Drawable drawable = context.getResources().getDrawable(R.mipmap.contract_add_new_friends_small3x);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        holder.topTv.setCompoundDrawables(drawable,null,null,null);
        holder.topTv.setText(R.string.Link_New_Friends);
        if (TextUtils.isEmpty(friendEntity.getAvatar())) {
            holder.newFriendLayout.setBackground(BaseApplication.getInstance()
                    .getResources().getDrawable(R.drawable.selector_list_item_bg));
            holder.newFriendLayoutAvater.setImageResource(R.mipmap.contract_new_friend3x);
            holder.newFriendName.setText(R.string.Link_New_friend);
            holder.tipsTv.setText("");
            holder.countTv.setVisibility(View.GONE);
        } else {
            holder.newFriendLayout.setBackgroundColor(BaseApplication.getInstance()
                    .getResources().getColor(R.color.color_c8ccd5));
            GlideUtil.loadImage(holder.newFriendLayoutAvater, mData.get(position).getAvatar());
            holder.newFriendName.setText(mData.get(position).getUsername());
            holder.tipsTv.setText(mData.get(position).getRemark());
            holder.countTv.setVisibility(View.VISIBLE);
            holder.countTv.setText(String.valueOf(mData.get(position).getSource()));
        }
        if (onSideMenuListence != null) {
            holder.newFriendLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSideMenuListence.itemClick(position, mData.get(position));
                }
            });
        }
    }

    private void showTop(ViewHolder holder, ContactEntity entity, ContactEntity lastEntity, Context context,String curName){
        int curType = getItemType(entity);
        int lastType = getItemType(lastEntity);

        holder.topTv.setCompoundDrawables(null,null,null,null);
        if(curType != lastType){
            holder.topTv.setVisibility(View.VISIBLE);
            Drawable drawable = null;
            switch (curType){
                case 1://Common friends
                    drawable = context.getResources().getDrawable(R.mipmap.contract_favorite13x);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    holder.topTv.setCompoundDrawables(drawable,null,null,null);
                    holder.topTv.setText(R.string.Link_Favorite_Friend);
                    break;
                case 2://group
                    drawable = context.getResources().getDrawable(R.mipmap.contract_group_chat3x);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    holder.topTv.setCompoundDrawables(drawable,null,null,null);
                    holder.topTv.setText(R.string.Link_Group);
                    break;
                case 3://friend
                    break;
            }
        }else{
            holder.topTv.setVisibility(View.GONE);
        }

        if(curType == 3){
            String curFirst = TextUtils.isEmpty(curName) ? "*" : PinyinUtil.chatToPinyin(curName.charAt(0));
            if (curType != lastType) {
                holder.topTv.setVisibility(View.VISIBLE);
                holder.topTv.setText(curFirst);
            }else{
                String lastName = TextUtils.isEmpty(lastEntity.getRemark()) ? lastEntity.getUsername() : lastEntity.getRemark();
                String lastFirst = PinyinUtil.chatToPinyin(lastName.charAt(0));
                if (lastFirst.equals(curFirst)) {
                    holder.topTv.setVisibility(View.GONE);
                } else {
                    holder.topTv.setVisibility(View.VISIBLE);
                    holder.topTv.setText(curFirst);
                }
            }
        }

    }

    private void showConnect(ViewHolder holder, final int position){
        holder.newFriendLayout.setVisibility(View.VISIBLE);
        holder.sideScrollView.setVisibility(View.GONE);
        holder.friendCountTv.setVisibility(View.GONE);

        holder.newFriendLayout.setBackground(BaseApplication.getInstance()
                .getResources().getDrawable(R.drawable.selector_list_item_bg));
        holder.newFriendLayoutAvater.setImageResource(R.mipmap.connect_logo);
        holder.newFriendName.setText("Connect");
        holder.tipsTv.setText("");
        holder.countTv.setVisibility(View.GONE);

        holder.newFriendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSideMenuListence.itemClick(position, mData.get(position));
            }
        });
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
     * item type
     * @param friendEntity
     * @return 0：add friend 1：command friend 2：group 3：friend
     */
    private int getItemType(ContactEntity friendEntity) {
        if (TextUtils.isEmpty(friendEntity.getPub_key())) {
            return 0;
        } else if (friendEntity.getCommon() != null && friendEntity.getCommon()==1) {
            return 1;
        }  else if (TextUtils.isEmpty(friendEntity.getAddress())) {
            return 2;
        } else {
            return 3;
        }
    }

    public int getPositionForSection(char selectchar) {
        if(mData.size() - startPosition == 0)
            return -1;
        for (int i = startPosition; i < mData.size()-1; i++) {
            ContactEntity entity = mData.get(i);
            String showName = TextUtils.isEmpty(entity.getRemark()) ? entity.getUsername() : entity.getRemark();
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
        void itemClick(int position, ContactEntity entity);

        void setFriend(int position, ContactEntity entity);
    }
}
