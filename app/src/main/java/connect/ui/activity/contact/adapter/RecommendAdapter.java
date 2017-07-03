package connect.ui.activity.contact.adapter;

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
import connect.db.green.bean.RecommandFriendEntity;
import connect.ui.activity.R;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import connect.utils.glide.GlideUtil;
import connect.view.SideScrollView;
import connect.view.roundedimageview.RoundedImageView;

/**
 * Created by Administrator on 2017/1/21.
 */

public class RecommendAdapter extends BaseAdapter {

    private ArrayList<RecommandFriendEntity> mList = new ArrayList<>();
    private OnAddListence onAddListence;

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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_recommend, parent, false);
            viewHolder = new ViewHolder(convertView);
            viewHolder.contentLayout.getLayoutParams().width = SystemDataUtil.getScreenWidth();
            viewHolder.sideScrollView.setSideScrollListener(sideScrollListener);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final RecommandFriendEntity recommendEntity = mList.get(position);
        GlideUtil.loadAvater(viewHolder.avatarRimg,recommendEntity.getAvatar() + "?size=80");
        viewHolder.nicknameTv.setText(recommendEntity.getUsername());
        viewHolder.statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddListence.add(position,recommendEntity);
            }
        });
        viewHolder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddListence.itemClick(position,recommendEntity);
            }
        });
        viewHolder.deleteTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddListence.deleteItem(position,recommendEntity);
            }
        });
        return convertView;
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

    static class ViewHolder {
        @Bind(R.id.delete_tv)
        ImageView deleteTv;
        @Bind(R.id.bottom_layout)
        RelativeLayout bottomLayout;
        @Bind(R.id.avatar_rimg)
        RoundedImageView avatarRimg;
        @Bind(R.id.nickname_tv)
        TextView nicknameTv;
        @Bind(R.id.status_btn)
        Button statusBtn;
        @Bind(R.id.content_layout)
        RelativeLayout contentLayout;
        @Bind(R.id.side_scroll_view)
        SideScrollView sideScrollView;
        @Bind(R.id.content_rela)
        LinearLayout contentRela;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
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

    public void setOnAddListence(OnAddListence onAddListence) {
        this.onAddListence = onAddListence;
    }

    public interface OnAddListence {

        void add(int position, RecommandFriendEntity entity);

        void itemClick(int position, RecommandFriendEntity entity);

        void deleteItem(int position, RecommandFriendEntity entity);

    }

}
