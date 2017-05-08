package connect.ui.activity.contact.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.ui.base.BaseApplication;
import connect.utils.PinyinUtil;
import connect.ui.activity.contact.bean.PhoneContactBean;
import connect.utils.glide.GlideUtil;
import connect.view.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/30.
 */
public class AddPhoneAdapter extends BaseAdapter {

    private ArrayList<PhoneContactBean> mDataList = new ArrayList<>();
    private List<PhoneContactBean> selectList = new ArrayList<>();
    private final int VIEW_TYP_TITLE = 100;
    private final int VIEW_TYP_SERVER = 101;
    private final int VIEW_TYP_LOAD = 102;
    private OnSeleListence onSeleListence;
    private int serverSize;

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
    public int getItemViewType(int position) {
        if (!TextUtils.isEmpty(mDataList.get(position).getAddress())) {
            return VIEW_TYP_SERVER;
        } else if (!TextUtils.isEmpty(mDataList.get(position).getPhone())) {
            return VIEW_TYP_LOAD;
        } else {
            return VIEW_TYP_TITLE;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);
        int tmp = 0;
        if (convertView != null) {
            tmp = (Integer) convertView.getTag(R.id.status_key);
        }
        if (convertView == null || tmp != type) {
            holder = new ViewHolder();
            switch (type) {
                case VIEW_TYP_TITLE:
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_phone_title, parent, false);
                    holder.titleTv = (TextView) convertView.findViewById(R.id.title_tv);
                    break;
                case VIEW_TYP_SERVER:
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_phone_request, parent, false);
                    holder.avater = (RoundedImageView)convertView.findViewById(R.id.avatar_rimg);
                    holder.nameTvS = (TextView)convertView.findViewById(R.id.nickname_tv);
                    holder.nicName = (TextView)convertView.findViewById(R.id.hint_tv);
                    holder.addBtn = (Button) convertView.findViewById(R.id.status_btn);
                    break;
                case VIEW_TYP_LOAD:
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_phone_local, parent, false);
                    holder.txt = (TextView)convertView.findViewById(R.id.txt);
                    holder.select = convertView.findViewById(R.id.select);
                    holder.nameTv = (TextView)convertView.findViewById(R.id.name_tv);
                    holder.phoneTv = (TextView)convertView.findViewById(R.id.phone_tv);
                    break;
            }
            if (convertView != null) {
                convertView.setTag(holder);
                convertView.setTag(R.id.status_key, type);
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mDataList != null && mDataList.size() > 0) {
            final PhoneContactBean contactBean = mDataList.get(position);
            switch (type) {
                case VIEW_TYP_TITLE:
                    holder.titleTv.setText(R.string.Set_Installed);
                    break;
                case VIEW_TYP_SERVER:
                    GlideUtil.loadAvater(holder.avater,contactBean.getAvater());
                    holder.nameTvS.setText(contactBean.getName());
                    holder.nicName.setText(contactBean.getNickName());
                    showStatus(position,contactBean,holder.addBtn);
                    break;
                case VIEW_TYP_LOAD:
                    String lastName = "";
                    if(position > 0 && !TextUtils.isEmpty(mDataList.get(position - 1).getName())){
                        lastName = PinyinUtil.chatToPinyin(mDataList.get(position - 1).getName().charAt(0));
                    }
                    String curName = PinyinUtil.chatToPinyin(contactBean.getName().charAt(0));
                    if (lastName.equals(curName)) {
                        holder.txt.setVisibility(View.GONE);
                    } else {
                        holder.txt.setVisibility(View.VISIBLE);
                        holder.txt.setText(curName);
                    }

                    if (selectList.contains(contactBean)) {
                        holder.select.setSelected(true);
                    } else {
                        holder.select.setSelected(false);
                    }
                    holder.nameTv.setText(contactBean.getName());
                    holder.phoneTv.setText(contactBean.getPhone());
                    convertView.setOnClickListener(itemClickListener);
                    convertView.setTag(R.id.position_key,position);
                    break;
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView titleTv;

        TextView txt;
        View select;
        TextView nameTv;
        TextView phoneTv;

        RoundedImageView avater;
        TextView nameTvS;
        TextView nicName;
        Button addBtn;

    }

    private View.OnClickListener itemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int)v.getTag(R.id.position_key);
            PhoneContactBean bean = mDataList.get(position);
            View secview = v.findViewById(R.id.select);
            if (selectList.contains(bean)) {
                selectList.remove(bean);
                secview.setSelected(false);
            } else {
                selectList.add(bean);
                secview.setSelected(true);
            }

            if(onSeleListence != null){
                onSeleListence.seleFriend(selectList);
            }
        }
    };

    private void showStatus(final int positon, final PhoneContactBean contactBean, Button view){
        switch (contactBean.getStatus()){//1：Did not add  2：Have been added 3：In the validation
            case 1:
                view.setText(R.string.Link_Add);
                view.setEnabled(true);
                view.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_ffffff));
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(onSeleListence != null){
                            onSeleListence.addFriend(positon,contactBean);
                        }
                    }
                });
                break;
            case 2:
                view.setText(R.string.Link_Added);
                view.setEnabled(false);
                view.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_858998));
                break;
            case 3:
                view.setText(R.string.Link_Verify);
                view.setEnabled(false);
                view.setTextColor(BaseApplication.getInstance().getResources().getColor(R.color.color_858998));
                break;
        }
    }

    public void setDataNotify(List<PhoneContactBean> list) {
        mDataList.clear();
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    public List<PhoneContactBean> getSeleList(){
         return selectList;
    }

    public void setOnSeleListence(OnSeleListence onSeleListence){
        this.onSeleListence = onSeleListence;
    }

    public void setServerSize(int size){
        this.serverSize = size;
    }

    public int getPositionForSection(char selectchar) {
        if(mDataList.size() - serverSize == 0)
            return -1;
        for (int i = serverSize; i < mDataList.size(); i++) {
            PhoneContactBean entity = mDataList.get(i);
            String showName = entity.getName();
            String firstChar = PinyinUtil.chatToPinyin(showName.charAt(0));
            if (firstChar.charAt(0) == selectchar) {
                return i;
            }
        }
        return -1;
    }

    public interface OnSeleListence {

        void seleFriend(List<PhoneContactBean> list);

        void addFriend(int position,PhoneContactBean contactBean);

    }

}
