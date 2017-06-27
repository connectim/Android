package connect.ui.activity.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import connect.view.roundedimageview.RoundedImageView;

import java.util.List;

/**
 * Created by gtq on 2016/11/28.
 */
public class PickHoriScrollAdapter {
    private Context context;
    private List<String> paths;

    private LayoutInflater inflate;

    public PickHoriScrollAdapter(Context context, List<String> paths) {
        this.context = context;
        this.paths = paths;
        inflate = LayoutInflater.from(context);
    }

    public int getCount(){
        return paths.size();
    }

    public String getItemObj(int position) {
        return paths.get(position);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        PickHolder holder = null;
        if (convertView == null) {
            convertView = inflate.inflate(R.layout.item_pickimg, parent, false);
            holder = new PickHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (PickHolder) convertView.getTag();
        }

        String path = paths.get(position);
        GlideUtil.loadAvater(holder.roundimg, path);
        return convertView;
    }

    static class PickHolder {
        RoundedImageView roundimg;
        ImageView state;

        public PickHolder(View view){
            roundimg= (RoundedImageView) view.findViewById(R.id.roundimg);
            state= (ImageView) view.findViewById(R.id.state);
        }
    }
}