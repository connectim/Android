package connect.activity.login.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.widget.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by john on 2016/11/27.
 */

public class AcountAdapter extends BaseAdapter {

    private OnChildClickListence onChildClickListence;
    private ArrayList<UserBean> listData = new ArrayList<>();
    private int index;
    private boolean isShowClose;

    public AcountAdapter(OnChildClickListence onChildClickListence) {
        this.onChildClickListence = onChildClickListence;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_seleaccount_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if (position % 2 == 0) {
            convertView.setBackgroundResource(R.color.color_cc999999);
        } else {
            convertView.setBackgroundResource(R.color.color_ccc0c0c0);
        }

        if(isShowClose){
            viewHolder.imageView.setVisibility(View.VISIBLE);
            viewHolder.imageView.setImageResource(R.mipmap.input_delete);
            viewHolder.imageView.setClickable(true);
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onChildClickListence.onCloseClick(listData.get(position));
                    listData.remove(position);
                    ArrayList arrayList = new ArrayList<>();
                    arrayList.addAll(listData);
                    setDataNotify(arrayList,0,false);
                }
            });
        }else{
            viewHolder.imageView.setVisibility(View.GONE);
            if(position == index){
                viewHolder.imageView.setVisibility(View.VISIBLE);
                viewHolder.imageView.setImageResource(R.mipmap.success_message);
                viewHolder.imageView.setClickable(false);
            }
        }



        return convertView;
    }

    public void setDataNotify(List list,int index,boolean isShowClose) {
        this.index = index;
        this.isShowClose = isShowClose;
        listData.clear();
        listData.addAll(list);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        @Bind(R.id.roundimg_head)
        RoundedImageView roundimgHead;
        @Bind(R.id.text_view)
        TextView textView;
        @Bind(R.id.image_view)
        ImageView imageView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public interface OnChildClickListence{

        void onCloseClick(UserBean accountBean);

    }

}
