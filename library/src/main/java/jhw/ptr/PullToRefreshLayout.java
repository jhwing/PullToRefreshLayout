package jhw.ptr;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;

/**
 * Created by jihongwen on 16/5/11.
 * <p>
 * 基于 {@link android.support.v4.widget.SwipeRefreshLayout}
 * 支持自定义header view
 * <p>
 * 刷新方式，自动刷新和松开手指刷新
 */
public class PullToRefreshLayout extends ViewGroup {

    private static final String LOG_TAG = PullToRefreshLayout.class.getSimpleName();

    private static final boolean DEBUG = true;

    private static final int INVALID_POINTER = -1;

    private static final float DRAG_RATE = .5f;

    private static final int DEFAULT_CIRCLE_TARGET = 100;

    private static final int ANIMATE_TO_TRIGGER_DURATION = 300;

    private static final int ANIMATE_TO_START_DURATION = 500;    // 回到起始位置用时

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    private float mSpinnerFinalOffset;                           // 下拉组件最终可滑动偏移量

    private float mTotalDragDistance = -1;                       // 下拉刷新触发临界值
    private int mCurrentTargetOffsetTop = 0;                     // 当前下拉组件顶部坐标偏移量, mTarget 顶部坐标
    private int mHeaderOffsetTop = 0;                     // 当前下拉组件顶部坐标偏移量, mTarget 顶部坐标
    private int mOriginalOffsetTop = 0;                              // 下拉组件原始偏移量
    private int mActivePointerId = INVALID_POINTER;              // 触控点id
    private int mTouchSlop;                                      // 触发移动的最短距离
    private float mInitialMotionY;                               // 初始移动y坐标 加上 mTouchSlop
    private float mInitialDownY;                                 // 初始按下y坐标
    private float mTotalUnconsumed;                              // 未消耗的累加值
    private boolean mIsBeingDragged;                             // 是否开始拖动
    private boolean mRefreshing = false;
    private boolean mIsPullRefresh = false;
    private boolean mIsHeaderPullRefresh = true;
    private boolean mReturningToStart = false;

    private int mFrom;

    private final DecelerateInterpolator mDecelerateInterpolator;

    private final int[] mParentScrollConsumed = new int[2];      // 父view滚动消耗
    private final int[] mParentOffsetInWindow = new int[2];      // 父view偏移量

    private BaseRefreshHeader mHeaderView;                       // header view 的容器
    private View mTarget;

    private OnRefreshListener mRefreshListener;

    public PullToRefreshLayout(Context context) {
        this(context, null);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mSpinnerFinalOffset = DEFAULT_CIRCLE_TARGET * metrics.density;
        mTotalDragDistance = mSpinnerFinalOffset;
        mHeaderView = new BaseRefreshHeader(getContext());
        addView(mHeaderView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    public void setHeaderView(RefreshHandler headerView) {
        mHeaderView.addRefreshView(headerView);
    }

    /**
     * header view 是否在 mTarget 之上
     *
     * @param headerView
     * @param bringToFront
     */
    public void setHeaderView(RefreshHandler headerView, boolean bringToFront) {
        if (bringToFront) {
            if (mHeaderView != null) {
                removeView(mHeaderView);
            }
            addView(mHeaderView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            mHeaderView.addRefreshView(headerView);
        } else {
            mHeaderView.addRefreshView(headerView);
        }
        mSpinnerFinalOffset = mHeaderView.spinnerFinalOffset();
        mTotalDragDistance = mHeaderView.totalDragDistance();
        mHeaderOffsetTop = mHeaderView.originalOffsetTop();
    }

    public void setPullRefresh(boolean isPullRefresh) {
        mIsPullRefresh = isPullRefresh;
    }

    /**
     * header view 是否可滑动，默认为可以滑动，一般header 在顶部屏幕之外时，应该可以滑动，如果在mTarget后面可以不滑动
     *
     * @param isHeaderPullRefresh
     */
    public void setHeaderPullRefresh(boolean isHeaderPullRefresh) {
        mIsHeaderPullRefresh = isHeaderPullRefresh;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        ensureTarget();
        if (getChildCount() == 0) {
            return;
        }
        if (mTarget == null) {
            return;
        }

        final View child = mTarget;
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();

        // 当onLayout被调用时,确保child位置为实际滑动到的位置
        child.layout(childLeft, childTop + mCurrentTargetOffsetTop, childLeft + childWidth, childTop + childHeight + mCurrentTargetOffsetTop);

        final int left = childLeft;
        final int top = (mHeaderOffsetTop + childTop);
        final int right = left + mHeaderView.getMeasuredWidth();
        final int bottom = top + mHeaderView.getMeasuredHeight();
        mHeaderView.layout(left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ensureTarget();
        if (mTarget == null) {
            return;
        }

        mTarget.measure(MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));

        final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight(), mHeaderView.getLayoutParams().width);
        final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(), mHeaderView.getLayoutParams().height);
        mHeaderView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (mRefreshing || mReturningToStart || canChildScrollUp()) {
            return false;
        }


        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                mInitialDownY = initialDownY;
                break;
            case MotionEvent.ACTION_MOVE:
                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                final float yDiff = y - mInitialDownY;
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mInitialMotionY = mInitialDownY + mTouchSlop;
                    mIsBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (mRefreshing || mReturningToStart || canChildScrollUp()) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_MOVE: {
                pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float overScrollTop = (y - mInitialMotionY) * DRAG_RATE; // 滑动偏移量
                if (mIsBeingDragged) {
                    if (overScrollTop > 0) {
                        if (DEBUG)
                            Log.d(LOG_TAG, "onTouchEvent");
                        moveSpinner(overScrollTop);
                    } else {
                        return false;
                    }
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP: {
                pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float overScrollTop = (y - mInitialMotionY) * DRAG_RATE;
                mIsBeingDragged = false;
                finishSpinner(overScrollTop);
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                return false;
        }
        return true;
    }

    public void setRefreshing(boolean refreshing) {
        if (refreshing && mRefreshing != refreshing) {
            // 开始刷新
            setRefreshing(refreshing, true);
        } else {
            setRefreshing(refreshing, false);
        }
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (mRefreshing != refreshing) {
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                mHeaderView.onRefreshing();
                if (notify) {
                    if (mRefreshListener != null) {
                        mRefreshListener.onRefresh();
                    }
                }
                if (!mIsPullRefresh) {
                    animateOffsetToCorrectPosition(mCurrentTargetOffsetTop);
                }
            } else {
                mHeaderView.onRefreshComplete();
                animateOffsetToStartPosition(mCurrentTargetOffsetTop);
            }
        }
    }

    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    private void moveSpinner(float overScrollTop) {
        if (mReturningToStart) {
            return;
        }
        // 滑动距离和可滑动距离比值  滑动距离等于 mTotalDragDistance 时 到达临界点，比值为1 值为1 时可以刷新
        float originalDragPercent = overScrollTop / mTotalDragDistance;
        // 比值小于1 或者等于1
        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
        mHeaderView.onRefreshChangePercent(dragPercent);
        // 比值减 0.4 后乘以 1.6
        // float adjustedPercent = (float) Math.max(dragPercent - .4, 0) * 5 / 3;
        // 滑动距离 和 可刷新距离的差值  extraOS 不计算阻力的额外滑动距离  extraMove 计算阻力后的额外滑动距离  根据 extraOS 计算阻力值
        float extraOS = Math.abs(overScrollTop) - mTotalDragDistance;
        // 下拉组件最大可滑动距离，默认和mTotalDragDistance一样 192
        float slingshotDist = mSpinnerFinalOffset;
        // 滑动距离大于最大可滑动距离时，比值不为零  tensionSlingshotPercent 值为 extraOS／slingshotDist
        // 当extraOS 大于 slingshotDist * 2 比值恒定为2
        float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, slingshotDist * 2) / slingshotDist);
        // 阻力
        float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow((tensionSlingshotPercent / 4), 2)) * 2f;
        // 额外的滑动距离, 最大到 slingshotDist 的一半乘2
        float extraMove = (slingshotDist) * tensionPercent * 2;
        // 滑动距离大于mTotalDragDistance 时 dragPercent 等于1
        // mm 等于header view 从top算起的实际移动距离, 当达到最大滑动距离时，变成恒定值,
        // 当滑动到可以刷新的位置时, dragPercent 等于1
        int mm = (int) ((slingshotDist * dragPercent) + extraMove);
        int targetY = mOriginalOffsetTop + mm;

        // 超过下拉刷新临界值
        if (overScrollTop > mTotalDragDistance) {
            mHeaderView.onRefreshPrepare();
            if (mIsPullRefresh && !mRefreshing) {
                setRefreshing(true, true);
            }
        } else {
            // 下拉刷新开始
            mHeaderView.onRefreshPullBegin();
        }

        int offset = targetY - mCurrentTargetOffsetTop;
        // targetY 减去 上次 header view的 getTop 值就是本次滑动需要移动的offset
        setTargetOffsetTopAndBottom(offset, true /* requires update */);

    }

    private void finishSpinner(float overscrollTop) {

        // 下拉刷新临界点
        if (overscrollTop > mTotalDragDistance) {
            if (mIsPullRefresh) {
                // 已经在刷新了
                animateOffsetToCorrectPosition(mCurrentTargetOffsetTop);
                return;
            }
            // 正在刷新
            setRefreshing(true, true);
        } else {
            mRefreshing = false;
            mHeaderView.onRefreshComplete();
            animateOffsetToStartPosition(mCurrentTargetOffsetTop);
        }

    }

    /**
     * 回到起始位置
     * 最终会回到mOriginalOffsetTop的位置
     *
     * @param interpolatedTime 0.0 到 1.0之间
     */
    private void moveToStart(float interpolatedTime) {
        int targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
        int offset = targetTop - mTarget.getTop();
        if (offset == 0 && interpolatedTime == 1) {// 偏移量位0,并且动画执行完成
            mHeaderView.onReturnToStart();
        }
        float originalDragPercent = targetTop / mTotalDragDistance;
        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
        mHeaderView.onRefreshChangePercent(dragPercent);
        setTargetOffsetTopAndBottom(offset, true /* requires update */);
    }

    /**
     * 回到起始位置移动动画
     */
    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    /**
     * 回到起始位置
     */
    private void animateOffsetToStartPosition(int from) {
        mReturningToStart = true;
        mFrom = from;
        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        mTarget.startAnimation(mAnimateToStartPosition);
    }

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int endTarget = (mHeaderView.getHeaderHeight() - Math.abs(mOriginalOffsetTop));
            int targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mTarget.getTop();
            setTargetOffsetTopAndBottom(offset, true /* requires update */);
        }
    };

    private void animateOffsetToCorrectPosition(int from) {
        mFrom = from;
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        mTarget.clearAnimation();
        mTarget.startAnimation(mAnimateToCorrectPosition);
    }

    private void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
        mHeaderView.onRefreshChange(offset);
        mTarget.offsetTopAndBottom(offset);
        mCurrentTargetOffsetTop = mTarget.getTop();
        if (mIsHeaderPullRefresh) {
            mHeaderView.offsetTopAndBottom(offset);
            mHeaderOffsetTop = mHeaderView.getTop();
        }
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mHeaderView)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mRefreshListener = listener;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }
}
