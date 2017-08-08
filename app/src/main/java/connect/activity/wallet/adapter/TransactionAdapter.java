package connect.activity.wallet.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.activity.contact.adapter.FriendRecordAdapter;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import connect.utils.data.RateFormatUtil;
import connect.widget.AvatarGridView;
import connect.widget.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 * Created by Administrator on 2016/12/14.
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<Connect.Transaction> mListData = new ArrayList();

    public TransactionAdapter(Activity activity){
        this.activity=activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_wallet_transaction, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        int typeTx = mListData.get(position).getTxType();
        viewHolder.avatarGridview.setVisibility(View.GONE);
        viewHolder.avaterRimg.setVisibility(View.GONE);
        Connect.Transaction transaction = mListData.get(position);
        switch (typeTx){
            case 0:// Address transfer
            case 6://outer transfer
                viewHolder.avaterRimg.setVisibility(View.VISIBLE);
                viewHolder.avaterRimg.setImageResource(R.mipmap.bitcoin_luckybag3x);
                break;
            case 1://Common transfer and payment
            case 2://gather
            case 8://gather
                List<Connect.UserInfoBalance> list = mListData.get(position).getUserInfosList();
                if(list.size() > 0){
                    ArrayList arrayList = new ArrayList<String>();
                    for(Connect.UserInfoBalance userInfoBalance : list){
                        arrayList.add(userInfoBalance.getAvatar());
                    }
                    viewHolder.avatarGridview.setVisibility(View.VISIBLE);
                    viewHolder.avatarGridview.setAvaterData(arrayList);
                }else{
                    viewHolder.avaterRimg.setVisibility(View.VISIBLE);
                    viewHolder.avaterRimg.setImageResource(R.mipmap.bitcoin_luckybag3x);
                }
                break;
            case 3://lucky packet
            case 4:
            case 5:
            case 7://system lucky packet
                viewHolder.avaterRimg.setVisibility(View.VISIBLE);
                viewHolder.avaterRimg.setImageResource(R.mipmap.luckpacket_record2x);
                break;
            default:
                break;
        }
        if(typeTx == 7) {
            viewHolder.nameTv.setText(R.string.Wallet_From_Connect_team);
        }else if (transaction.getUserInfosList().size() > 0) {
            if(TextUtils.isEmpty(transaction.getUserInfos(0).getUsername())){
                viewHolder.nameTv.setText(mListData.get(position).getUserInfos(0).getAddress());
            }else{
                viewHolder.nameTv.setText(mListData.get(position).getUserInfos(0).getUsername());
            }
        } else if(transaction.getOutputsList().size() > 0){
            viewHolder.nameTv.setText(transaction.getOutputs(0).getAddresses(0));
        } else {
            viewHolder.nameTv.setText("me");
        }

        if (mListData.get(position).getBalance() > 0) {
            viewHolder.balanceTv.setTextColor(activity.getResources().getColor(R.color.color_00c400));
            viewHolder.balanceTv.setText("+" + RateFormatUtil.longToDoubleBtc(mListData.get(position).getBalance()));
        } else {
            viewHolder.balanceTv.setTextColor(activity.getResources().getColor(R.color.color_f04a5f));
            viewHolder.balanceTv.setText("" + RateFormatUtil.longToDoubleBtc(mListData.get(position).getBalance()));
        }

        viewHolder.timeTv.setText(TimeUtil.getTime(mListData.get(position).getCreatedAt() * 1000, TimeUtil.DATE_FORMAT_MONTH_HOUR));

        if (mListData.get(position).getConfirmations() > 0) {//confirm
            viewHolder.statusTv.setTextColor(activity.getResources().getColor(R.color.color_767a82));
            viewHolder.statusTv.setText(activity.getString(R.string.Wallet_Confirmed));
        } else {
            viewHolder.statusTv.setTextColor(activity.getResources().getColor(R.color.color_f04a5f));
            viewHolder.statusTv.setText(activity.getString(R.string.Wallet_Unconfirmed));
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListData != null && mListData.size() >= position) {
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


    class ViewHolder extends RecyclerView.ViewHolder {

        RoundedImageView avaterRimg;
        AvatarGridView avatarGridview;
        RelativeLayout leftRela;
        TextView nameTv;
        TextView balanceTv;
        TextView timeTv;
        TextView statusTv;

        ViewHolder(View itemview) {
            super(itemview);
            avaterRimg = (RoundedImageView) itemview.findViewById(R.id.avater_rimg);
            avatarGridview = (AvatarGridView) itemview.findViewById(R.id.avatar_gridview);
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

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener{
        void itemClick(Connect.Transaction transaction);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
