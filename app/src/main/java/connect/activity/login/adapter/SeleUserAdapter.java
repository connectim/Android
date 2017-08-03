package connect.activity.login.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.utils.system.SystemDataUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.SideScrollView;
import connect.widget.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/8.
 */
public class SeleUserAdapter extends RecyclerView.Adapter<SeleUserAdapter.UserHolder> {

    private final LayoutInflater inflater;
    private final OnItemClickListence onItemClickListence;
    private SideScrollView sideScrollView;
    private List<UserBean> msgRoomEntities = new ArrayList<>();
    private UserBean userBean;

    public SeleUserAdapter(Activity activity,OnItemClickListence onItemClickListence) {
        inflater = LayoutInflater.from(activity);
        this.onItemClickListence = onItemClickListence;
    }

    public void setData(List<UserBean> entities,UserBean userBean) {
        if(entities != null){
            this.msgRoomEntities.addAll(entities);
        }
        this.userBean = userBean;
        notifyDataSetChanged();
    }

    @Override
    public SeleUserAdapter.UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_login_sele_user,parent, false);
        UserHolder userHolder = new UserHolder(view);
        return userHolder;
    }

    @Override
    public void onBindViewHolder(SeleUserAdapter.UserHolder holder, final int position) {
        if(userBean != null && userBean.getTalkKey().equals(msgRoomEntities.get(position).getTalkKey())){
            holder.statusImg.setVisibility(View.VISIBLE);
        }else{
            holder.statusImg.setVisibility(View.GONE);
        }
        GlideUtil.loadAvater(holder.avater,msgRoomEntities.get(position).getAvatar());
        holder.name.setText(msgRoomEntities.get(position).getName());

        holder.contentLayout.getLayoutParams().width = SystemDataUtil.getScreenWidth();
        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuIsOpen()) {
                    closeMenu();
                }
                onItemClickListence.onClick(msgRoomEntities.get(position),position);
            }
        });

        holder.bottomClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
                onItemClickListence.itemOnClick(msgRoomEntities.get(position),position);
                removeData(msgRoomEntities.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return msgRoomEntities.size();
    }

    public class UserHolder extends RecyclerView.ViewHolder {
        private final ImageView bottomClear;
        private final RelativeLayout contentLayout;
        private final RoundedImageView avater;
        private final TextView name;
        private final ImageView statusImg;
        public UserHolder(View itemView) {
            super(itemView);
            contentLayout = (RelativeLayout) itemView.findViewById(R.id.content_layout);
            bottomClear = (ImageView) itemView.findViewById(R.id.bottom_clear);
            avater = (RoundedImageView) itemView.findViewById(R.id.avater_rimg);
            name = (TextView)itemView.findViewById(R.id.name_tv);
            statusImg = (ImageView)itemView.findViewById(R.id.status_img);

            ((SideScrollView) itemView).setSideScrollListener(sideScrollListener);
        }
    }

    public void removeData(UserBean userBean) {
        for(int i = 0;i < msgRoomEntities.size();i ++){
            if(userBean.getPubKey().equals(msgRoomEntities.get(i).getPubKey())){
                msgRoomEntities.remove(i);
                notifyDataSetChanged();
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
