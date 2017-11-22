package connect.activity.contact.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import protos.Connect;

public class SubscribeAdapter extends RecyclerView.Adapter<SubscribeAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<Connect.RSS> listData = new ArrayList<>();
    private OnItemListener onItemListener;

    public SubscribeAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public SubscribeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_contact_list_friend, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(SubscribeAdapter.ViewHolder holder, final int position) {
        final Connect.RSS rss = listData.get(position);
        if(position == 0 || (listData.get(position-1).getSubRss()^rss.getSubRss())){
            holder.topTv.setVisibility(View.VISIBLE);
            holder.lineView.setVisibility(View.GONE);
            if(rss.getSubRss()){
                holder.topTv.setText(R.string.Link_Have_subscribed_to);
            }else{
                holder.topTv.setText(R.string.Link_Not_to_subscribe_to);
            }
        }else{
            holder.topTv.setVisibility(View.GONE);
            holder.lineView.setVisibility(View.VISIBLE);
        }
        GlideUtil.loadAvatarRound(holder.avatar, rss.getIcon());
        holder.name.setText(rss.getTitle());
        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemListener.itemClick(position, rss);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView avatar;
        TextView topTv;
        RelativeLayout contentLayout;
        View lineView;

        public ViewHolder (View itemView) {
            super(itemView);
            topTv = (TextView) itemView.findViewById(R.id.top_tv);
            contentLayout = (RelativeLayout) itemView.findViewById(R.id.content_layout);
            avatar = (ImageView) itemView.findViewById(R.id.avatar_rimg);
            name = (TextView) itemView.findViewById(R.id.name_tv);
            lineView = itemView.findViewById(R.id.line_view);
        }
    }

    public void setNotify(List<Connect.RSS> list){
        listData.clear();
        listData.addAll(list);
        notifyDataSetChanged();
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    public interface OnItemListener {
        void itemClick(int position, Connect.RSS entity);
    }

}
