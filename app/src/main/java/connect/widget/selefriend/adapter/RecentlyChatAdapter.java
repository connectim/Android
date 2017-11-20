package connect.widget.selefriend.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import connect.activity.home.bean.RoomAttrBean;
import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;


public class RecentlyChatAdapter extends RecyclerView.Adapter<RecentlyChatAdapter.ViewHolder> {

    private final Activity activity;
    private ArrayList<RoomAttrBean> dataList = new ArrayList<>();
    private OnItemClickListener itemClickListener;

    public RecentlyChatAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public RecentlyChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_contactcard, parent, false);
        RecentlyChatAdapter.ViewHolder holder = new RecentlyChatAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecentlyChatAdapter.ViewHolder holder, int position) {
        final RoomAttrBean roomAttrBean = dataList.get(position);
        holder.txt.setVisibility(View.GONE);
        holder.name.setText(roomAttrBean.getName());
        GlideUtil.loadAvatarRound(holder.avatar, roomAttrBean.getAvatar());
        holder.contentLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.itemClick(roomAttrBean);
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

        TextView txt;
        ImageView avatar;
        TextView name;
        LinearLayout contentLin;

        ViewHolder(View itemView) {
            super(itemView);
            this.txt = (TextView) itemView.findViewById(R.id.txt);
            this.avatar = (ImageView) itemView.findViewById(R.id.roundimg);
            this.name = (TextView) itemView.findViewById(R.id.tvName);
            this.contentLin = (LinearLayout) itemView.findViewById(R.id.content_lin);
        }
    }

    public void setDataNotify(List<RoomAttrBean> list) {
        dataList.clear();
        dataList.addAll(list);
        notifyDataSetChanged();
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener{
        void itemClick(RoomAttrBean attrBean);
    }



}
