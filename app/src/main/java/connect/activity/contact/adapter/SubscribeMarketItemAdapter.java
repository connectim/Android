package connect.activity.contact.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import connect.ui.activity.R;

public class SubscribeMarketItemAdapter extends RecyclerView.Adapter<SubscribeMarketItemAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<String> listData = new ArrayList<>();

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
        String name = listData.get(position);
        holder.priceTv.setText(name);

        if(position == 2 || position == 4){
            holder.gainsTv.setText("+ 20%" );
            holder.gainsTv.setBackground(activity.getResources().getDrawable((R.drawable.shape_stroke_red)));
        }else{
            holder.gainsTv.setText("- 20%" );
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

    public void setNotify(ArrayList<String> list){
        listData.clear();
        listData.addAll(list);
        notifyDataSetChanged();
    }

}
