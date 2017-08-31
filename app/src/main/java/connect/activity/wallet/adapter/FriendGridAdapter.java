package connect.activity.wallet.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 *
 * Created by Administrator on 2016/12/22.
 */
public class FriendGridAdapter extends BaseAdapter {

    private ArrayList<ContactEntity> mListData = new ArrayList();

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_friend_grid, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(TextUtils.isEmpty(mListData.get(position).getAvatar())){
            viewHolder.avaterRimg.setImageResource(R.mipmap.message_add_friends2x);
        }else{
            GlideUtil.loadAvatarRound(viewHolder.avaterRimg,mListData.get(position).getAvatar());
            viewHolder.nameTv.setText(mListData.get(position).getUsername());
        }

        return convertView;
    }

    public void setNotifyData(List list) {
        mListData.clear();
        mListData.add(new ContactEntity());
        mListData.addAll(list);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        @Bind(R.id.avater_rimg)
        ImageView avaterRimg;
        @Bind(R.id.name_tv)
        TextView nameTv;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
