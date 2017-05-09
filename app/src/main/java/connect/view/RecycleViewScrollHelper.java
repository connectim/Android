package connect.view;

import android.support.annotation.IntRange;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * RecycleViewScroll Monitor helper class
 */
public class RecycleViewScrollHelper extends RecyclerView.OnScrollListener {
    private RecyclerView mRvScroll = null;
    private OnScrollDirectionChangedListener mScrollDirectionChangedListener = null;
    //Monitoring event of sliding position change
    private OnScrollPositionChangedListener mScrollPositionChangedListener = null;
    //Whether or not to slide to the top and bottom
    private boolean mIsCheckTopBottomTogether = false;
    //Detect the top / bottom priority of the slide, and then detect the slider to the bottom
    private boolean mIsCheckTopFirstBottomAfter = false;
    //Detection of the bottom of the screen when the screen is full screen state
    private boolean mIsCheckBottomFullRecycle = false;
    //Detection of the top of the screen when the screen is full screen state
    private boolean mIsCheckTopFullRecycle = false;
    //Tolerance value allowed for top full screen detection
    private int mTopOffsetFaultTolerance = 0;
    //Tolerance value allowed at bottom full screen detection
    private int mBottomOffsetFaultTolerance = 0;

    private int mScrollDx = 0;
    private int mScrollDy = 0;

    private boolean isScrollTop = false;
    private boolean isScrollBottom = false;

    /**
     * RecycleView's sliding listening event is used to detect whether to slide to the top or slide to the bottom
     *
     * @param listener {@link OnScrollPositionChangedListener}Sliding position change monitoring event
     */
    public RecycleViewScrollHelper(OnScrollPositionChangedListener listener) {
        mScrollPositionChangedListener = listener;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (mScrollPositionChangedListener == null || recyclerView.getAdapter() == null || recyclerView.getChildCount() <= 0) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
            int lastItemPosition = linearManager.findLastVisibleItemPosition();
            int firstItemPosition = linearManager.findFirstVisibleItemPosition();
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                //Top / bottom detection priority
                if (!mIsCheckTopFirstBottomAfter) {
                    //First detect the bottom
                    if (this.checkIfScrollToBottom(recyclerView, lastItemPosition, adapter.getItemCount())) {
                        //If the slide is detected in the end, determine whether the need to detect sliding to the top
                        if (mIsCheckTopBottomTogether) {
                            //Detect whether to slide to the top
                            this.checkIfScrollToTop(recyclerView, firstItemPosition);
                            return;
                        } else {
                            return;
                        }
                    } else if (this.checkIfScrollToTop(recyclerView, firstItemPosition)) {
                        return;
                    }
                } else {
                    if (this.checkIfScrollToTop(recyclerView, firstItemPosition)) {
                        if (mIsCheckTopBottomTogether) {
                            this.checkIfScrollToBottom(recyclerView, lastItemPosition, adapter.getItemCount());
                            return;
                        } else {
                            return;
                        }
                    } else if (this.checkIfScrollToBottom(recyclerView, lastItemPosition, adapter.getItemCount())) {
                        return;
                    }
                }
            }
        }
        mScrollPositionChangedListener.onScrollToUnknown(false, false);
    }

    /**
     * Detects whether the slide to the top of the item and callback events
     *
     * @param recyclerView
     * @param firstItemPosition
     * @return
     */
    private boolean checkIfScrollToTop(RecyclerView recyclerView, int firstItemPosition) {
        isScrollTop = false;
        if (firstItemPosition == 0) {
            if (mIsCheckTopFullRecycle) {
                int childCount = recyclerView.getChildCount();
                View firstChild = recyclerView.getChildAt(0);
                View lastChild = recyclerView.getChildAt(childCount - 1);
                int top = firstChild.getTop();
                int bottom = lastChild.getBottom();
                int topEdge = recyclerView.getPaddingTop() - mTopOffsetFaultTolerance;
                int bottomEdge = recyclerView.getHeight() - recyclerView.getPaddingBottom() - mBottomOffsetFaultTolerance;
                if (top >= topEdge && bottom > bottomEdge) {
                    mScrollPositionChangedListener.onScrollToTop();
                    isScrollTop = true;
                } else {
                    mScrollPositionChangedListener.onScrollToUnknown(true, false);
                }
            } else {
                mScrollPositionChangedListener.onScrollToTop();
                isScrollTop = true;
            }
        }
        return isScrollTop;
    }

    /**
     * Detect whether to slide to the bottom of the item and callback events
     *
     * @param recyclerView
     * @param lastItemPosition
     * @param itemCount
     * @return
     */
    private boolean checkIfScrollToBottom(RecyclerView recyclerView, int lastItemPosition, int itemCount) {
        isScrollBottom = false;
        if (lastItemPosition + 1 == itemCount) {
            if (mIsCheckBottomFullRecycle) {
                int childCount = recyclerView.getChildCount();
                View lastChildView = recyclerView.getChildAt(childCount - 1);
                View firstChildView = recyclerView.getChildAt(0);
                int top = firstChildView.getTop();
                int bottom = lastChildView.getBottom();
                int bottomEdge = recyclerView.getHeight() - recyclerView.getPaddingBottom() + mBottomOffsetFaultTolerance;
                int topEdge = recyclerView.getPaddingTop() + mTopOffsetFaultTolerance;
                if (bottom <= bottomEdge && top < topEdge) {
                    mScrollPositionChangedListener.onScrollToBottom();
                    isScrollBottom = true;
                } else {
                    mScrollPositionChangedListener.onScrollToUnknown(false, true);
                }
            } else {
                mScrollPositionChangedListener.onScrollToBottom();
                isScrollBottom = true;
            }
        }
        return isScrollBottom;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (mScrollDirectionChangedListener != null) {
            if (dx == 0 && dy == 0) {
                mScrollDirectionChangedListener.onScrollDirectionChanged(0, 0);
            } else if (dx == 0) {
                boolean isUp = dy > 0;
                boolean isBeenUp = mScrollDy > 0;
                if (isUp != isBeenUp) {
                    mScrollDx = dx;
                    mScrollDy = dy;
                    mScrollDirectionChangedListener.onScrollDirectionChanged(dx, dy);
                }
            } else if (dy == 0) {
                boolean isLeft = dx > 0;
                boolean isBeenLeft = mScrollDx > 0;
                if (isLeft != isBeenLeft) {
                    mScrollDx = dx;
                    mScrollDy = dy;
                    mScrollDirectionChangedListener.onScrollDirectionChanged(dx, dy);
                }
            }
        }
    }

    /**
     * Sliding to the specified location
     *
     * @param posi Starting from 0 to calculate
     */
    public void scrollToPosition(int posi) {
        scrollToPosition(posi, 0);
    }

    public void scrollToPosition(int posi, int top) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mRvScroll.getLayoutManager();
        layoutManager.scrollToPositionWithOffset(posi - 1, top);// -1==>Starting from 0 to calculate
    }

    //Reset the data
    private void reset() {
        mScrollDx = 0;
        mScrollDy = 0;
    }

    /**
     * whether to slide to the bottom of the item and callback events
     *
     * @param recyclerView
     */
    public void attachToRecycleView(RecyclerView recyclerView) {
        if (mRvScroll != recyclerView) {
            unAttachToRecycleView();
        }
        mRvScroll = recyclerView;
        if (recyclerView != null) {
            recyclerView.addOnScrollListener(this);
        }
    }

    /**
     * Remove binding with recycleView
     */
    public void unAttachToRecycleView() {
        if (mRvScroll != null) {
            mRvScroll.removeOnScrollListener(this);
        }
        this.reset();
    }

    /**
     * Set the callback interface when the sliding direction changes
     *
     * @param listener
     */
    public void setScrollDirectionChangedListener(OnScrollDirectionChangedListener listener) {
        mScrollDirectionChangedListener = listener;
    }

    /**
     * The tolerance setting the top allowable offset value, this value only in allowing the detection of effective full screen, when the {@link #setCheckIfItemViewFullRecycleViewForTop (Boolean) is set to true or {@link #setCheckIfItemViewFullRecycleViewForBottom} (Boolean)} set to true >.<br/
     * in the detection of the bottom sliding, the top of the test will be added to this capacity difference (easier to judge the current first childView has exceeded the scope of the recycleView), to help determine whether the slide to the bottom
     * when detecting the top slide, the value of the difference is added to the top (which is easier to judge to the top)
     *
     * @param offset Tolerance values, the value must be zero or positive
     */
    public void setTopOffsetFaultTolerance(@IntRange(from = 0) int offset) {
        mTopOffsetFaultTolerance = offset;
    }

    /**
     * The tolerance setting the top allowable offset value, this value only in allowing the detection of effective full screen, when the {@link #setCheckIfItemViewFullRecycleViewForTop (Boolean) is set to true or {@link #setCheckIfItemViewFullRecycleViewForBottom} (Boolean)} set to true >.<br/
     * when detecting the bottom slide, the bottom of the test will be added to this capacity difference (easier to judge the current childView has exceeded the recycleView display range), to help determine whether to slide to the top
     * when detecting the top slide, the value of the difference is added to the bottom (which is more likely to be judged to slide to the bottom)
     *
     * @param offset Tolerance values, the value must be zero or positive
     */
    public void setBottomFaultTolerance(@IntRange(from = 0) int offset) {
        mBottomOffsetFaultTolerance = offset;
    }

    /**
     * Set whether you need to check whether the recycleView is full screen itemView callback event.<br/>
     * <p/>
     * when the number of childView RecycleView is small, it is possible that the RecycleView has shown all the itemView, and there is no possibility of upward sliding.<br/>
     * if the current value is set to true, the RecycleView will not be able to fully display all of the itemView, the callback will be sliding to the top of the event; otherwise it will not be processed; <br/>
     * if it is set to false, the sliding event will be returned whenever the slide and the top item display
     *
     * @param isNeedToCheck True for when testing whether full screen display; False detection, the callback event directly
     */
    public void setCheckIfItemViewFullRecycleViewForTop(boolean isNeedToCheck) {
        mIsCheckTopFullRecycle = isNeedToCheck;
    }

    /**
     * Set whether you need to check whether the recycleView is full screen itemView callback event.</br>
     * <p/>
     * when the number of childView RecycleView is small, it is possible that RecycleView has shown all the itemView, and there is no possibility of a downward slide
     * if the current value is set to true, the RecycleView will not be able to fully display all of the itemView, it will call back to the bottom of the event; otherwise it will not handle;
     * if set to false otherwise, no matter what time, as long as the slide to the bottom of the callback event
     *
     * @param isNeedToCheck True for the detection of full screen display; false does not detect, direct callback event
     */
    public void setCheckIfItemViewFullRecycleViewForBottom(boolean isNeedToCheck) {
        mIsCheckBottomFullRecycle = isNeedToCheck;
    }

    /**
     * Whether to test where sliding. The default is false, detection of slide to the bottom first
     *
     * @param isTopFirst true :First inspection slide again to the top slide to the bottom; False first slide to the bottom to slide to the top
     */
    public void setCheckScrollToTopFirstBottomAfter(boolean isTopFirst) {
        mIsCheckTopFirstBottomAfter = isTopFirst;
    }

    /**
     * Set whether to detect the slide to the top and bottom, the default is false, first detected any state will be returned directly, will not continue to detect other states
     *
     * @param isCheckTogether True is detected for all two states, and even if a state of.False has been detected, no state will be detected when it detects any state first
     */
    public void setCheckScrollToTopBottomTogether(boolean isCheckTogether) {
        mIsCheckTopBottomTogether = isCheckTogether;
    }

    /**
     * The sliding position changes the listening event, sliding to the top / bottom or not above two positions
     */
    public interface OnScrollPositionChangedListener {

        void onScrollToTop();

        void onScrollToBottom();

        void onScrollToUnknown(boolean isTopViewVisible, boolean isBottomViewVisible);
    }

    /**
     * Monitor event when sliding direction changes
     */
    public interface OnScrollDirectionChangedListener {
        /**
         * When the direction of the sliding direction is monitored, when the two parameter values are all 0, the data changes layout
         *
         * @param scrollVertical   The vertical direction of the sliding direction, up >0, down <0, motionless (horizontal sliding) =0
         * @param scrollHorizontal Horizontal direction of sliding, left >0, right <0, motionless (vertical sliding) =0
         */
        void onScrollDirectionChanged(int scrollHorizontal, int scrollVertical);
    }

    public boolean isScrollTop() {
        return isScrollTop;
    }

    public boolean isScrollBottom() {
        return isScrollBottom;
    }

    public void setScrollBottom(boolean scrollBottom) {
        isScrollBottom = scrollBottom;
    }
}
