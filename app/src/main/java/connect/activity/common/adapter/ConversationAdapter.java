package connect.activity.common.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.contact.adapter.FriendRecordAdapter;
import connect.ui.activity.R;
import connect.activity.home.bean.RoomAttrBean;
import connect.utils.glide.GlideUtil;
import connect.widget.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 * share card
 * Created by Administrator on 2017/2/20.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private Activity activity;
    private List<RoomAttrBean> roomList = new ArrayList<>();

    public ConversationAdapter(Activity activity) {
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
        final RoomAttrBean roomAttrBean = roomList.get(position);
        holder.txt.setVisibility(View.GONE);
        holder.name.setText(roomAttrBean.getName());
        GlideUtil.loadAvater(holder.roundimg,roomAttrBean.getAvatar());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (roomList != null && roomList.size() > position) {
                    RoomAttrBean attrBean = roomList.get(position);
                    itemClickListener.itemClick(attrBean);
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
        return roomList.size();
    }

    public void setDataNotify(List<RoomAttrBean> list) {
        roomList.clear();
        roomList.addAll(list);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txt;
        RoundedImageView roundimg;
        TextView name;

        ViewHolder(View itemview) {
            super(itemview);
            this.txt = (TextView) itemview.findViewById(R.id.txt);
            this.roundimg = (RoundedImageView) itemview.findViewById(R.id.roundimg);
            this.name = (TextView) itemview.findViewById(R.id.tvName);

        }
    }

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener{
        void itemClick(RoomAttrBean attrBean);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
