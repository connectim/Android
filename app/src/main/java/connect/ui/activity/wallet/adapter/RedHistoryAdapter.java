package connect.ui.activity.wallet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.utils.data.RateFormatUtil;
import connect.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import protos.Connect;

/**
 * Created by Administrator on 2016/12/19.
 */
public class RedHistoryAdapter extends BaseAdapter {

    private ArrayList<Connect.RedPackageInfo> mListData = new ArrayList();

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_red_history, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Connect.RedPackageInfo redPackageInfo = (Connect.RedPackageInfo)getItem(position);
        Connect.RedPackage redPackage = redPackageInfo.getRedpackage();

        if(redPackage.getTyp() == 1){
            viewHolder.statusTv.setText(R.string.Wallet_Sent_via_link);
        }else{
            if(redPackage.getCategory() == 2){
                viewHolder.statusTv.setText(R.string.Wallet_Sent_to_group);
            }else{
                viewHolder.statusTv.setText(R.string.Wallet_Sent_to_friend);
            }
        }

        viewHolder.moneyTv.setText(RateFormatUtil.longToDoubleBtc(redPackage.getMoney()) + parent.getContext().getString(R.string.Set_BTC_symbol));
        viewHolder.timeTv.setText(TimeUtil.getTime(redPackage.getCreatedAt() * 1000,TimeUtil.DATE_FORMAT_MONTH_HOUR));

        if(redPackage.getRemainSize() == 0){
            viewHolder.numberTv.setText(parent.getContext().getString(R.string.Wallet_Opened,
                    redPackage.getSize() - redPackage.getRemainSize(),redPackage.getSize()));
        }else if(redPackage.getDeadline() < 0){
            viewHolder.numberTv.setText(parent.getContext().getString(R.string.Chat_Expired,
                    redPackage.getSize() - redPackage.getRemainSize(),redPackage.getSize()));
        }else {
            viewHolder.numberTv.setText(parent.getContext().getString(R.string.Wallet_PacketSend_Opened) + " "
                    + (redPackage.getSize() - redPackage.getRemainSize()) +"/"+ redPackage.getSize());
        }

        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.status_tv)
        TextView statusTv;
        @Bind(R.id.money_tv)
        TextView moneyTv;
        @Bind(R.id.time_tv)
        TextView timeTv;
        @Bind(R.id.number_tv)
        TextView numberTv;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public void setNotifyData(List list, boolean isClear) {
        if (isClear) {
            mListData.clear();
        }
        mListData.addAll(list);
        notifyDataSetChanged();
    }


}
