package connect.activity.workbench.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.ui.activity.R;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import protos.Connect;

/**
 * Created by Administrator on 2018/1/16 0016.
 */

public class VisitorAdapter extends RecyclerView.Adapter<VisitorAdapter.ViewHolder>  {

    private final Activity activity;
    private final int style;
    private ArrayList<Connect.VisitorRecord> mListData = new ArrayList();
    private OnItemClickListener itemClickListener;

    public VisitorAdapter(Activity activity, int style) {
        this.style = style;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_work_visitor, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Connect.VisitorRecord visitorRecord = mListData.get(position);

        holder.nameTv.setText(visitorRecord.getGuestName());
        holder.reasonTv.setText(activity.getString(R.string.Work_Visitors_reason, visitorRecord.getReason()));

        holder.phoneTv.setText(StringUtil.getFormatPhone(visitorRecord.getGuestPhone()));
        String time = TimeUtil.getTime(visitorRecord.getStartTime()*1000, TimeUtil.DATE_FORMAT_MONTH_HOUR) + "——" +
                TimeUtil.getTime(visitorRecord.getEndTime()*1000, TimeUtil.DATE_FORMAT_MONTH_HOUR);
        holder.timeTv.setText(activity.getString(R.string.Work_Visitors_time, time));

        if(style == 1){
            holder.statusTv.setTextColor(activity.getResources().getColor(R.color.color_868686));
            holder.statusTv.setText("(" + activity.getResources().getString(R.string.Work_Visitors_to_audit) + ")");
        }else{
            if(visitorRecord.getPass()){
                holder.statusTv.setTextColor(activity.getResources().getColor(R.color.color_3081EA));
                holder.statusTv.setText("(" + activity.getResources().getString(R.string.Chat_Have_agreed) + ")");
            }else{
                holder.statusTv.setTextColor(activity.getResources().getColor(R.color.color_C72F2F));
                holder.statusTv.setText("(" + activity.getResources().getString(R.string.Chat_Have_refused) + ")");
            }
        }

        holder.contentLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.itemClick(visitorRecord);
            }
        });
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

        LinearLayout contentLinear;
        TextView nameTv;
        TextView reasonTv;
        TextView timeTv;
        TextView phoneTv;
        TextView statusTv;

        ViewHolder(View itemView) {
            super(itemView);
            contentLinear = (LinearLayout) itemView.findViewById(R.id.content_linear);
            nameTv = (TextView) itemView.findViewById(R.id.name_tv);
            reasonTv = (TextView) itemView.findViewById(R.id.reason_tv);
            timeTv = (TextView) itemView.findViewById(R.id.time_tv);
            phoneTv = (TextView) itemView.findViewById(R.id.phone_tv);
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
        void itemClick(Connect.VisitorRecord visitorRecord);

        void callClick(Connect.VisitorRecord visitorRecord);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
