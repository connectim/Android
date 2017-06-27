package connect.activity.login.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.activity.login.bean.CountryBean;
import connect.utils.PinyinUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/1/5.
 */
public class CountryAdapter extends BaseAdapter {

    private ArrayList<CountryBean> mDataList = new ArrayList<>();

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_login_country, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
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

        return convertView;
    }

    public void setDataNotify(List<CountryBean> list) {
        mDataList.clear();
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        @Bind(R.id.top_tv)
        TextView topTv;
        @Bind(R.id.country_tv)
        TextView countryTv;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
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

}
