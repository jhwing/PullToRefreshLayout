package jhw.ptr;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
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
 * <p/>
 * 基于 {@link android.support.v4.widget.SwipeRefreshLayout}
 * 支持自定义header view
 */
public class PullToRefreshLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    private static final String LOG_TAG = PullToRefreshLayout.class.getSimpleName();

    private static final int INVALID_POINTER = -1;

    private static final float DRAG_RATE = .5f;

    private static final int DEFAULT_CIRCLE_TARGET = 64;

    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;

    private static final int ANIMATE_TO_START_DURATION = 500;

    private static final float DECELERATE_INTERPOLATION_FACTOR = 3f;

    private float mSpinnerFinalOffset;                           // 下拉组件最终可滑动偏移量

    private float mTotalDragDistance = -1;                       // 下拉刷新触发临界值
    private int mCurrentTargetOffsetTop = 0;                     // 当前下拉组件顶部坐标偏移量, mTarget 顶部坐标
    private int mOriginalOffsetTop = 0;                              // 下拉组件原始偏移量
    private int mActivePointerId = INVALID_POINTER;              // 触控点id
    private int mTouchSlop;                                      // 触发移动的最短距离
    private float mInitialMotionY;                               // 初始移动y坐标 加上 mTouchSlop
    private float mInitialDownY;                                 // 初始按下y坐标
    private float mTotalUnconsumed;                              // 未消耗的累加值
    private boolean mIsBeingDragged;                             // 是否开始拖动
    private boolean mRefreshing = false;

    protected int mFrom;

    private final DecelerateInterpolator mDecelerateInterpolator;

    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final int[] mParentScrollConsumed = new int[2];      // 父view滚动消耗
    private final int[] mParentOffsetInWindow = new int[2];      // 父view偏移量
    private boolean mNestedScrollInProgress;                     // 是否处在嵌套滑动处理中

    private BaseRefreshHeader mHeaderView;                       // header view 的容器
    private View mTarget;

    private OnRefreshListener mRefreshListener;

    public PullToRefreshLayout(Context context) {
        this(context, null);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // view 移动动画,先快后慢
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mSpinnerFinalOffset = DEFAULT_CIRCLE_TARGET * metrics.density;
        mTotalDragDistance = mSpinnerFinalOffset;
        mHeaderView = new BaseRefreshHeader(getContext());
        addView(mHeaderView);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
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
        }
        addView(mHeaderView);
        mHeaderView.addRefreshView(headerView);
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

        Log.d("jihongwen", "mCurrentTargetOffsetTop:" + mCurrentTargetOffsetTop);
        // 触发layout时child位置为实际滑动到的位置
        child.layout(childLeft, childTop + mCurrentTargetOffsetTop, childLeft + childWidth, childTop + childHeight + mCurrentTargetOffsetTop);
        mHeaderView.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
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
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        // 只接收垂直方向滚动
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        // Dispatch up to the nested parent
        startNestedScroll(nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mTotalUnconsumed = 0;
        mNestedScrollInProgress = true;
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollInProgress = false;
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        if (mTotalUnconsumed > 0) {
            finishSpinner(mTotalUnconsumed);
            mTotalUnconsumed = 0;
        }
        // Dispatch up our nested parent
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow);
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (dy < 0 && !canChildScrollUp()) {
            mTotalUnconsumed += Math.abs(dy);
            Log.d("jihongwen", "onNestedScroll");
            moveSpinner(mTotalUnconsumed);
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        // 先于子view滑动
        if (dy > 0 && mTotalUnconsumed > 0) {
            if (dy > mTotalUnconsumed) {
                consumed[1] = dy - (int) mTotalUnconsumed;
                mTotalUnconsumed = 0;
            } else {
                mTotalUnconsumed -= dy;
                consumed[1] = dy;
            }
            Log.d("jihongwen", "onNestedScroll");
            moveSpinner(mTotalUnconsumed);
        }

        // Now let our nested parent consume the leftovers
        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
                                        int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        if (canChildScrollUp()) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                mInitialDownY = initialDownY;
                // Log.d("jihongwen", "onInterceptTouchEvent:ACTION_DOWN  mInitialDownY" + mInitialDownY);
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

        if (canChildScrollUp() || mNestedScrollInProgress) {
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
                        Log.d("jihongwen", "onTouchEvent");
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

    public void setRefreshing(boolean refreshing, final boolean notify) {
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
                animateOffsetToCorrectPosition(mCurrentTargetOffsetTop);
            } else {
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
        // Log.d("jihongwen", "::::::::::moveSpinner:::::::::");

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

        if (overScrollTop > mTotalDragDistance) {
            // 松开刷新
            mHeaderView.onRefreshReady();
        } else {
            // 下拉刷新
            mHeaderView.onRefreshBegin();
        }

        int offset = targetY - mCurrentTargetOffsetTop;
        // targetY 减去 上次 header view的 getTop 值就是本次滑动需要移动的offset
        setTargetOffsetTopAndBottom(offset, true /* requires update */);

    }

    private void finishSpinner(float overscrollTop) {
        // 下拉刷新临界点
        if (overscrollTop > mTotalDragDistance) {
            // 正在刷新
            setRefreshing(true, true);
        } else {
            animateOffsetToStartPosition(mCurrentTargetOffsetTop);
        }

    }

    private void moveToStart(float interpolatedTime) {
        int targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
        int offset = targetTop - mTarget.getTop();
        setTargetOffsetTopAndBottom(offset, false /* requires update */);
    }

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int endTarget = (int) (mSpinnerFinalOffset - Math.abs(mOriginalOffsetTop));
            int targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mTarget.getTop();
            setTargetOffsetTopAndBottom(offset, false /* requires update */);
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

    /**
     * 回到起始位置
     */
    private void animateOffsetToStartPosition(int from) {
        mFrom = from;
        mHeaderView.onRefreshComplete();
        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        mTarget.startAnimation(mAnimateToStartPosition);
    }

    private void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
        mHeaderView.onRefreshChange(offset);
        mTarget.offsetTopAndBottom(offset);
        mCurrentTargetOffsetTop = mTarget.getTop();
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
