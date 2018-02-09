package connect.activity.contact.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.utils.system.SystemDataUtil;
import connect.widget.SideScrollView;
import protos.Connect;

public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<Connect.UserInfoBase> mList = new ArrayList<>();
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
        final Connect.UserInfoBase userInfoBase = mList.get(position);
        viewHolder.contentLayout.getLayoutParams().width = SystemDataUtil.getScreenWidth();
        GlideUtil.loadAvatarRound(viewHolder.avatarRimg,userInfoBase.getAvatar() + "?size=80");
        viewHolder.nicknameTv.setText(userInfoBase.getUsername());
        viewHolder.statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddListener.add(position,userInfoBase);
            }
        });
        viewHolder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddListener.itemClick(position,userInfoBase);
            }
        });
        viewHolder.deleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddListener.deleteItem(position, userInfoBase);
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

    public ArrayList<Connect.UserInfoBase> getData(){
        return mList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView deleteTv;
        RelativeLayout bottomLayout;
        ImageView avatarRimg;
        TextView nicknameTv;
        Button statusBtn;
        RelativeLayout contentLayout;
        SideScrollView sideScrollView;
        LinearLayout contentRela;

        ViewHolder(View itemview) {
            super(itemview);
            deleteTv = (ImageView) itemview.findViewById(R.id.delete_tv);
            bottomLayout = (RelativeLayout) itemview.findViewById(R.id.bottom_layout);
            avatarRimg = (ImageView) itemview.findViewById(R.id.avatar_rimg);
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

        void add(int position, Connect.UserInfoBase entity);

        void itemClick(int position, Connect.UserInfoBase entity);

        void deleteItem(int position, Connect.UserInfoBase entity);

    }

}
