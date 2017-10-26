package connect.activity.chat.adapter;

import android.content.Context;
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

/**
 * Created by Administrator on 2017/10/26.
 */

public class CrowdingDetailAdapter extends RecyclerView.Adapter<CrowdingDetailAdapter.ViewHolder>{

    private Context context;
    private List<Connect.CrowdfundingRecord> crowdRecords = new ArrayList<>();

    public CrowdingDetailAdapter(){

    }

    public void setDatas(List<Connect.CrowdfundingRecord> records) {
        this.crowdRecords = records;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_payment, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Connect.CrowdfundingRecord crowdRecord = crowdRecords.get(position);
        Connect.UserInfo userInfo = crowdRecord.getUser();

        GlideUtil.loadAvatarRound(holder.avaterRimg, userInfo.getAvatar());
        holder.nameTv.setText(userInfo.getUsername());

        holder.balanceTv.setText(RateFormatUtil.longToDoubleBtc(crowdRecord.getAmount()) + context.getResources().getString(R.string.Set_BTC_symbol));

        String time = TimeUtil.getTime(crowdRecord.getCreatedAt() * 1000, TimeUtil.DATE_FORMAT_MONTH_HOUR);
        holder.timeTv.setText(time);

        if (crowdRecord.getStatus() == 0) {
            holder.statusTv.setText(context.getString(R.string.Wallet_Waitting_for_pay));
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.color_f04a5f));
        } else if (crowdRecord.getStatus() == 1) {
            holder.statusTv.setText(context.getString(R.string.Wallet_Unconfirmed));
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.color_f04a5f));
        } else {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.color_767a82));
            holder.statusTv.setText(context.getString(R.string.Wallet_Confirmed));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (crowdRecords != null && position < crowdRecords.size()) {
                    itemClickListener.itemClick(crowdRecords.get(position));
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
        return crowdRecords.size();
    }

    private OnItemClickListener itemClickListener;
    public interface OnItemClickListener {
        void itemClick(Connect.CrowdfundingRecord record);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView avaterRimg;
        RelativeLayout leftRela;
        TextView nameTv;
        TextView balanceTv;
        TextView timeTv;
        TextView statusTv;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            avaterRimg = (ImageView) itemView.findViewById(R.id.avater_rimg);
            leftRela = (RelativeLayout) itemView.findViewById(R.id.left_rela);
            nameTv = (TextView) itemView.findViewById(R.id.name_tv);
            balanceTv = (TextView) itemView.findViewById(R.id.balance_tv);
            timeTv = (TextView) itemView.findViewById(R.id.time_tv);
            statusTv = (TextView) itemView.findViewById(R.id.status_tv);
        }
    }
}
