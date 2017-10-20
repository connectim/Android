package connect.activity.wallet.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.utils.data.RateFormatUtil;
import connect.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

import protos.Connect;

/**
 * Created by Administrator on 2016/12/19.
 */
public class RedHistoryAdapter extends RecyclerView.Adapter<RedHistoryAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<Connect.RedPackageInfo> mListData = new ArrayList();

    public RedHistoryAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_wallet_red_history, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Connect.RedPackageInfo redPackageInfo = mListData.get(position);
        Connect.RedPackage redPackage = redPackageInfo.getRedpackage();

        if(redPackage.getTyp() == 1){
            viewHolder.statusTv.setText(R.string.Wallet_Sent_via_link);
        }else{
            if(redPackage.getCategory() == 1){
                viewHolder.statusTv.setText(R.string.Wallet_Sent_to_group);
            }else{
                viewHolder.statusTv.setText(R.string.Wallet_Sent_to_friend);
            }
        }

        viewHolder.moneyTv.setText(RateFormatUtil.longToDoubleBtc(redPackage.getMoney()) + activity.getString(R.string.Set_BTC_symbol));
        viewHolder.timeTv.setText(TimeUtil.getTime(redPackage.getCreatedAt() * 1000,TimeUtil.DATE_FORMAT_MONTH_HOUR));

        if(redPackage.getRemainSize() == 0){
            viewHolder.numberTv.setText(activity.getString(R.string.Wallet_Opened,
                    redPackage.getSize() - redPackage.getRemainSize(),redPackage.getSize()));
        }else if(redPackage.getDeadline() < 0){
            viewHolder.numberTv.setText(activity.getString(R.string.Chat_Expired,
                    redPackage.getSize() - redPackage.getRemainSize(),redPackage.getSize()));
        }else {
            viewHolder.numberTv.setText(activity.getString(R.string.Wallet_PacketSend_Opened) + " "
                    + (redPackage.getSize() - redPackage.getRemainSize()) +"/"+ redPackage.getSize());
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListData!=null&&mListData.size()>=position){
                    itemClickListener.itemClick(mListData.get(position));
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
        return mListData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView statusTv;
        TextView moneyTv;
        TextView timeTv;
        TextView numberTv;

        ViewHolder(View itemview) {
            super(itemview);
            statusTv = (TextView) itemview.findViewById(R.id.status_tv);
            moneyTv = (TextView) itemview.findViewById(R.id.money_tv);
            timeTv = (TextView) itemview.findViewById(R.id.time_tv);
            numberTv = (TextView) itemview.findViewById(R.id.number_tv);
        }
    }

    public void setNotifyData(List list, boolean isClear) {
        if (isClear) {
            mListData.clear();
        }
        mListData.addAll(list);
        notifyDataSetChanged();
    }

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener{
        void itemClick(Connect.RedPackageInfo packageInfo);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
