package connect.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;

import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.widget.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi head display
 * Created by Administrator on 2016/12/15.
 */
public class AvatarGridView extends RelativeLayout {

    private final GridView gridView;
    private final Adapter adapter;
    private final RoundedImageView avatarRimg;
    /** Default spacing */
    private final int DEFAULT_SPACING = 5;
    private final RelativeLayout gridRela;

    public AvatarGridView(Context context) {
        this(context, null);
    }

    public AvatarGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(context, R.layout.avater_gridview, this);
        avatarRimg = (RoundedImageView)view.findViewById(R.id.avatar_rimg);
        gridRela = (RelativeLayout)view.findViewById(R.id.grid_rela);
        gridView = (GridView) view.findViewById(R.id.grid_view);
        gridView.setHorizontalSpacing(DEFAULT_SPACING);
        gridView.setVerticalSpacing(DEFAULT_SPACING);
        adapter = new Adapter();
        gridView.setAdapter(adapter);
    }

    public void setAvaterData(List<String> list) {
        if(list.size() == 1){
            avatarRimg.setVisibility(VISIBLE);
            gridRela.setVisibility(GONE);
            GlideUtil.loadAvater(avatarRimg,list.get(0));
            return;
        }

        avatarRimg.setVisibility(GONE);
        gridRela.setVisibility(VISIBLE);
        if (list.size() == 2 || list.size() == 4) {
            gridView.setNumColumns(2);
        } else {
            gridView.setNumColumns(3);
        }
        gridView.setPadding(DEFAULT_SPACING,DEFAULT_SPACING,DEFAULT_SPACING,DEFAULT_SPACING);
        adapter.setDataNotify(list);
    }

    class Adapter extends BaseAdapter {
        public ArrayList<String> listData = new ArrayList<>();

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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_avater_gridview, parent, false);
                viewHolder.avaterRimg = (HightEqWidthRounderImage)convertView.findViewById(R.id.avater_rimg);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder)convertView.getTag();
            }
            GlideUtil.loadAvater(viewHolder.avaterRimg,listData.get(position));
            return convertView;
        }

        private class ViewHolder {
            HightEqWidthRounderImage avaterRimg;
        }

        public void setDataNotify(List<String> list) {
            listData.clear();
            listData.addAll(list);
            notifyDataSetChanged();
        }

    }

}
