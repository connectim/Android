package connect.activity.wallet.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import connect.database.green.bean.CurrencyAddressEntity;
import connect.ui.activity.R;

/**
 * Created by Administrator on 2017/7/11.
 */

public class CAddressAdapter extends RecyclerView.Adapter<CAddressAdapter.CAddressHolder> {

    private LayoutInflater inflate;
    private CurrencyAddressEntity addressEntity;
    private List<CurrencyAddressEntity> addressEntities;

    public CAddressAdapter(Activity activity, List<CurrencyAddressEntity> addressEntities) {
        this.inflate = LayoutInflater.from(activity);
        this.addressEntities = addressEntities;
    }

    @Override
    public CAddressHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflate.inflate(R.layout.item_caddress, parent, false);
        CAddressHolder cardHolder = new CAddressHolder(view);
        view.setOnClickListener(itemClickListener);
        return cardHolder;
    }

    @Override
    public void onBindViewHolder(CAddressHolder holder, int position) {
        addressEntity = addressEntities.get(position);
        holder.itemView.setTag(addressEntity);

        holder.addressTv.setText(addressEntity.getAddress());
    }

    @Override
    public int getItemCount() {
        return addressEntities.size();
    }

    private CAddressAdapter.OnItemClickListener itemClickListener;

    public interface OnItemClickListener extends View.OnClickListener{
    }

    public void setItemClickListener(OnItemClickListener clickListener) {
        this.itemClickListener = clickListener;
    }

    class CAddressHolder extends RecyclerView.ViewHolder{

        private TextView addressTv;

        public CAddressHolder(View itemView) {
            super(itemView);
            addressTv= (TextView) itemView.findViewById(R.id.txt1);
        }
    }
}
