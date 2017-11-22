package connect.activity.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.widget.MaterialBadgeTextView;

/**
 * Created by puin on 17-11-21.
 */
public class UserSubscribeAdapter extends RecyclerView.Adapter<UserSubscribeAdapter.UserSubscribeHolder> {

    public void setData() {

    }

    @Override
    public UserSubscribeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_user_subscribe, parent, false);
        UserSubscribeHolder subscribeHolder = new UserSubscribeHolder(view);
        return subscribeHolder;
    }

    @Override
    public void onBindViewHolder(UserSubscribeHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class UserSubscribeHolder extends RecyclerView.ViewHolder {

        ImageView roundimg;
        MaterialBadgeTextView badgeTextView;
        TextView titleTv;
        TextView contentTv;
        TextView timeTv;

        UserSubscribeHolder(View view) {
            super(view);
            roundimg = (ImageView) view.findViewById(R.id.roundimg);
            badgeTextView = (MaterialBadgeTextView) view.findViewById(R.id.badgetv);
            titleTv = (TextView) view.findViewById(R.id.tv_title);
            contentTv = (TextView) view.findViewById(R.id.textView);
            timeTv = (TextView) view.findViewById(R.id.time_tv);
        }
    }
}
