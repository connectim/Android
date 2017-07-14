package connect.activity.wallet.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.activity.contact.adapter.FriendRecordAdapter;
import connect.activity.wallet.bean.TransferBean;
import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.widget.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 *
 * Created by Administrator on 2017/1/22.
 */

public class LatelyTransferAdapter extends RecyclerView.Adapter<LatelyTransferAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<TransferBean> mDataList = new ArrayList<>();

    public LatelyTransferAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public LatelyTransferAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_wallet_lately_transaction, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        TransferBean transEntity = mDataList.get(position);
        switch (transEntity.getType()){
            case 1:
                viewHolder.avaterRimg.setBackgroundResource(R.mipmap.luckpacket_record2x);
                viewHolder.nameTv.setText(transEntity.getName());
                break;
            case 2:
                viewHolder.avaterRimg.setBackgroundResource(R.mipmap.bitcoin_luckybag3x);
                viewHolder.nameTv.setText(transEntity.getName());
                break;
            case 3:
                viewHolder.avaterRimg.setBackgroundResource(R.mipmap.bitcoin_luckybag3x);
                viewHolder.nameTv.setText(transEntity.getAddress());
                break;
            case 4:
                GlideUtil.loadAvater(viewHolder.avaterRimg,transEntity.getAvater());
                viewHolder.nameTv.setText(transEntity.getName());
                break;
            case 5:
                viewHolder.avaterRimg.setBackgroundResource(R.mipmap.luckpacket_record2x);
                viewHolder.nameTv.setText(transEntity.getName());
                break;
            default:
                break;
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDataList != null && mDataList.size() >= position) {
                    itemClickListener.itemClick(mDataList.get(position));
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
        return mDataList.size();
    }

    public void setDataNotigy(List<TransferBean> list) {
        if(list != null){
            mDataList.clear();
            mDataList.addAll(list);
            notifyDataSetChanged();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        RoundedImageView avaterRimg;
        TextView nameTv;

        ViewHolder(View itemview) {
            super(itemview);
            avaterRimg = (RoundedImageView) itemview.findViewById(R.id.avater_rimg);
            nameTv = (TextView) itemview.findViewById(R.id.name_tv);
        }
    }

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener{
        void itemClick(TransferBean transferBean);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
