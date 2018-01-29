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

public class NewRequestAdapter extends RecyclerView.Adapter<NewRequestAdapter.ViewHolder> {

    public static int ACCEPTE_ADD_FRIEND = 1;
    public static int FINISH_ADD_FRIEND = 2;
    public static int VALIDATION_ADD_FRIEND = 3;
    public static int RECOMMEND_ADD_FRIEND = 4;
    private ArrayList<FriendRequestEntity> mList = new ArrayList<>();
    private OnAcceptListener onAcceptListener;
    private Activity activity;
    private boolean isShowMoreRecommend;

    public NewRequestAdapter(Activity activity){
        this.activity=activity;
    }

    public void setDataNotify(boolean isShowMoreRecommend, List list) {
        mList.clear();
        mList.addAll(0, list);
        this.isShowMoreRecommend = isShowMoreRecommend;
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
        viewHolder.topRela.setVisibility(View.GONE);
        /*if(position == 0){
            viewHolder.txt.setText(R.string.Link_Your_invitation);
            viewHolder.moreTv.setVisibility(View.GONE);
        }else {
            viewHolder.topRela.setVisibility(View.GONE);
        }*/

        GlideUtil.loadAvatarRound(viewHolder.avatarRimg, friendRequestEntity.getAvatar() + "?size=80");
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
                onAcceptListener.itemClick(position, friendRequestEntity);
            }
        });
        viewHolder.statusBtn.setOnClickListener(onClickListener);
        viewHolder.statusBtn.setTag(position);
        viewHolder.deleteTv.setOnClickListener(onClickListener);
        viewHolder.deleteTv.setTag(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            int position = (int)v.getTag();
            FriendRequestEntity entity = mList.get(position);
            switch (v.getId()){
                case R.id.status_btn:
                    onAcceptListener.accept(position, entity);
                    break;
                case R.id.delete_tv:
                    closeMenu();
                    onAcceptListener.deleteItem(position, entity);
                    break;
                case R.id.more_tv:
                    onAcceptListener.itemClick(position, new FriendRequestEntity());
                    break;
                default:
                    break;
            }
        }
    };

    private void showRequestBtn(Button btn, final FriendRequestEntity requestEntity, final int position) {
        switch (requestEntity.getStatus()) {//1：To be accepted  2：Have been added 3：In the validation 4：Recommend friends
            case 1:
            case 4:
                if(requestEntity.getStatus() == ACCEPTE_ADD_FRIEND){
                    btn.setText(R.string.Link_Accept);
                }else{
                    btn.setText(R.string.Link_Add);
                }
                btn.setEnabled(true);
                btn.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_ffffff));
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onAcceptListener != null) {
                            onAcceptListener.accept(position, requestEntity);
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
        ImageView avatarRimg;
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
            avatarRimg= (ImageView) itemview.findViewById(R.id.avatar_rimg);
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

    public void setOnAcceptListener(OnAcceptListener onAcceptListener) {
        this.onAcceptListener = onAcceptListener;
    }

    public interface OnAcceptListener {

        void accept(int position, FriendRequestEntity entity);

        void itemClick(int position, FriendRequestEntity entity);

        void deleteItem(int position, FriendRequestEntity entity);

    }
}
