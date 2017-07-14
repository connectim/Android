package connect.activity.wallet.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import connect.activity.contact.adapter.FriendRecordAdapter;
import connect.database.MemoryDataManager;
import connect.ui.activity.R;
import connect.utils.data.RateFormatUtil;
import connect.utils.TimeUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.AvatarGridView;
import connect.widget.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import protos.Connect;

/**
 * Created by Administrator on 2016/12/20.
 */
public class TransferOutAdapter extends RecyclerView.Adapter<TransferOutAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<Connect.ExternalBillingInfo> mListData = new ArrayList();

    public TransferOutAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public TransferOutAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_wallet_transaction, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(TransferOutAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.avaterRimg.setVisibility(View.VISIBLE);
        viewHolder.avatarGridview.setVisibility(View.GONE);
        if(TextUtils.isEmpty(mListData.get(position).getReceiverInfo().getAvatar())){
            GlideUtil.loadAvater(viewHolder.avaterRimg, MemoryDataManager.getInstance().getAvatar());
            viewHolder.nameTv.setText(mListData.get(position).getSender());
        }else{
            GlideUtil.loadAvater(viewHolder.avaterRimg,mListData.get(position).getReceiverInfo().getAvatar());
            viewHolder.nameTv.setText(mListData.get(position).getReceiverInfo().getUsername());
        }

        viewHolder.balanceTv.setTextColor(activity.getResources().getColor(R.color.color_f04a5f));
        viewHolder.balanceTv.setText(RateFormatUtil.longToDoubleBtc(mListData.get(position).getAmount()));

        viewHolder.timeTv.setText(TimeUtil.getTime(mListData.get(position).getCreatedAt() * 1000, TimeUtil.DATE_FORMAT_MONTH_HOUR));

        if (mListData.get(position).getReceived()) {
            viewHolder.statusTv.setTextColor(activity.getResources().getColor(R.color.color_767a82));
            viewHolder.statusTv.setText(activity.getString(R.string.Wallet_Confirmed));
        } else if(mListData.get(position).getCancelled()){
            viewHolder.statusTv.setTextColor(activity.getResources().getColor(R.color.color_f04a5f));
            viewHolder.statusTv.setText(activity.getString(R.string.Wallet_Canceled));
        } else {
            viewHolder.statusTv.setTextColor(activity.getResources().getColor(R.color.color_f04a5f));
            viewHolder.statusTv.setText(activity.getString(R.string.Wallet_Unconfirmed));
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
        void itemClick(Connect.ExternalBillingInfo billingInfo);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
