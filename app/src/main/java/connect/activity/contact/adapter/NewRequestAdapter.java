package connect.activity.contact.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.activity.base.BaseApplication;
import connect.database.green.bean.FriendRequestEntity;
import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.utils.system.SystemDataUtil;
import connect.widget.SideScrollView;
import connect.widget.roundedimageview.RoundedImageView;

/**
 * Created by Administrator on 2016/12/29.
 */
public class NewRequestAdapter extends RecyclerView.Adapter<NewRequestAdapter.ViewHolder> {

    private ArrayList<FriendRequestEntity> mList = new ArrayList<>();
    private OnAcceptListence onAcceptListence;
    private int recommendCount;

    private Activity activity;

    public NewRequestAdapter(Activity activity){
        this.activity=activity;
    }

    public void setDataNotify(List list) {
        mList.clear();
        mList.addAll(0, list);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_contact_friend_request, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final FriendRequestEntity friendRequestEntity = mList.get(position);

        viewHolder.txt.setVisibility(View.VISIBLE);
        viewHolder.sideScrollView.setVisibility(View.VISIBLE);

        if (position == 0 || (mList.get(position - 1).getStatus() == 4 && friendRequestEntity.getStatus() != 4)) {
            viewHolder.topRela.setVisibility(View.VISIBLE);
            if(friendRequestEntity.getStatus() == 4){
                viewHolder.txt.setText(R.string.Link_People_you_may_know);
                if(recommendCount == 4){
                    viewHolder.moreTv.setVisibility(View.VISIBLE);
                    viewHolder.moreTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onAcceptListence.itemClick(position, new FriendRequestEntity());
                        }
                    });
                }
            }else{
                viewHolder.txt.setText(R.string.Link_Your_invitation);
                viewHolder.moreTv.setVisibility(View.GONE);
            }
        } else {
            viewHolder.topRela.setVisibility(View.GONE);
        }

        GlideUtil.loadAvater(viewHolder.avatarRimg, friendRequestEntity.getAvatar() + "?size=80");
        viewHolder.nicknameTv.setText(friendRequestEntity.getUsername());
        viewHolder.hintTv.setText(friendRequestEntity.getTips());
        showRequestBtn(viewHolder.statusBtn, friendRequestEntity, position);
        viewHolder.contentLayout.getLayoutParams().width = SystemDataUtil.getScreenWidth();
        viewHolder.contentLayout.setTag(viewHolder.sideScrollView);
        viewHolder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SideScrollView scrollView = (SideScrollView) v.getTag();
                if (menuIsOpen(scrollView)) {
                    closeMenu();
                }
            }
        });

        viewHolder.statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAcceptListence.itemClick(position, friendRequestEntity);
            }
        });
        viewHolder.deleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
                onAcceptListence.deleteItem(position, friendRequestEntity);
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

    private void showRequestBtn(Button btn, final FriendRequestEntity requestEntity, final int position) {
        switch (requestEntity.getStatus()) {//1：To be accepted  2：Have been added 3：In the validation 4：Recommend friends
            case 1:
            case 4:
                if(requestEntity.getStatus() == 1){
                    btn.setText(R.string.Link_Accept);
                }else{
                    btn.setText(R.string.Link_Add);
                }
                btn.setEnabled(true);
                btn.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_ffffff));
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onAcceptListence != null) {
                            onAcceptListence.accept(position, requestEntity);
                        }
                    }
                });
                break;
            case 2:
                btn.setText(R.string.Link_Added);
                btn.setEnabled(false);
                btn.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_858998));
                break;
            case 3:
                btn.setText(R.string.Link_Verify);
                btn.setEnabled(false);
                btn.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_858998));
                break;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView txt;
        ImageView deleteTv;
        RelativeLayout bottomLayout;
        RoundedImageView avatarRimg;
        TextView nicknameTv;
        TextView hintTv;
        Button statusBtn;
        RelativeLayout contentLayout;
        SideScrollView sideScrollView;
        RelativeLayout contentRela;
        RelativeLayout topRela;
        TextView moreTv;

        ViewHolder(View itemview) {
            super(itemview);
            txt= (TextView) itemview.findViewById(R.id.txt);
            deleteTv= (ImageView) itemview.findViewById(R.id.delete_tv);
            bottomLayout= (RelativeLayout) itemview.findViewById(R.id.bottom_layout);
            avatarRimg= (RoundedImageView) itemview.findViewById(R.id.avatar_rimg);
            nicknameTv= (TextView) itemview.findViewById(R.id.nickname_tv);
            hintTv= (TextView) itemview.findViewById(R.id.hint_tv);
            statusBtn= (Button) itemview.findViewById(R.id.status_btn);
            contentLayout= (RelativeLayout) itemview.findViewById(R.id.content_layout);
            sideScrollView= (SideScrollView) itemview.findViewById(R.id.side_scroll_view);
            contentRela= (RelativeLayout) itemview.findViewById(R.id.content_rela);
            topRela= (RelativeLayout) itemview.findViewById(R.id.top_rela);
            moreTv= (TextView) itemview.findViewById(R.id.more_tv);
            ((SideScrollView) itemView.findViewById(R.id.side_scroll_view)).setSideScrollListener(sideScrollListener);
        }
    }

    public void setRecommendCount(int count){
        this.recommendCount = count;
    }

    public Boolean menuIsOpen(SideScrollView scrollView) {
        return scrollView != null && scrollView.isOpen();
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

    public void setOnAcceptListence(OnAcceptListence onAcceptListence) {
        this.onAcceptListence = onAcceptListence;
    }

    public interface OnAcceptListence {

        void accept(int position, FriendRequestEntity entity);

        void itemClick(int position, FriendRequestEntity entity);

        void deleteItem(int position, FriendRequestEntity entity);

    }
}
