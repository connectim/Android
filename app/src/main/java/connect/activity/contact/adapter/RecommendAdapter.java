package connect.activity.contact.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.database.green.bean.RecommandFriendEntity;
import connect.ui.activity.R;
import connect.utils.system.SystemDataUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.SideScrollView;
import connect.widget.roundedimageview.RoundedImageView;

public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<RecommandFriendEntity> mList = new ArrayList<>();
    private OnAddListener onAddListener;

    public RecommendAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_contact_recommend, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final RecommandFriendEntity recommendEntity = mList.get(position);
        viewHolder.contentLayout.getLayoutParams().width = SystemDataUtil.getScreenWidth();
        GlideUtil.loadAvater(viewHolder.avatarRimg,recommendEntity.getAvatar() + "?size=80");
        viewHolder.nicknameTv.setText(recommendEntity.getUsername());
        viewHolder.statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddListener.add(position,recommendEntity);
            }
        });
        viewHolder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddListener.itemClick(position,recommendEntity);
            }
        });
        viewHolder.deleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddListener.deleteItem(position,recommendEntity);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setDataNotify(List list,boolean isClear) {
        if(isClear)
            mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public ArrayList<RecommandFriendEntity> getData(){
        return mList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView deleteTv;
        RelativeLayout bottomLayout;
        RoundedImageView avatarRimg;
        TextView nicknameTv;
        Button statusBtn;
        RelativeLayout contentLayout;
        SideScrollView sideScrollView;
        LinearLayout contentRela;

        ViewHolder(View itemview) {
            super(itemview);
            deleteTv = (ImageView) itemview.findViewById(R.id.delete_tv);
            bottomLayout = (RelativeLayout) itemview.findViewById(R.id.bottom_layout);
            avatarRimg = (RoundedImageView) itemview.findViewById(R.id.avatar_rimg);
            nicknameTv = (TextView) itemview.findViewById(R.id.nickname_tv);
            statusBtn = (Button) itemview.findViewById(R.id.status_btn);
            contentLayout = (RelativeLayout) itemview.findViewById(R.id.content_layout);
            sideScrollView = (SideScrollView) itemview.findViewById(R.id.side_scroll_view);
            contentRela = (LinearLayout) itemview.findViewById(R.id.content_rela);
            sideScrollView.setSideScrollListener(sideScrollListener);
        }
    }

    public void closeMenu() {
        if (sideScrollView != null) {
            sideScrollView.closeMenu();
            sideScrollView = null;
        }
    }

    public Boolean menuIsOpen() {
        return sideScrollView != null;
    }

    private SideScrollView sideScrollView;
    private SideScrollView.SideScrollListener sideScrollListener = new SideScrollView.SideScrollListener() {

        @Override
        public void onMenuIsOpen(View view) {
            sideScrollView = (SideScrollView) view;
        }

        @Override
        public void onDownOrMove(SideScrollView slidingButtonView) {
            if (menuIsOpen()) {
                if (sideScrollView != slidingButtonView) {
                    closeMenu();
                }
            }
        }
    };

    public void setOnAddListener(OnAddListener onAddListener) {
        this.onAddListener = onAddListener;
    }

    public interface OnAddListener {

        void add(int position, RecommandFriendEntity entity);

        void itemClick(int position, RecommandFriendEntity entity);

        void deleteItem(int position, RecommandFriendEntity entity);

    }

}
