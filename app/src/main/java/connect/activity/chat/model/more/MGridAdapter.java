package connect.activity.chat.model.more;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.activity.chat.bean.BaseAction;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by gtq on 2016/11/24.
 */
public class MGridAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<BaseAction> actionList;

    public MGridAdapter(Context context, List<BaseAction> actions) {
        layoutInflater = LayoutInflater.from(context);
        this.actionList = actions;
    }

    @Override
    public int getCount() {
        return actionList.size();
    }

    @Override
    public Object getItem(int position) {
        return actionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_chat_more, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BaseAction baseAction = actionList.get(position);
        viewHolder.img.setBackgroundResource(baseAction.getIconResId());
        viewHolder.txt.setText(layoutInflater.getContext().getResources().getString(baseAction.getTitleId()));
        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.img)
        ImageView img;
        @Bind(R.id.txt)
        TextView txt;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}