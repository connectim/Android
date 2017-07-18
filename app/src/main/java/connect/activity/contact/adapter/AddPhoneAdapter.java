package connect.activity.contact.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.activity.base.BaseApplication;
import connect.utils.PinyinUtil;
import connect.activity.contact.bean.PhoneContactBean;
import connect.utils.glide.GlideUtil;
import connect.widget.roundedimageview.RoundedImageView;
import protos.Connect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/30.
 */
public class AddPhoneAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<PhoneContactBean> mDataList = new ArrayList<>();
    private List<PhoneContactBean> selectList = new ArrayList<>();
    private OnSeleListence onSeleListence;
    private int serverSize;

    private Activity activity;

    public enum ITEMTYPE {
        VIEW_TYP_TITLE,
        VIEW_TYP_SERVER,
        VIEW_TYP_LOAD
    }

    public AddPhoneAdapter(Activity activity){
        this.activity=activity;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View convertView = null;
        RecyclerView.ViewHolder holder = null;
        if (viewType == ITEMTYPE.VIEW_TYP_TITLE.ordinal()) {
            convertView = inflater.inflate(R.layout.item_contact_phone_title, parent, false);
            holder = new TitleViewHolder(convertView);
        } else if (viewType == ITEMTYPE.VIEW_TYP_SERVER.ordinal()) {
            convertView = inflater.inflate(R.layout.item_contact_phone_request, parent, false);
            holder = new ServerHolder(convertView);
        } else if (viewType == ITEMTYPE.VIEW_TYP_LOAD.ordinal()) {
            convertView = inflater.inflate(R.layout.item_contact_phone_local, parent, false);
            holder = new LoaderHolder(convertView);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (mDataList != null && mDataList.size() > 0) {
            final PhoneContactBean contactBean = mDataList.get(position);
            if(type==ITEMTYPE.VIEW_TYP_TITLE.ordinal()){
                ((TitleViewHolder)holder).titleTv.setText(R.string.Set_Installed);
            }else if(type==ITEMTYPE.VIEW_TYP_SERVER.ordinal()){
                GlideUtil.loadAvater(((ServerHolder)holder).avater,contactBean.getAvater());
                ((ServerHolder)holder).nameTvS.setText(contactBean.getName());
                ((ServerHolder)holder).nicName.setText(contactBean.getNickName());
                showStatus(position,contactBean,((ServerHolder)holder).addBtn);
            }if(type==ITEMTYPE.VIEW_TYP_LOAD.ordinal()){
                String lastName = "";
                if(position > 0 && !TextUtils.isEmpty(mDataList.get(position - 1).getName())){
                    lastName = PinyinUtil.chatToPinyin(mDataList.get(position - 1).getName().charAt(0));
                }
                String curName = PinyinUtil.chatToPinyin(contactBean.getName().charAt(0));
                if (lastName.equals(curName)) {
                    ((LoaderHolder) holder).txt.setVisibility(View.GONE);
                } else {
                    ((LoaderHolder) holder).txt.setVisibility(View.VISIBLE);
                    ((LoaderHolder) holder).txt.setText(curName);
                }

                if (selectList.contains(contactBean)) {
                    ((LoaderHolder) holder).select.setSelected(true);
                } else {
                    ((LoaderHolder) holder).select.setSelected(false);
                }
                ((LoaderHolder) holder).nameTv.setText(contactBean.getName());
                ((LoaderHolder) holder).phoneTv.setText(contactBean.getPhone());
                ((LoaderHolder) holder).itemView.setOnClickListener(itemListener);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (!TextUtils.isEmpty(mDataList.get(position).getAddress())) {
            return ITEMTYPE.VIEW_TYP_SERVER.ordinal();
        } else if (!TextUtils.isEmpty(mDataList.get(position).getPhone())) {
            return ITEMTYPE.VIEW_TYP_LOAD.ordinal();
        } else {
            return ITEMTYPE.VIEW_TYP_TITLE.ordinal();
        }
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {

        TextView titleTv;

        public TitleViewHolder(View itemView) {
            super(itemView);
            titleTv = (TextView) itemView.findViewById(R.id.title_tv);
        }
    }

    class ServerHolder extends RecyclerView.ViewHolder{

        RoundedImageView avater;
        TextView nameTvS;
        TextView nicName;
        Button addBtn;

        public ServerHolder(View itemView) {
            super(itemView);
            avater = (RoundedImageView)itemView.findViewById(R.id.avatar_rimg);
            nameTvS = (TextView)itemView.findViewById(R.id.nickname_tv);
            nicName = (TextView)itemView.findViewById(R.id.hint_tv);
            addBtn = (Button) itemView.findViewById(R.id.status_btn);
        }
    }

    class LoaderHolder extends RecyclerView.ViewHolder{

        TextView txt;
        View select;
        TextView nameTv;
        TextView phoneTv;

        public LoaderHolder(View itemView) {
            super(itemView);
            txt = (TextView)itemView.findViewById(R.id.txt);
            select = itemView.findViewById(R.id.select);
            nameTv = (TextView)itemView.findViewById(R.id.name_tv);
            phoneTv = (TextView)itemView.findViewById(R.id.phone_tv);
        }
    }

    private View.OnClickListener itemListener = new View.OnClickListener() {
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