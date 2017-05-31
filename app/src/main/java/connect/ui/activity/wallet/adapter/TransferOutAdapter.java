package connect.ui.activity.wallet.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.data.RateFormatUtil;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.glide.GlideUtil;
import connect.view.AvatarGridView;
import connect.view.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import protos.Connect;

/**
 * Created by Administrator on 2016/12/20.
 */
public class TransferOutAdapter extends BaseAdapter {

    private ArrayList<Connect.ExternalBillingInfo> mListData = new ArrayList();

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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_transaction, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.avaterRimg.setVisibility(View.VISIBLE);
        viewHolder.avatarGridview.setVisibility(View.GONE);
        if(TextUtils.isEmpty(mListData.get(position).getReceiverInfo().getAvatar())){
            GlideUtil.loadAvater(viewHolder.avaterRimg, MemoryDataManager.getInstance().getAvatar());
            viewHolder.nameTv.setText(mListData.get(position).getSender());
        }else{
            GlideUtil.loadAvater(viewHolder.avaterRimg,mListData.get(position).getReceiverInfo().getAvatar());
            viewHolder.nameTv.setText(mListData.get(position).getReceiverInfo().getUsername());
        }

        viewHolder.balanceTv.setTextColor(parent.getContext().getResources().getColor(R.color.color_f04a5f));
        viewHolder.balanceTv.setText(RateFormatUtil.longToDoubleBtc(mListData.get(position).getAmount()));

        viewHolder.timeTv.setText(TimeUtil.getTime(mListData.get(position).getCreatedAt() * 1000, TimeUtil.DATE_FORMAT_MONTH_HOUR));

        if (mListData.get(position).getReceived()) {
            viewHolder.statusTv.setTextColor(parent.getContext().getResources().getColor(R.color.color_767a82));
            viewHolder.statusTv.setText(parent.getContext().getString(R.string.Wallet_Confirmed));
        } else if(mListData.get(position).getCancelled()){
            viewHolder.statusTv.setTextColor(parent.getContext().getResources().getColor(R.color.color_f04a5f));
            viewHolder.statusTv.setText(parent.getContext().getString(R.string.Wallet_Canceled));
        } else {
            viewHolder.statusTv.setTextColor(parent.getContext().getResources().getColor(R.color.color_f04a5f));
            viewHolder.statusTv.setText(parent.getContext().getString(R.string.Wallet_Unconfirmed));
        }

        return convertView;
    }

    class ViewHolder {
        @Bind(R.id.avater_rimg)
        RoundedImageView avaterRimg;
        @Bind(R.id.avatar_gridview)
        AvatarGridView avatarGridview;
        @Bind(R.id.left_rela)
        RelativeLayout leftRela;
        @Bind(R.id.name_tv)
        TextView nameTv;
        @Bind(R.id.balance_tv)
        TextView balanceTv;
        @Bind(R.id.time_tv)
        TextView timeTv;
        @Bind(R.id.status_tv)
        TextView statusTv;

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
