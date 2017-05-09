package connect.ui.activity.set.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.ui.activity.wallet.bean.RateBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/1/12.
 */

public class CurrencyAdapter extends BaseAdapter {

    private ArrayList<RateBean> mDataList = new ArrayList<>();
    private String currency = "";

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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currency, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        RateBean rateBean = mDataList.get(position);
        if(TextUtils.isEmpty(rateBean.getName())){
            viewHolder.text.setText(rateBean.getSymbol() + " " + rateBean.getCode());
        }else{
            viewHolder.text.setText(rateBean.getName());
        }
        if(currency.equals(rateBean.getCode())){
            viewHolder.img.setVisibility(View.VISIBLE);
        }else{
            viewHolder.img.setVisibility(View.GONE);
        }
        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.text)
        TextView text;
        @Bind(R.id.img)
        ImageView img;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
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

}
