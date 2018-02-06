package connect.activity.workbench.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import connect.activity.workbench.data.MenuBean;
import connect.activity.workbench.data.MenuData;
import connect.ui.activity.R;
import connect.utils.dialog.DialogUtil;
import protos.Connect;


/**
 * Created by PuJin on 2018/1/15.
 */

public class WorkSearchAdapter extends RecyclerView.Adapter<WorkSearchAdapter.ViewHolder> {

    private Context context;
    private List<Connect.Application> applications = null;

    public void setDatas(List<Connect.Application> applications) {
        this.applications = applications;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_worksearch, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Connect.Application application = applications.get(position);

        MenuBean menuBean = MenuData.getInstance().getData(application.getCode());
        if(menuBean == null){
            menuBean = new MenuBean();
        }
        holder.categortyImg.setBackgroundResource(menuBean.getIconId());
        holder.categoryTxt.setText(menuBean.getTextId());
        holder.addStateTxt.setVisibility(View.GONE);
        holder.searchStateImg.setSelected(!application.getAdded());
        holder.searchStateImg.setVisibility(application.getCategory() == 1 ? View.GONE : View.VISIBLE);
        holder.contentRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (application.getCategory() == 1) {
                    DialogUtil.showAlertTextView(context, context.getString(R.string.Set_tip_title),
                            context.getString(R.string.Link_Function_Under_Development),
                            "", "", true, new DialogUtil.OnItemClickListener() {
                                @Override
                                public void confirm(String value) {
                                }

                                @Override
                                public void cancel() {
                                }
                            });
                } else {
                    String code = application.getCode();
                    interWorksearch.itemClick(!application.getAdded(), code);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return applications == null ? 0 : applications.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout thirdPartRelative;
        RelativeLayout contentRelative;
        ImageView categortyImg;
        TextView categoryTxt;
        TextView addStateTxt;
        ImageView searchStateImg;

        public ViewHolder(View itemView) {
            super(itemView);
            thirdPartRelative = (RelativeLayout) itemView.findViewById(R.id.relative_thirdpart);
            contentRelative = (RelativeLayout) itemView.findViewById(R.id.relative_worksearch_content);
            categortyImg = (ImageView) itemView.findViewById(R.id.image_category);
            categoryTxt = (TextView) itemView.findViewById(R.id.text_category);
            addStateTxt = (TextView) itemView.findViewById(R.id.text_has_added);
            searchStateImg = (ImageView) itemView.findViewById(R.id.image_search_state);
        }
    }

    public InterWorksearch interWorksearch;

    public void setInterWorksearch(InterWorksearch interWorksearch) {
        this.interWorksearch = interWorksearch;
    }

    public interface InterWorksearch {
        void itemClick(boolean isAdd, String code);
    }
}