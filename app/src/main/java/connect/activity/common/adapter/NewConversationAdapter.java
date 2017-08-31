package connect.activity.common.adapter;

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

/**
 * Created by Administrator on 2017/2/20.
 */

public class NewConversationAdapter extends RecyclerView.Adapter<NewConversationAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<ContactBean> listData = new ArrayList<>();
    private int startPosition;
    private ContactListManage contactManage = new ContactListManage();

    public NewConversationAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_contactcard, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        ContactBean contactBean = listData.get(position);
        ContactBean lastBean = position == 0 ? null : listData.get(position - 1);
        String letter = contactManage.checkShowFriendTop(contactBean, lastBean);
        if (TextUtils.isEmpty(letter)) {
            holder.txt.setVisibility(View.GONE);
        } else {
            holder.txt.setVisibility(View.VISIBLE);
            switch (contactBean.getStatus()) {
                case 2:
                    Drawable draGroup = activity.getResources().getDrawable(R.mipmap.contract_group_chat3x);
                    draGroup.setBounds(0, 0, draGroup.getMinimumWidth(), draGroup.getMinimumHeight());
                    holder.txt.setCompoundDrawables(draGroup, null, null, null);
                    holder.txt.setText(R.string.Link_Group);
                    break;
                case 3:
                    Drawable draFavorite = activity.getResources().getDrawable(R.mipmap.contract_favorite13x);
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
        GlideUtil.loadAvatarRound(holder.roundimg, contactBean.getAvatar());
        holder.name.setText(contactBean.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listData != null && listData.size() > position) {
                    ContactBean bean = listData.get(position);
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
        return listData.size();
    }

    public int getPositionForSection(char selectchar) {
        if (listData.size() - startPosition == 0)
            return -1;
        for (int i = startPosition; i < listData.size(); i++) {
            ContactBean entity = listData.get(i);
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

    public void setDataNotify(List<ContactBean> list) {
        listData.clear();
        listData.addAll(list);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.txt)
        TextView txt;
        @Bind(R.id.roundimg)
        ImageView roundimg;
        @Bind(R.id.tvName)
        TextView name;

        ViewHolder(View itemview) {
            super(itemview);
            txt = (TextView) itemview.findViewById(R.id.txt);
            roundimg = (ImageView) itemview.findViewById(R.id.roundimg);
            name = (TextView) itemview.findViewById(R.id.tvName);
        }
    }

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void itemClick(ContactBean contactBean);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
