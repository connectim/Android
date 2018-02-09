package connect.widget.selefriend.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import connect.activity.contact.model.ContactListManage;
import connect.activity.home.bean.ContactBean;
import connect.ui.activity.R;
import connect.utils.PinyinUtil;
import connect.utils.glide.GlideUtil;


public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private Activity mActivity;
    private ArrayList<ContactBean> dataList = new ArrayList<>();
    private ContactListManage contactManage = new ContactListManage();
    private OnItemClickListener itemClickListener;
    private int startPosition;

    public ContactAdapter(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.item_contactcard, parent, false);
        ContactAdapter.ViewHolder holder = new ContactAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ContactAdapter.ViewHolder holder, final int position) {
        ContactBean contactBean = dataList.get(position);
        ContactBean lastBean = position == 0 ? null : dataList.get(position - 1);
        String letter = contactManage.checkShowFriendTop(contactBean, lastBean);
        if (TextUtils.isEmpty(letter)) {
            holder.txt.setVisibility(View.GONE);
        } else {
            holder.txt.setVisibility(View.VISIBLE);
            switch (contactBean.getStatus()) {
                case 2:
                    Drawable draGroup = mActivity.getResources().getDrawable(R.mipmap.contract_group_chat3x);
                    draGroup.setBounds(0, 0, draGroup.getMinimumWidth(), draGroup.getMinimumHeight());
                    holder.txt.setCompoundDrawables(draGroup, null, null, null);
                    holder.txt.setText(R.string.Link_Group);
                    break;
                case 3:
                    Drawable draFavorite = mActivity.getResources().getDrawable(R.mipmap.contract_favorite13x);
                    draFavorite.setBounds(0, 0, draFavorite.getMinimumWidth(), draFavorite.getMinimumHeight());
                    holder.txt.setCompoundDrawables(draFavorite, null, null, null);
                    holder.txt.setText(R.string.Link_Favorite_Friend);
                    break;
                case 4:
                    holder.txt.setCompoundDrawables(null, null, null, null);
                    holder.txt.setText(letter);
                    break;
                default:
                    break;
            }
        }
        GlideUtil.loadAvatarRound(holder.avatar, contactBean.getAvatar());
        holder.name.setText(contactBean.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataList != null && dataList.size() > position) {
                    ContactBean bean = dataList.get(position);
                    itemClickListener.itemClick(bean);
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.txt)
        TextView txt;
        @Bind(R.id.roundimg)
        ImageView avatar;
        @Bind(R.id.tvName)
        TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            txt = (TextView) itemView.findViewById(R.id.txt);
            avatar = (ImageView) itemView.findViewById(R.id.roundimg);
            name = (TextView) itemView.findViewById(R.id.tvName);
        }
    }

    public int getPositionForSection(char selectChar) {
        if (dataList.size() - startPosition == 0)
            return -1;
        for (int i = startPosition; i < dataList.size(); i++) {
            ContactBean entity = dataList.get(i);
            String showName = entity.getName();
            String firstChar = PinyinUtil.chatToPinyin(showName.charAt(0));
            if (firstChar.charAt(0) == selectChar) {
                return i;
            }
        }
        return -1;
    }

    public void setStartPosition(int count) {
        this.startPosition = count;
    }

    public void setDataNotify(List<ContactBean> list) {
        dataList.clear();
        dataList.addAll(list);
        notifyDataSetChanged();
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener {
        void itemClick(ContactBean contactBean);
    }

}
