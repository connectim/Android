package connect.activity.workbench.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import connect.activity.home.adapter.CompanyAdapter;
import connect.ui.activity.R;
import protos.Connect;

/**
 * Created by PuJin on 2018/1/15.
 */

public class WorkSearchAdapter extends RecyclerView.Adapter<WorkSearchAdapter.ViewHolder> {


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_contact_department, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
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
        return 10;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout contentLin;
        TextView departmentTv;
        TextView countTv;

        public ViewHolder(View itemView) {
            super(itemView);

        }
    }
}