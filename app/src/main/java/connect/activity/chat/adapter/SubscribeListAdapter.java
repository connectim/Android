package connect.activity.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import connect.activity.chat.subscribe.SubscribeListActivity;
import connect.activity.chat.subscribe.SubscribeMessageActivity;
import connect.database.green.bean.SubscribeConversationEntity;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.MaterialBadgeTextView;

/**
 * Created by puin on 17-11-21.
 */
public class SubscribeListAdapter extends RecyclerView.Adapter<SubscribeListAdapter.SubscribeHolder> {

    private List<SubscribeConversationEntity> subscribeEntities;
    private SubscribeListActivity activity;

    public SubscribeListAdapter(SubscribeListActivity activity) {
        this.activity = activity;
    }

    public void setData(List<SubscribeConversationEntity> subscribeEntities) {
        this.subscribeEntities = subscribeEntities;
        notifyDataSetChanged();
    }

    @Override
    public SubscribeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_list_subscribe, parent, false);
        SubscribeHolder subscribeHolder = new SubscribeHolder(view);
        return subscribeHolder;
    }

    @Override
    public void onBindViewHolder(SubscribeHolder holder, int position) {
        final SubscribeConversationEntity conversationEntity = subscribeEntities.get(position);

        GlideUtil.loadImage(holder.roundimg, conversationEntity.getIcon());
        String titleTxt = TextUtils.isEmpty(conversationEntity.getTitle()) ?
                "" : conversationEntity.getTitle();
        holder.titleTv.setText(titleTxt);

        String content = TextUtils.isEmpty(conversationEntity.getContent()) ?
                "" : conversationEntity.getContent();
        holder.contentTv.setText(content);
        holder.timeTv.setText(TimeUtil.getTime(conversationEntity.getTime(),TimeUtil.DATE_FORMAT_MONTH_HOUR));
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                long rssId = conversationEntity.getRssId();
                SubscribeMessageActivity.startActivity(activity, rssId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subscribeEntities.size();
    }

    static class SubscribeHolder extends RecyclerView.ViewHolder {

        ImageView roundimg;
        MaterialBadgeTextView badgeTextView;
        TextView titleTv;
        TextView contentTv;
        TextView timeTv;

        SubscribeHolder(View view) {
            super(view);
            roundimg = (ImageView) view.findViewById(R.id.roundimg);
            badgeTextView = (MaterialBadgeTextView) view.findViewById(R.id.badgetv);
            titleTv = (TextView) view.findViewById(R.id.tv_title);
            contentTv = (TextView) view.findViewById(R.id.textView);
            timeTv = (TextView) view.findViewById(R.id.time_tv);
        }
    }
}
