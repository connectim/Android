package connect.activity.workbench.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.ui.activity.R;
import connect.utils.TimeUtil;
import protos.Connect;

/**
 * Created by Administrator on 2018/2/5 0005.
 */

public class WarehouseAdapter extends RecyclerView.Adapter<WarehouseAdapter.ViewHolder> {

    private final Activity activity;
    private ArrayList<Connect.StaffLog> mListData = new ArrayList();
    private OnItemClickListener itemClickListener;

    public WarehouseAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_work_warehouse, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Connect.StaffLog staffLog = mListData.get(position);
        holder.timeTv.setText(activity.getString(R.string.Work_Time, TimeUtil.getTime(staffLog.getDateTime()*1000, TimeUtil.DEFAULT_DATE_FORMAT)));
        holder.typeTv.setText(activity.getString(R.string.Work_Entering_the_warehouse,
                TextUtils.isEmpty(staffLog.getLocation()) ? staffLog.getDeviceId() : staffLog.getLocation()));

        if(staffLog.getStatus() == 1){
            holder.statusTv.setText(R.string.Wallet_Confirmed);
            holder.statusTv.setTextColor(activity.getResources().getColor(R.color.color_3081EA));
        }else{
            holder.statusTv.setText(R.string.Wallet_Unconfirmed);
            holder.statusTv.setTextColor(activity.getResources().getColor(R.color.color_868686));
        }
        holder.contentLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.itemClick(staffLog);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout contentLinear;
        TextView typeTv;
        TextView timeTv;
        TextView statusTv;

        ViewHolder(View itemView) {
            super(itemView);
            contentLinear = (LinearLayout) itemView.findViewById(R.id.content_linear);
            typeTv = (TextView) itemView.findViewById(R.id.type_tv);
            timeTv = (TextView) itemView.findViewById(R.id.time_tv);
            statusTv = (TextView) itemView.findViewById(R.id.status_tv);
        }
    }

    public void setNotifyData(List list, boolean isClear) {
        if (isClear) {
            mListData.clear();
        }
        mListData.addAll(list);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{
        void itemClick(Connect.StaffLog visitorRecord);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
