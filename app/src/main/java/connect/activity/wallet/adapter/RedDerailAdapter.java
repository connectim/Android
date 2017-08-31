package connect.activity.wallet.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.glide.GlideUtil;
import protos.Connect;

/**
 * Created by Administrator on 2016/12/19.
 */
public class RedDerailAdapter extends RecyclerView.Adapter<RedDerailAdapter.ViewHolder> {

    private Activity activity;
    private Long bestAmount = 0L;
    private List<Connect.GradRedPackageHistroy> mListData = new ArrayList<>();

    public RedDerailAdapter(Activity activity,List<Connect.GradRedPackageHistroy> mListData, long bestAmount) {
        this.activity=activity;
        this.mListData = mListData;
        this.bestAmount = bestAmount;
    }

    @Override
    public RedDerailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_wallet_red_detail, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RedDerailAdapter.ViewHolder viewHolder, final int position) {
        GlideUtil.loadAvatarRound(viewHolder.avaterRimg,mListData.get(position).getUserinfo().getAvatar());
        viewHolder.nameTv.setText(mListData.get(position).getUserinfo().getUsername());
        viewHolder.timeTv.setText(TimeUtil.getTime(mListData.get(position).getCreatedAt() * 1000,TimeUtil.DATE_FORMAT_MONTH_HOUR));
        viewHolder.moneyTv.setText(RateFormatUtil.longToDoubleBtc(mListData.get(position).getAmount()) + " BTC");
        if(bestAmount == mListData.get(position).getAmount()){
            viewHolder.bestTv.setVisibility(View.VISIBLE);
        }else{
            viewHolder.bestTv.setVisibility(View.GONE);
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListData!=null&&position<=mListData.size()){
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
        @Bind(R.id.avater_rimg)
        ImageView avaterRimg;
        @Bind(R.id.name_tv)
        TextView nameTv;
        @Bind(R.id.money_tv)
        TextView moneyTv;
        @Bind(R.id.time_tv)
        TextView timeTv;
        @Bind(R.id.best_tv)
        TextView bestTv;

        ViewHolder(View itemview) {
            super(itemview);
            avaterRimg = (ImageView) itemview.findViewById(R.id.avater_rimg);
            nameTv = (TextView) itemview.findViewById(R.id.name_tv);
            moneyTv = (TextView) itemview.findViewById(R.id.money_tv);
            timeTv = (TextView) itemview.findViewById(R.id.time_tv);
            bestTv = (TextView) itemview.findViewById(R.id.best_tv);
        }
    }

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener{
        void itemClick(Connect.GradRedPackageHistroy histroy);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
