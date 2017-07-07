package connect.view.pullTorefresh;

import android.util.SparseArray;
import android.view.View;

/**
 * Created by 
 */
public class ViewHolder {
  public static <T extends View> T findViewById(View view, int id) {
    SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
    if (viewHolder == null) {
      viewHolder = new SparseArray<>();
      view.setTag(viewHolder);
    }
    View childView = viewHolder.get(id);
    if (childView == null) {
      childView = view.findViewById(id);
      viewHolder.put(id, childView);
    }
    return (T) childView;
  }
}
