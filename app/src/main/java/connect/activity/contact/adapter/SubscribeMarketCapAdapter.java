package connect.activity.contact.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.ui.activity.R;
import protos.Connect;

/**
 * Created by Administrator on 2017/11/24 0024.
 */

public class SubscribeMarketCapAdapter extends RecyclerView.Adapter<SubscribeMarketCapAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<Connect.Capitalization> listData = new ArrayList<>();

    public SubscribeMarketCapAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public SubscribeMarketCapAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_contact_subscribe_cap, parent, false);
        SubscribeMarketCapAdapter.ViewHolder holder = new SubscribeMarketCapAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(SubscribeMarketCapAdapter.ViewHolder holder, int position) {
        Connect.Capitalization capitalization = listData.get(position);
        holder.nameTv.setText(capitalization.getName());
        holder.volumeTv.setText(capitalization.getMarketCapUsd() + "$");
        holder.priceTv.setText(capitalization.getPriceUsd() + "$");
        holder.rankingTv.setText(capitalization.getRank());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView nameTv;
        private final TextView volumeTv;
        private final TextView priceTv;
        private final TextView rankingTv;

        ViewHolder(View itemView) {
            super(itemView);
            nameTv = (TextView)itemView.findViewById(R.id.name_tv);
            volumeTv = (TextView)itemView.findViewById(R.id.volume_tv);
            priceTv = (TextView)itemView.findViewById(R.id.price_tv);
            rankingTv = (TextView)itemView.findViewById(R.id.ranking_tv);
        }
    }

    public void setNotify(List<Connect.Capitalization> list){
        listData.clear();
        listData.addAll(list);
        notifyDataSetChanged();
    }

}
