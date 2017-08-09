package connect.activity.login.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import connect.activity.contact.adapter.FriendRecordAdapter;
import connect.ui.activity.R;
import connect.activity.login.bean.CountryBean;
import connect.utils.PinyinUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import protos.Connect;

/**
 * Created by Administrator on 2017/1/5.
 */
public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.ViewHolder> {

    private Activity activity;
    private ArrayList<CountryBean> mDataList = new ArrayList<>();

    public CountryAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_login_country, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        CountryBean countryBean = mDataList.get(position);
        holder.countryTv.setText("+" + countryBean.getCode() + " " + countryBean.getName());

        String lastName = "";
        if(position > 0 && !TextUtils.isEmpty(mDataList.get(position - 1).getName())){
            lastName = PinyinUtil.chatToPinyin(mDataList.get(position - 1).getName().charAt(0));
        }
        String curName = PinyinUtil.chatToPinyin(countryBean.getName().charAt(0));
        if (lastName.equals(curName)) {
            holder.topTv.setVisibility(View.GONE);
        } else {
            holder.topTv.setVisibility(View.VISIBLE);
            holder.topTv.setText(curName);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDataList!=null&&position<=mDataList.size()){
                    itemClickListener.itemClick(mDataList.get(position));
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }


    public void setDataNotify(List<CountryBean> list) {
        mDataList.clear();
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView topTv;
        TextView countryTv;

        ViewHolder(View itemview) {
            super(itemview);
            topTv = (TextView) itemview.findViewById(R.id.top_tv);
            countryTv = (TextView) itemview.findViewById(R.id.country_tv);
        }
    }

    public int getPositionForSection(char selectchar) {
        if(mDataList.size() == 0)
            return -1;
        for (int i = 0; i < mDataList.size()-1; i++) {
            CountryBean countryBean = mDataList.get(i);
            String showName = countryBean.getName();
            String firstChar = PinyinUtil.chatToPinyin(showName.charAt(0));
            if (firstChar.charAt(0) == selectchar) {
                return i;
            }
        }
        return -1;
    }

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener{
        void itemClick(CountryBean countryBean);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

}
