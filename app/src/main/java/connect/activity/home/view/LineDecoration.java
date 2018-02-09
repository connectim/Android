package connect.activity.home.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import connect.ui.activity.R;
import connect.utils.data.ResourceUtil;

/**
 * RecyclerView dividing line
 * Created by gtq on 2016/11/22.
 */
public class LineDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDrawable;

    public LineDecoration(Context context) {
        mDrawable = ResourceUtil.getDrawable(context, R.drawable.divider_recycler, null);
    }

    public LineDecoration(Context context, boolean isBold) {
        mDrawable = ResourceUtil.getDrawable(context, R.drawable.divider_recucle_8px, null);
    }

    public LineDecoration(Context context, Drawable drawable) {
        mDrawable = drawable;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        // The last entry does not set divider padding
        final int childCount = parent.getChildCount() - 1;
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDrawable.getIntrinsicHeight();
            mDrawable.setBounds(left, top, right, bottom);
            mDrawable.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int childAdapterPosition = parent.getChildAdapterPosition(view);
        int lastCount = parent.getAdapter().getItemCount() - 1;
        // The current entry is the last entry, and the divider padding is not set
        if (childAdapterPosition == lastCount) {
            outRect.set(0, 0, 0, 0);
        } else {
            outRect.set(0, 0, 0, mDrawable.getIntrinsicHeight());
        }
    }
}
