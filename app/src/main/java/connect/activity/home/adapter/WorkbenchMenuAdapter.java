package connect.activity.home.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import connect.activity.workbench.data.MenuBean;
import connect.ui.activity.R;
import connect.utils.system.SystemDataUtil;

/**
 * Created by Administrator on 2016/12/10.
 */
public class WorkbenchMenuAdapter extends RecyclerView.Adapter {

    private ArrayList<MenuBean> mDates = new ArrayList<>();
    private Activity mActivity;
    private OnItemMenuClickListener onItemClickListence;

    public WorkbenchMenuAdapter(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View view = inflater.inflate(R.layout.workbench_menu_item, parent, false);
        WalletMenuItem holder = new WalletMenuItem(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        WalletMenuItem viewHolder = (WalletMenuItem) holder;
        final MenuBean menuBean = mDates.get(position);

        viewHolder.iconImg.setImageResource(menuBean.getIconId());
        try {
            viewHolder.nameTv.setText(menuBean.getTextId());
        }catch (Exception e){
            e.printStackTrace();
        }
        viewHolder.contentLinear.setLayoutParams(new RecyclerView.LayoutParams(SystemDataUtil.getScreenWidth()/4, SystemDataUtil.getScreenWidth()/4));
        viewHolder.contentLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListence.itemClick(position, menuBean);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDates.size();
    }

    public class WalletMenuItem extends RecyclerView.ViewHolder {

        ImageView iconImg;
        TextView nameTv;
        LinearLayout contentLinear;

        public WalletMenuItem(View itemView) {
            super(itemView);
            iconImg = (ImageView)itemView.findViewById(R.id.icon_img);
            nameTv = (TextView)itemView.findViewById(R.id.name_tv);
            contentLinear = (LinearLayout)itemView.findViewById(R.id.content_linear);
        }
    }

    public void setNotify(ArrayList<MenuBean> list){

        mDates.clear();
        mDates.addAll(list);
        notifyDataSetChanged();
    }

    public void setOnItemClickListence(OnItemMenuClickListener onItemClickListence){
        this.onItemClickListence = onItemClickListence;
    }

    public interface OnItemMenuClickListener{
        void itemClick(int position, MenuBean item);
    }

}
