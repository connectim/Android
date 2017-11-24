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

public class SubscribeMarketItemAdapter extends RecyclerView.Adapter<SubscribeMarketItemAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<Connect.Ticker> listData = new ArrayList<>();

    public SubscribeMarketItemAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public SubscribeMarketItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_contact_subscribe, parent, false);
        SubscribeMarketItemAdapter.ViewHolder holder = new SubscribeMarketItemAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(SubscribeMarketItemAdapter.ViewHolder holder, int position) {
        Connect.Ticker ticker = listData.get(position);
        holder.nameTv.setText(ticker.getSymbol());
        holder.volumeTv.setText(ticker.getVolume() + "(24h)");
        holder.priceTv.setText(ticker.getLast());
        if(ticker.getChangePercentage().contains("-")){
            holder.gainsTv.setText(ticker.getChangePercentage());
            holder.gainsTv.setBackground(activity.getResources().getDrawable((R.drawable.shape_stroke_red)));
        }else{
            holder.gainsTv.setText(ticker.getChangePercentage());
            holder.gainsTv.setBackground(activity.getResources().getDrawable(R.drawable.shape_stroke_green));
        }

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView nameTv;
        private final TextView volumeTv;
        private final TextView priceTv;
        private final TextView gainsTv;

        ViewHolder(View itemView) {
            super(itemView);
            nameTv = (TextView)itemView.findViewById(R.id.name_tv);
            volumeTv = (TextView)itemView.findViewById(R.id.volume_tv);
            priceTv = (TextView)itemView.findViewById(R.id.price_tv);
            gainsTv = (TextView)itemView.findViewById(R.id.gains_tv);
        }
    }

    public void setNotify(List<Connect.Ticker> list){
        listData.clear();
        listData.addAll(list);
        notifyDataSetChanged();
    }

}
