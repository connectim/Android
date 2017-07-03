package connect.activity.common.adapter;

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
import connect.activity.home.bean.RoomAttrBean;
import connect.utils.glide.GlideUtil;
import connect.widget.roundedimageview.RoundedImageView;

/**
 * share card
 * Created by Administrator on 2017/2/20.
 */

public class ConversationAdapter extends BaseAdapter {

    private List<RoomAttrBean> roomList = new ArrayList<>();

    @Override
    public int getCount() {
        return roomList.size();
    }

    @Override
    public Object getItem(int position) {
        return roomList.get(position);
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
            holder = (ViewHolder) convertView.getTag();
        }

        RoomAttrBean roomAttrBean = roomList.get(position);
        holder.txt.setVisibility(View.GONE);
        holder.name.setText(roomAttrBean.getName());
        GlideUtil.loadAvater(holder.roundimg,roomAttrBean.getAvatar());

        return convertView;
    }

    public void setDataNotify(List<RoomAttrBean> list) {
        roomList.clear();
        roomList.addAll(list);
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
