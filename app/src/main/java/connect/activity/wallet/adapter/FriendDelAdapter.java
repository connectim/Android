package connect.activity.wallet.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.system.SystemDataUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.SideScrollView;

public class FriendDelAdapter extends RecyclerView.Adapter<FriendDelAdapter.FriendHolder> {

    private List<ContactEntity> list = new ArrayList<>();
    private SideScrollView sideScrollView;

    public void setData(List<ContactEntity> entities) {
        this.list.clear();
        if(entities != null){
            this.list.addAll(entities);
        }
        notifyDataSetChanged();
    }

    @Override
    public FriendDelAdapter.FriendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_friend_del, parent, false);
        FriendHolder addressHolder = new FriendHolder(view);
        return addressHolder;
    }

    @Override
    public void onBindViewHolder(final FriendDelAdapter.FriendHolder holder, final int position) {
        holder.contentLayout.getLayoutParams().width = SystemDataUtil.getScreenWidth();
        final ContactEntity friendEntity = list.get(position);
        GlideUtil.loadAvatarRound(holder.avater,friendEntity.getAvatar());
        holder.name.setText(friendEntity.getUsername());
        holder.deleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
                removeData(friendEntity);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class FriendHolder extends RecyclerView.ViewHolder{
        private final ImageView deleteTv;
        private final RelativeLayout contentLayout;
        private final ImageView avater;
        private final TextView name;
        public FriendHolder(View itemView) {
            super(itemView);
            contentLayout = (RelativeLayout) itemView.findViewById(R.id.content_layout);
            deleteTv = (ImageView) itemView.findViewById(R.id.delete_tv);
            avater = (ImageView) itemView.findViewById(R.id.avater_rimg);
            name = (TextView)itemView.findViewById(R.id.name_tv);

            ((SideScrollView) itemView).setSideScrollListener(sideScrollListener);
        }
    }


    public void removeData(ContactEntity friendEntity) {
        for(int i = 0;i < list.size();i ++){
            if(friendEntity.getUid().equals(list.get(i).getUid())){
                list.remove(i);
                notifyItemRemoved(i);
                break;
            }
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

    public List<ContactEntity> getDataList(){
        return list;
    }

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

}
