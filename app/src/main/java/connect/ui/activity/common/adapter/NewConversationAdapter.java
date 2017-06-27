package connect.ui.activity.common.adapter;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.ui.activity.R;
import connect.ui.activity.contact.model.ContactListManage;
import connect.ui.activity.home.bean.ContactBean;
import connect.utils.PinyinUtil;
import connect.utils.glide.GlideUtil;
import connect.view.roundedimageview.RoundedImageView;

/**
 * Created by Administrator on 2017/2/20.
 */

public class NewConversationAdapter extends BaseAdapter {

    private ArrayList<ContactBean> listData = new ArrayList<>();
    private int startPosition;
    private ContactListManage contactManage = new ContactListManage();

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contactcard, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        ContactBean contactBean = listData.get(position);
        ContactBean lastBean = position == 0 ? null : listData.get(position-1);
        String letter = contactManage.checkShowFriendTop(contactBean,lastBean);
        if(TextUtils.isEmpty(letter)){
            holder.txt.setVisibility(View.GONE);
        }else{
            holder.txt.setVisibility(View.VISIBLE);
            switch (contactBean.getStatus()){
                case 2:
                    Drawable draGroup = parent.getContext().getResources().getDrawable(R.mipmap.contract_group_chat3x);
                    draGroup.setBounds(0, 0, draGroup.getMinimumWidth(), draGroup.getMinimumHeight());
                    holder.txt.setCompoundDrawables(draGroup,null,null,null);
                    holder.txt.setText(R.string.Link_Group);
                    break;
                case 3:
                    Drawable draFavorite = parent.getContext().getResources().getDrawable(R.mipmap.contract_favorite13x);
                    draFavorite.setBounds(0, 0, draFavorite.getMinimumWidth(), draFavorite.getMinimumHeight());
                    holder.txt.setCompoundDrawables(draFavorite,null,null,null);
                    holder.txt.setText(R.string.Link_Favorite_Friend);
                    break;
                case 4:
                    holder.txt.setCompoundDrawables(null,null,null,null);
                    holder.txt.setText(letter);
                    break;
                default:
                    break;
            }
        }
        GlideUtil.loadAvater(holder.roundimg,contactBean.getAvatar());
        holder.name.setText(contactBean.getName());
        return convertView;
    }

    public int getPositionForSection(char selectchar) {
        if(listData.size() - startPosition == 0)
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

    static class ViewHolder {
        @Bind(R.id.txt)
        TextView txt;
        @Bind(R.id.roundimg)
        RoundedImageView roundimg;
        @Bind(R.id.tvName)
        TextView name;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
