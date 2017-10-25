package connect.activity.wallet.adapter;

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
import connect.utils.TimeUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import protos.Connect;

public class GroupGatherRecordsAdapter extends RecyclerView.Adapter<GroupGatherRecordsAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<Connect.Crowdfunding> mListData = new ArrayList();

    public GroupGatherRecordsAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_wallet_groupgather_records, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Connect.Crowdfunding crowdfunding = mListData.get(position);

        GlideUtil.loadAvatarRound(viewHolder.avaterRimg, crowdfunding.getSender().getAvatar());
        viewHolder.nameTv.setText(activity.getString(R.string.Common_In, crowdfunding.getGroupName()));
        viewHolder.timeTv.setText(TimeUtil.getTime(crowdfunding.getCreatedAt() * 1000, TimeUtil.DATE_FORMAT_MONTH_HOUR));
        viewHolder.balanceTv.setText(activity.getString(R.string.Set_BTC_symbol) + RateFormatUtil.longToDoubleBtc(crowdfunding.getTotal()));
        if (crowdfunding.getStatus() == 0) {
            viewHolder.statusTv.setText(activity.getString(R.string.Chat_Crowd_founding_in_progress));
            viewHolder.statusTv.setTextColor(activity.getResources().getColor(R.color.color_6d6e75));
        } else if (crowdfunding.getStatus() == 1) {
            viewHolder.statusTv.setText(activity.getString(R.string.Common_Completed));
            viewHolder.statusTv.setTextColor(activity.getResources().getColor(R.color.color_37c65c));
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avaterRimg;
        RelativeLayout leftRela;
        TextView nameTv;
        TextView balanceTv;
        TextView timeTv;
        TextView statusTv;

        ViewHolder(View itemview) {
            super(itemview);
            avaterRimg = (ImageView) itemview.findViewById(R.id.avater_rimg);
            leftRela = (RelativeLayout) itemview.findViewById(R.id.left_rela);
            nameTv = (TextView) itemview.findViewById(R.id.name_tv);
            balanceTv = (TextView) itemview.findViewById(R.id.balance_tv);
            timeTv = (TextView) itemview.findViewById(R.id.time_tv);
            statusTv = (TextView) itemview.findViewById(R.id.status_tv);
        }
    }

    public void setNotifyData(List list, boolean isClear) {
        if (isClear) {
            mListData.clear();
        }
        mListData.addAll(list);
        notifyDataSetChanged();
    }
    private GroupGatherRecordsAdapter.OnItemClickListener itemClickListener;

    public interface OnItemClickListener{
        void itemClick(Connect.Crowdfunding crowdfunding);
    }

    public void setItemClickListener(GroupGatherRecordsAdapter.OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
