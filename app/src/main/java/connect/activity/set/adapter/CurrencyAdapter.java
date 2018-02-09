package connect.activity.set.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import connect.ui.activity.R;
import connect.utils.data.RateBean;

/**
 * Created by Administrator on 2017/1/12.
 */

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<RateBean> mDataList = new ArrayList<>();
    private String currency = "";

    public CurrencyAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_currency, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        RateBean rateBean = mDataList.get(position);
        if (TextUtils.isEmpty(rateBean.getName())) {
            viewHolder.text.setText(rateBean.getSymbol() + " " + rateBean.getCode());
        } else {
            viewHolder.text.setText(rateBean.getName());
        }
        if (rateBean.getCode().equals(currency)) {
            viewHolder.img.setVisibility(View.VISIBLE);
        } else {
            viewHolder.img.setVisibility(View.GONE);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDataList!=null&&mDataList.size()>=position){
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.text)
        TextView text;
        @Bind(R.id.img)
        ImageView img;

        ViewHolder(View itemview) {
            super(itemview);
            text = (TextView) itemview.findViewById(R.id.text);
            img = (ImageView) itemview.findViewById(R.id.img);
        }
    }

    public void setDataNotify(List<RateBean> list) {
        mDataList.clear();
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    public void setSeleCurrency(String value){
        this.currency = value;
    }

    public String getSeleCurrency(){
        return currency;
    }

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener{
        void itemClick(RateBean rateBean);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
