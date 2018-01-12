package connect.activity.chat.adapter;

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
import java.util.List;

import connect.activity.chat.set.group.DepartSelectBean;
import connect.ui.activity.R;
import connect.widget.DepartmentAvatar;
import protos.Connect;

/**
 * Created by PuJin on 2018/1/10.
 */

public class GroupDepartSelectAdapter extends RecyclerView.Adapter<GroupDepartSelectAdapter.ViewHolder> {

    private Activity activity;
    private List<String> uids = null;
    private List<DepartSelectBean> departSelectBeens = new ArrayList<>();

    public GroupDepartSelectAdapter(Activity activity) {
        this.activity = activity;
    }

    public void notifyData(List<DepartSelectBean> departSelectBeens) {
        this.departSelectBeens = departSelectBeens;
        notifyDataSetChanged();
    }

    @Override
    public GroupDepartSelectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_group_departselect, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final GroupDepartSelectAdapter.ViewHolder holder, final int position) {
        final DepartSelectBean department = departSelectBeens.get(position);
        if (department.getDepartment() != null) {
            holder.departmentLinear.setVisibility(View.VISIBLE);
            holder.contentLin.setVisibility(View.GONE);

            final Connect.Department department1 = department.getDepartment();
            final String departmentKey = "B" + department1.getId();
            holder.departmentSelectView.setSelected(departSelectListener.isContains(departmentKey));

            holder.departmentTv.setText(department1.getName());
            holder.departmentSelectView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isselect = holder.departmentSelectView.isSelected();
                    isselect = !isselect;
                    departSelectListener.departmentClick(isselect, department1);
                    holder.departmentSelectView.setSelected(isselect);
                }
            });
            holder.departmentLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!departSelectListener.isContains(departmentKey)) {
                        departSelectListener.itemClick(department1);
                    }
                }
            });
        } else {
            holder.departmentLinear.setVisibility(View.GONE);
            holder.contentLin.setVisibility(View.VISIBLE);

            final Connect.Workmate workmate = department.getWorkmate();
            final String workmateKey = "W" + workmate.getUid();
            holder.workmateSelectView.setSelected(departSelectListener.isContains(workmateKey));
            holder.nameTvS.setText(workmate.getName());
            if (TextUtils.isEmpty(workmate.getOU())) {
                holder.nicName.setVisibility(View.GONE);
            } else {
                holder.nicName.setVisibility(View.VISIBLE);
                holder.nicName.setText(workmate.getOU());
            }
            if (workmate.getRegisted()) {
                holder.avater.setAvatarName(workmate.getName(), false, 1);
            } else {
                holder.avater.setAvatarName(workmate.getName(), true, 1);
            }
            holder.workmateSelectView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!uids.contains(workmate.getUid()) && !TextUtils.isEmpty(workmate.getUid())) {
                        boolean isselect = holder.workmateSelectView.isSelected();
                        isselect = !isselect;
                        departSelectListener.workmateClick(isselect, workmate);
                        holder.workmateSelectView.setSelected(isselect);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return departSelectBeens.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout contentLin;
        DepartmentAvatar avater;
        TextView nameTvS;
        TextView nicName;
        Button addBtn;

        LinearLayout departmentLinear;
        TextView departmentTv;
        TextView countTv;

        private View departmentSelectView;
        private View workmateSelectView;

        public ViewHolder(View itemView) {
            super(itemView);
            contentLin = (RelativeLayout) itemView.findViewById(R.id.content_layout);
            avater = (DepartmentAvatar) itemView.findViewById(R.id.avatar_rimg);
            nameTvS = (TextView) itemView.findViewById(R.id.nickname_tv);
            nicName = (TextView) itemView.findViewById(R.id.hint_tv);
            addBtn = (Button) itemView.findViewById(R.id.status_btn);

            departmentLinear = (LinearLayout) itemView.findViewById(R.id.department_linear);
            departmentTv = (TextView) itemView.findViewById(R.id.department_tv);
            countTv = (TextView) itemView.findViewById(R.id.count_tv);

            departmentSelectView = itemView.findViewById(R.id.department_select);
            workmateSelectView = itemView.findViewById(R.id.workmate_select);
        }
    }


    private GroupDepartSelectListener departSelectListener;

    public void setItemClickListener(GroupDepartSelectListener selectListener) {
        this.departSelectListener = selectListener;
    }

    public interface GroupDepartSelectListener {

        void departmentClick(boolean isSelect, Connect.Department department);

        void workmateClick(boolean isSelect, Connect.Workmate workmate);

        boolean isContains(String selectKey);

        void itemClick(Connect.Department department);
    }

    public void setFriendUid(List<String> friendUid) {
        this.uids = friendUid;
    }
}