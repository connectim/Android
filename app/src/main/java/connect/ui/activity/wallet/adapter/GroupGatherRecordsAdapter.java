package connect.ui.activity.wallet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.ui.activity.R;
import connect.utils.data.RateFormatUtil;
import connect.utils.TimeUtil;
import connect.utils.glide.GlideUtil;
import connect.view.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 * Created by pujin on 2017/2/14.
 */
public class GroupGatherRecordsAdapter extends BaseAdapter {

    private ArrayList<Connect.Crowdfunding> mListData = new ArrayList();

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
        GroupGatherRecordsAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_groupgather_records, parent, false);
            viewHolder = new GroupGatherRecordsAdapter.ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GroupGatherRecordsAdapter.ViewHolder) convertView.getTag();
        }
        Connect.Crowdfunding crowdfunding = (Connect.Crowdfunding) getItem(position);

        GlideUtil.loadAvater(viewHolder.avaterRimg, crowdfunding.getSender().getAvatar());
        viewHolder.nameTv.setText(convertView.getContext().getString(R.string.Common_In, crowdfunding.getGroupName()));
        viewHolder.timeTv.setText(TimeUtil.getTime(crowdfunding.getCreatedAt() * 1000, TimeUtil.DATE_FORMAT_MONTH_HOUR));
        viewHolder.balanceTv.setText(parent.getContext().getString(R.string.Set_BTC_symbol) + RateFormatUtil.longToDoubleBtc(crowdfunding.getTotal()));
        if (crowdfunding.getStatus() == 0) {
            viewHolder.statusTv.setText(convertView.getContext().getString(R.string.Chat_Crowd_founding_in_progress));
            viewHolder.statusTv.setTextColor(convertView.getContext().getResources().getColor(R.color.color_6d6e75));
        } else if (crowdfunding.getStatus() == 1) {
            viewHolder.statusTv.setText(convertView.getContext().getString(R.string.Common_Completed));
            viewHolder.statusTv.setTextColor(convertView.getContext().getResources().getColor(R.color.color_37c65c));
        }
        return convertView;
    }

    class ViewHolder {
        @Bind(R.id.avater_rimg)
        RoundedImageView avaterRimg;
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
