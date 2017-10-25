package connect.activity.wallet.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.activity.home.bean.WalletMenuBean;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WalletMenuAdapter extends RecyclerView.Adapter<WalletMenuAdapter.WalletMenuItem> {

    private ArrayList<WalletMenuBean> mDates;
    private Activity mActivity;

    public WalletMenuAdapter(ArrayList<WalletMenuBean> mDates, Activity mActivity) {
        this.mDates = mDates;
        this.mActivity = mActivity;
    }

    @Override
    public WalletMenuAdapter.WalletMenuItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_menu_item, null);
        return new WalletMenuItem(view);
    }

    @Override
    public void onBindViewHolder(WalletMenuAdapter.WalletMenuItem holder, final int position) {
        WalletMenuBean menuBean = mDates.get(position);

        holder.iconImg.setImageResource(menuBean.getIconID());
        holder.nameTv.setText(menuBean.getNameID());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.itemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDates.size();
    }

    public class WalletMenuItem extends RecyclerView.ViewHolder {

        @Bind(R.id.icon_img)
        ImageView iconImg;
        @Bind(R.id.name_tv)
        TextView nameTv;

        private final LinearLayout mContentLinearLayout;

        public WalletMenuItem(View itemView) {
            super(itemView);
            mContentLinearLayout = (LinearLayout) itemView;
            ButterKnife.bind(this, itemView);
        }
    }

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void itemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
