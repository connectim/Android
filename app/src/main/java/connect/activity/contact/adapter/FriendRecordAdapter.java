package connect.activity.contact.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.data.RateFormatUtil;
import connect.utils.TimeUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.AvatarGridView;
import connect.widget.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/26 0026.
 */

public class FriendRecordAdapter extends BaseAdapter{

    private ArrayList<Connect.FriendBill> mListData = new ArrayList();
    private ContactEntity friendEntity;

    public FriendRecordAdapter(ContactEntity friendEntity) {
        this.friendEntity = friendEntity;
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_transaction, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (FriendRecordAdapter.ViewHolder)convertView.getTag();
        }
        Connect.FriendBill friendBill = mListData.get(position);
        viewHolder.avatarGridview.setVisibility(View.GONE);
        viewHolder.avaterRimg.setVisibility(View.VISIBLE);

        GlideUtil.loadAvater(viewHolder.avaterRimg,friendEntity.getAvatar());
        String curName = TextUtils.isEmpty(friendEntity.getRemark()) ? friendEntity.getUsername() : friendEntity.getRemark();
        viewHolder.nameTv.setText(curName);
        viewHolder.timeTv.setText(TimeUtil.getTime(friendBill.getCreatedAt() * 1000, TimeUtil.DATE_FORMAT_MONTH_HOUR));

        if (friendBill.getAmount() > 0) {
            viewHolder.balanceTv.setTextColor(parent.getContext().getResources().getColor(R.color.color_00c400));
            viewHolder.balanceTv.setText("+" + RateFormatUtil.longToDoubleBtc(friendBill.getAmount()));
        } else {
            viewHolder.balanceTv.setTextColor(parent.getContext().getResources().getColor(R.color.color_f04a5f));
            viewHolder.balanceTv.setText("" + RateFormatUtil.longToDoubleBtc(friendBill.getAmount()));
        }

        if (friendBill.getStatus() == 1) {
            viewHolder.statusTv.setTextColor(parent.getContext().getResources().getColor(R.color.color_767a82));
            viewHolder.statusTv.setText(parent.getContext().getString(R.string.Wallet_Confirmed));
        } else {
            viewHolder.statusTv.setTextColor(parent.getContext().getResources().getColor(R.color.color_f04a5f));
            viewHolder.statusTv.setText(parent.getContext().getString(R.string.Wallet_Unconfirmed));
        }
        return convertView;
    }

    class ViewHolder {
        @Bind(R.id.avater_rimg)
        RoundedImageView avaterRimg;
        @Bind(R.id.avatar_gridview)
        AvatarGridView avatarGridview;
        @Bind(R.id.left_rela)
        RelativeLayout leftRela;
        @Bind(R.id.name_tv)
        TextView nameTv;
        @Bind(R.id.balance_tv)
        TextView balanceTv;
        @Bind(R.id.time_tv)
        TextView timeTv;
        @Bind(R.id.status_tv)
        TextView statusTv;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public void setNotifyData(List list, boolean isClear) {
        if (isClear) {
            mListData.clear();
        }
        mListData.addAll(list);
        notifyDataSetChanged();
    }

}
