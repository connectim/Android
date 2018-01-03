package connect.activity.home.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import connect.ui.activity.R;
import protos.Connect;

/**
 * Created by Administrator on 2018/1/3 0003.
 */

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<ArrayList<Connect.Workmate>> list = new ArrayList<>();
    private OnItemClickListener itemClickListener;

    public CompanyAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public CompanyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_contact_department, parent, false);
        CompanyAdapter.ViewHolder holder = new CompanyAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(CompanyAdapter.ViewHolder holder, final int position) {
        final ArrayList<Connect.Workmate> itemList = list.get(position);
        holder.departmentTv.setText(itemList.get(0).getOU());
        //holder.countTv.setText(itemList.size());
        holder.contentLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.itemClick(itemList);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout contentLin;
        TextView departmentTv;
        TextView countTv;

        public ViewHolder(View itemView) {
            super(itemView);
            contentLin = (LinearLayout)itemView.findViewById(R.id.content_rela);
            departmentTv = (TextView)itemView.findViewById(R.id.department_tv);
            countTv = (TextView)itemView.findViewById(R.id.count_tv);
        }
    }

    public void setNotify(ArrayList<ArrayList<Connect.Workmate>> list){
        if(list.size() > 0){
            this.list.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener{
        void itemClick(ArrayList<Connect.Workmate> itemList);
    }

}
