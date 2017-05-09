package connect.ui.activity.contact.adapter;

import android.content.Context;
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
import connect.db.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.PinyinUtil;
import connect.utils.glide.GlideUtil;
import connect.view.roundedimageview.RoundedImageView;

/**
 * Created by Administrator on 2017/2/20.
 */

public class ShareCardContactAdapter extends BaseAdapter {

    private ArrayList<ContactEntity> listData = new ArrayList<>();
    private int startPosition;

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

        ContactEntity entity = listData.get(position);
        ContactEntity lastEntity = null;
        if(position != 0){
            lastEntity = listData.get(position - 1);
        }
        String curName = TextUtils.isEmpty(entity.getRemark()) ? entity.getUsername() : entity.getRemark();
        showTop(holder,entity,lastEntity,parent.getContext(),curName);

        GlideUtil.loadAvater(holder.roundimg,entity.getAvatar());
        holder.name.setText(curName);

        return convertView;
    }

    private void showTop(ViewHolder holder, ContactEntity entity, ContactEntity lastEntity, Context context, String curName){
        int curType = getItemType(entity);
        int lastType = getItemType(lastEntity);

        holder.txt.setCompoundDrawables(null,null,null,null);
        if(curType != lastType){
            holder.txt.setVisibility(View.VISIBLE);
            Drawable drawable = null;
            switch (curType){
                case 1:
                    drawable = context.getResources().getDrawable(R.mipmap.contract_favorite13x);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    holder.txt.setCompoundDrawables(drawable,null,null,null);
                    holder.txt.setText(R.string.Link_Favorite_Friend);
                    break;
                case 2:
                    drawable = context.getResources().getDrawable(R.mipmap.contract_group_chat3x);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    holder.txt.setCompoundDrawables(drawable,null,null,null);
                    holder.txt.setText(R.string.Link_Group);
                    break;
                case 3:
                    break;
            }
        }else{
            holder.txt.setVisibility(View.GONE);
        }

        if(curType == 3){
            String curFirst = PinyinUtil.chatToPinyin(curName.charAt(0));
            if(curType != lastType){
                holder.txt.setVisibility(View.VISIBLE);
                holder.txt.setText(curFirst);
            }else{
                String lastName = TextUtils.isEmpty(lastEntity.getRemark()) ? lastEntity.getUsername() : lastEntity.getRemark();
                String lastFirst = PinyinUtil.chatToPinyin(lastName.charAt(0));
                if (lastFirst.equals(curFirst)) {
                    holder.txt.setVisibility(View.GONE);
                } else {
                    holder.txt.setVisibility(View.VISIBLE);
                    holder.txt.setText(curFirst);
                }
            }
        }

    }

    /**
     *item type
     * @param friendEntity
     * @return 1：command friend 2：group 3：friend
     */
    private int getItemType(ContactEntity friendEntity) {
        if(friendEntity == null)
            return -1;

        if (friendEntity.getCommon() != null && friendEntity.getCommon()==1) {
            return 1;
        }  else if (TextUtils.isEmpty(friendEntity.getAddress())) {
            return 2;
        } else {
            return 3;
        }
    }

    public int getPositionForSection(char selectchar) {
        if(listData.size() - startPosition == 0)
            return -1;
        for (int i = startPosition; i < listData.size(); i++) {
            ContactEntity entity = listData.get(i);
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

    public void setDataNotify(List<ContactEntity> list) {
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
