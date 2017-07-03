package connect.ui.activity.wallet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.ui.activity.R;
import connect.ui.activity.wallet.bean.TransferBean;
import connect.utils.glide.GlideUtil;
import connect.view.roundedimageview.RoundedImageView;

/**
 *
 * Created by Administrator on 2017/1/22.
 */

public class LatelyTransferAdapter extends BaseAdapter {

    private ArrayList<TransferBean> mDataList = new ArrayList<>();

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_lately_transaction, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

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
        return convertView;
    }

    public void setDataNotigy(List<TransferBean> list) {
        if(list != null){
            mDataList.clear();
            mDataList.addAll(list);
            notifyDataSetChanged();
        }
    }

    static class ViewHolder {
        @Bind(R.id.avater_rimg)
        RoundedImageView avaterRimg;
        @Bind(R.id.name_tv)
        TextView nameTv;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
