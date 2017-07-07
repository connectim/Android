package connect.ui.activity.wallet.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.utils.data.RateFormatUtil;
import connect.utils.TimeUtil;
import connect.view.AvatarGridView;
import connect.view.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import protos.Connect;

/**
 * Created by Administrator on 2016/12/14.
 */
public class TransactionAdapter extends BaseAdapter {

    private ArrayList<Connect.Transaction> mListData = new ArrayList();

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
            viewHolder = (ViewHolder) convertView.getTag();
        }

        int typeTx = mListData.get(position).getTxType();
        viewHolder.avatarGridview.setVisibility(View.GONE);
        viewHolder.avaterRimg.setVisibility(View.GONE);
        switch (typeTx) {
            case 0:
                viewHolder.avaterRimg.setVisibility(View.VISIBLE);
                viewHolder.avaterRimg.setImageResource(R.mipmap.bitcoin_luckybag3x);
                break;
            case 1://Common transfer and payment
            case 2://gather
                List<Connect.UserInfoBalance> list = mListData.get(position).getUserInfosList();
                if(list.size() > 0){
                    ArrayList arrayList = new ArrayList<String>();
                    for(Connect.UserInfoBalance userInfoBalance : list){
                        arrayList.add(userInfoBalance.getAvatar());
                    }
                    viewHolder.avatarGridview.setVisibility(View.VISIBLE);
                    viewHolder.avatarGridview.setAvaterData(arrayList);
                }else{
                    viewHolder.avaterRimg.setVisibility(View.VISIBLE);
                    viewHolder.avaterRimg.setImageResource(R.mipmap.bitcoin_luckybag3x);
                }
                break;
            case 3://lucky packet
            case 4:
            case 5:
            case 7://system lucky packet
                viewHolder.avaterRimg.setVisibility(View.VISIBLE);
                viewHolder.avaterRimg.setImageResource(R.mipmap.luckpacket_record2x);
                break;
            case 6://outer transfer
                viewHolder.avaterRimg.setVisibility(View.VISIBLE);
                viewHolder.avaterRimg.setImageResource(R.mipmap.bitcoin_luckybag3x);
                break;
            default:
                break;
        }

        if(typeTx == 7) {
            viewHolder.nameTv.setText(R.string.Wallet_From_Connect_team);
        }else if (mListData.get(position).getUserInfosList().size() > 0) {
            if(TextUtils.isEmpty(mListData.get(position).getUserInfos(0).getAvatar())){
                viewHolder.nameTv.setText(mListData.get(position).getUserInfos(0).getAddress());
            }else{
                viewHolder.nameTv.setText(mListData.get(position).getUserInfos(0).getUsername());
            }
        } else {
            viewHolder.nameTv.setText("me");
        }

        if (mListData.get(position).getBalance() > 0) {
            viewHolder.balanceTv.setTextColor(parent.getContext().getResources().getColor(R.color.color_00c400));
            viewHolder.balanceTv.setText("+" + RateFormatUtil.longToDoubleBtc(mListData.get(position).getBalance()));
        } else {
            viewHolder.balanceTv.setTextColor(parent.getContext().getResources().getColor(R.color.color_f04a5f));
            viewHolder.balanceTv.setText("" + RateFormatUtil.longToDoubleBtc(mListData.get(position).getBalance()));
        }

        viewHolder.timeTv.setText(TimeUtil.getTime(mListData.get(position).getCreatedAt() * 1000, TimeUtil.DATE_FORMAT_MONTH_HOUR));

        if (mListData.get(position).getConfirmations() > 0) {//confirm
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
