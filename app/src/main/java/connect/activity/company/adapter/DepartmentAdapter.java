package connect.activity.company.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import connect.activity.base.BaseApplication;
import connect.ui.activity.R;
import connect.widget.DepartmentAvatar;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<DepartmentBean> list = new ArrayList<>();
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
        final DepartmentBean departmentBean = list.get(position);
        if(departmentBean.getId() != null){
            holder.departmentLinear.setVisibility(View.VISIBLE);
            holder.contentLin.setVisibility(View.GONE);

            holder.departmentTv.setText(departmentBean.getName());
            holder.countTv.setText("(" + departmentBean.getCount() + ")");
        }else{
            holder.departmentLinear.setVisibility(View.GONE);
            holder.contentLin.setVisibility(View.VISIBLE);

            //GlideUtil.loadAvatarRound(holder.avater, departmentBean.getAvatar());
            holder.nameTvS.setText(departmentBean.getName());
            if(TextUtils.isEmpty(departmentBean.getO_u())){
                holder.nicName.setVisibility(View.GONE);
            }else{
                holder.nicName.setVisibility(View.VISIBLE);
                holder.nicName.setText(departmentBean.getO_u());
            }
            if(departmentBean.getRegisted()){
                holder.avater.setAvatarName(departmentBean.getName(), false, departmentBean.getGender());

                /*holder.addBtn.setVisibility(View.GONE);
                holder.addBtn.setText(R.string.Chat_Chats);
                holder.addBtn.setEnabled(true);
                holder.addBtn.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_ffffff));
                holder.addBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(itemClickListener != null){
                            itemClickListener.addFriend(position, departmentBean);
                        }
                    }
                });*/
            }else{
                holder.avater.setAvatarName(departmentBean.getName(), true, departmentBean.getGender());
                /*holder.addBtn.setVisibility(View.VISIBLE);
                holder.addBtn.setText(R.string.Link_Not_logged_in);
                holder.addBtn.setEnabled(false);
                holder.addBtn.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_858998));*/
            }
        }
        holder.contentLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.itemClick(departmentBean);
            }
        });
        holder.departmentLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.itemClick(departmentBean);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout contentLin;
        DepartmentAvatar avater;
        TextView nameTvS;
        TextView nicName;
        Button addBtn;

        LinearLayout departmentLinear;
        TextView departmentTv;
        TextView countTv;

        public ViewHolder(View itemView) {
            super(itemView);
            contentLin = (RelativeLayout)itemView.findViewById(R.id.content_layout);
            avater = (DepartmentAvatar)itemView.findViewById(R.id.avatar_rimg);
            nameTvS = (TextView)itemView.findViewById(R.id.nickname_tv);
            nicName = (TextView)itemView.findViewById(R.id.hint_tv);
            addBtn = (Button) itemView.findViewById(R.id.status_btn);

            departmentLinear = (LinearLayout)itemView.findViewById(R.id.department_linear);
            departmentTv = (TextView)itemView.findViewById(R.id.department_tv);
            countTv = (TextView)itemView.findViewById(R.id.count_tv);
        }
    }

    public void setNotify(ArrayList<DepartmentBean> list){
        this.list.clear();
        if(list.size() > 0){
            this.list.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener{
        void itemClick(DepartmentBean departmentBean);

        void addFriend(int position, DepartmentBean departmentBean);
    }

}
