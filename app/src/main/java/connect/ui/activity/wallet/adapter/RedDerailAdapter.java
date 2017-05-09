package connect.ui.activity.wallet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import connect.ui.activity.R;
import connect.utils.data.RateFormatUtil;
import connect.utils.TimeUtil;
import connect.utils.glide.GlideUtil;
import connect.view.roundedimageview.RoundedImageView;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import protos.Connect;

/**
 * Created by Administrator on 2016/12/19.
 */
public class RedDerailAdapter extends BaseAdapter {

    private final Long bestAmount;
    private List<Connect.GradRedPackageHistroy> mListData = new ArrayList<>();

    public RedDerailAdapter(List<Connect.GradRedPackageHistroy> mListData,Long bestAmount) {
        this.mListData = mListData;
        this.bestAmount = bestAmount;
    }

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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_red_detail, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        GlideUtil.loadAvater(viewHolder.avaterRimg,mListData.get(position).getUserinfo().getAvatar());
        viewHolder.nameTv.setText(mListData.get(position).getUserinfo().getUsername());
        viewHolder.timeTv.setText(TimeUtil.getTime(mListData.get(position).getCreatedAt() * 1000,TimeUtil.DATE_FORMAT_MONTH_HOUR));
        viewHolder.moneyTv.setText(RateFormatUtil.longToDoubleBtc(mListData.get(position).getAmount()) + " BTC");
        if(bestAmount == mListData.get(position).getAmount()){
            viewHolder.bestTv.setVisibility(View.VISIBLE);
        }else{
            viewHolder.bestTv.setVisibility(View.GONE);
        }
        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.avater_rimg)
        RoundedImageView avaterRimg;
        @Bind(R.id.name_tv)
        TextView nameTv;
        @Bind(R.id.money_tv)
        TextView moneyTv;
        @Bind(R.id.time_tv)
        TextView timeTv;
        @Bind(R.id.best_tv)
        TextView bestTv;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
