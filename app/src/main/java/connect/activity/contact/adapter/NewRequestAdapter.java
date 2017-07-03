package connect.activity.contact.adapter;

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
import connect.database.green.bean.FriendRequestEntity;
import connect.ui.activity.R;
import connect.activity.base.BaseApplication;
import connect.utils.system.SystemDataUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.SideScrollView;
import connect.widget.roundedimageview.RoundedImageView;

/**
 * Created by Administrator on 2016/12/29.
 */
public class NewRequestAdapter extends BaseAdapter {

    private ArrayList<FriendRequestEntity> mList = new ArrayList<>();
    private OnAcceptListence onAcceptListence;
    private int recommendCount;

    public void setDataNotify(List list) {
        mList.clear();
        mList.addAll(0, list);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_friend_request, parent, false);
            viewHolder = new ViewHolder(convertView);
            viewHolder.contentLayout.getLayoutParams().width = SystemDataUtil.getScreenWidth();
            viewHolder.sideScrollView.setSideScrollListener(sideScrollListener);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
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
        viewHolder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAcceptListence.itemClick(position, friendRequestEntity);
            }
        });
        viewHolder.deleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAcceptListence.deleteItem(position, friendRequestEntity);
            }
        });

        return convertView;
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

    class ViewHolder {
        @Bind(R.id.txt)
        TextView txt;
        @Bind(R.id.delete_tv)
        ImageView deleteTv;
        @Bind(R.id.bottom_layout)
        RelativeLayout bottomLayout;
        @Bind(R.id.avatar_rimg)
        RoundedImageView avatarRimg;
        @Bind(R.id.nickname_tv)
        TextView nicknameTv;
        @Bind(R.id.hint_tv)
        TextView hintTv;
        @Bind(R.id.status_btn)
        Button statusBtn;
        @Bind(R.id.content_layout)
        RelativeLayout contentLayout;
        @Bind(R.id.side_scroll_view)
        SideScrollView sideScrollView;
        @Bind(R.id.content_rela)
        LinearLayout contentRela;
        @Bind(R.id.top_rela)
        RelativeLayout topRela;
        @Bind(R.id.more_tv)
        TextView moreTv;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public void setRecommendCount(int count){
        this.recommendCount = count;
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
