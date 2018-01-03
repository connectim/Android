package connect.activity.company.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import connect.activity.base.BaseApplication;
import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import protos.Connect;

/**
 * Created by Administrator on 2018/1/3 0003.
 */

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<Connect.Workmate> list;
    private OnItemClickListener itemClickListener;

    public DepartmentAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public DepartmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_company_contact, parent, false);
        DepartmentAdapter.ViewHolder holder = new DepartmentAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(DepartmentAdapter.ViewHolder holder, final int position) {
        final Connect.Workmate workmate = list.get(position);
        GlideUtil.loadImage(holder.avater, workmate.getAvatar());
        holder.nameTvS.setText(workmate.getName());
        holder.nicName.setText(workmate.getOU());
        if(workmate.getRegisted()){
            holder.addBtn.setText(R.string.Link_Add);
            holder.addBtn.setEnabled(true);
            holder.addBtn.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_ffffff));
            holder.addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemClickListener != null){
                        itemClickListener.addFriend(position, workmate);
                    }
                }
            });
        }else{
            holder.addBtn.setText(R.string.Link_Not_logged_in);
            holder.addBtn.setEnabled(false);
            holder.addBtn.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_858998));
        }
        holder.contentLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(workmate.getRegisted()){
                    itemClickListener.itemClick(workmate);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout contentLin;
        ImageView avater;
        TextView nameTvS;
        TextView nicName;
        Button addBtn;
        public ViewHolder(View itemView) {
            super(itemView);
            contentLin = (LinearLayout)itemView.findViewById(R.id.content_rela);
            avater = (ImageView)itemView.findViewById(R.id.avatar_rimg);
            nameTvS = (TextView)itemView.findViewById(R.id.nickname_tv);
            nicName = (TextView)itemView.findViewById(R.id.hint_tv);
            addBtn = (Button) itemView.findViewById(R.id.status_btn);
        }
    }

    public void setNotify(ArrayList<Connect.Workmate> list){
        if(list.size() > 0){
            this.list.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener{
        void itemClick(Connect.Workmate workmate);

        void addFriend(int position, Connect.Workmate workmate);
    }

}
