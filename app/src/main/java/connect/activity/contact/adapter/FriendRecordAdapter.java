package connect.activity.contact.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import protos.Connect;

public class FriendRecordAdapter extends RecyclerView.Adapter<FriendRecordAdapter.ViewHolder> {


    private Activity activity;
    private ArrayList<Connect.FriendBill> mListData = new ArrayList();
    private ContactEntity friendEntity;

    public FriendRecordAdapter(Activity activity, ContactEntity friendEntity) {
        this.activity = activity;
        this.friendEntity = friendEntity;
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
        final Connect.FriendBill friendBill = mListData.get(position);
        viewHolder.avaterRimg.setVisibility(View.VISIBLE);

        GlideUtil.loadAvatarRound(viewHolder.avaterRimg, friendEntity.getAvatar());
        String curName = friendEntity.getName();
        viewHolder.nameTv.setText(curName);
        viewHolder.timeTv.setText(TimeUtil.getTime(friendBill.getCreatedAt() * 1000, TimeUtil.DATE_FORMAT_MONTH_HOUR));

        if (friendBill.getAmount() > 0) {
            viewHolder.balanceTv.setTextColor(activity.getResources().getColor(R.color.color_00c400));
            viewHolder.balanceTv.setText("+" + RateFormatUtil.longToDoubleBtc(friendBill.getAmount()));
        } else {
            viewHolder.balanceTv.setTextColor(activity.getResources().getColor(R.color.color_f04a5f));
            viewHolder.balanceTv.setText("" + RateFormatUtil.longToDoubleBtc(friendBill.getAmount()));
        }

        if (friendBill.getStatus() == 1) {
            viewHolder.statusTv.setTextColor(activity.getResources().getColor(R.color.color_767a82));
            viewHolder.statusTv.setText(activity.getString(R.string.Wallet_Confirmed));
        } else {
            viewHolder.statusTv.setTextColor(activity.getResources().getColor(R.color.color_f04a5f));
            viewHolder.statusTv.setText(activity.getString(R.string.Wallet_Unconfirmed));
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListData != null && mListData.size() > position) {
                    Connect.FriendBill friendBill1 = mListData.get(position);
                    itemClickListener.itemClick(friendBill1);
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

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener{
        void itemClick(Connect.FriendBill friendBill);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
