package connect.activity.workbench.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import connect.ui.activity.R;


/**
 * Created by PuJin on 2018/1/15.
 */

public class WorkSearchAdapter extends RecyclerView.Adapter<WorkSearchAdapter.ViewHolder> {


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_worksearch, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

    }

    @Override
    public int getItemCount() {
        return 5;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout thirdPartRelative;
        ImageView categortyImg;
        TextView categoryTxt;
        TextView addStateTxt;
        ImageView searchStateImg;

        public ViewHolder(View itemView) {
            super(itemView);
            thirdPartRelative = (RelativeLayout) itemView.findViewById(R.id.relative_thirdpart);
            categortyImg = (ImageView) itemView.findViewById(R.id.image_category);
            categoryTxt = (TextView) itemView.findViewById(R.id.text_category);
            addStateTxt = (TextView) itemView.findViewById(R.id.text_has_added);
            searchStateImg = (ImageView) itemView.findViewById(R.id.image_search_state);
        }
    }
}