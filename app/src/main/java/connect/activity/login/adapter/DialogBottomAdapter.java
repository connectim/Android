package connect.activity.login.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import connect.activity.contact.adapter.FriendRecordAdapter;
import connect.ui.activity.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import protos.Connect;

/**
 * Created by Administrator on 2016/12/5.
 */
public class DialogBottomAdapter extends RecyclerView.Adapter<DialogBottomAdapter.ViewHolder> {

    private ArrayList<String> list;

    public DialogBottomAdapter(ArrayList<String> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_wallet_transaction, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.titleTv.setText(list.get(position));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(list!=null&&list.size()>=position){
                    itemClickListener.itemClick(position,list.get(position));
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
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleTv;

        ViewHolder(View itemview) {
            super(itemview);
            titleTv = (TextView) itemview.findViewById(R.id.title_tv);
        }
    }

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener{
        void itemClick(int position,String string);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
