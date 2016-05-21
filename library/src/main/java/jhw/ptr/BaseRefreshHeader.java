package jhw.ptr;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by jihongwen on 16/5/19.
 */
public class BaseRefreshHeader extends FrameLayout implements RefreshHandler {

    private final String LOG_TAG = BaseRefreshHeader.class.getSimpleName();

    private final boolean DEBUG = false;

    RefreshHandler mRefreshHandler;

    View mRefreshView;

    public BaseRefreshHeader(Context context) {
        super(context);
    }

    public BaseRefreshHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseRefreshHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addRefreshView(RefreshHandler refreshHandler) {
        mRefreshHandler = refreshHandler;
        if (refreshHandler.getHeaderView() != null && refreshHandler.getHeaderView() != mRefreshView) {
            if (mRefreshView != null) {
                removeView(mRefreshView);
            }
            mRefreshView = refreshHandler.getHeaderView();
        }
        addView(mRefreshView, LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onRefreshPullBegin() {
        if (DEBUG)
            Log.d(LOG_TAG, "onRefreshPullBegin");
        mRefreshHandler.onRefreshPullBegin();
    }

    @Override
    public void onRefreshPrepare() {
        if (DEBUG)
            Log.d(LOG_TAG, "onRefreshPrepare");
        mRefreshHandler.onRefreshPrepare();
    }

    @Override
    public void onRefreshing() {
        if (DEBUG)
            Log.d(LOG_TAG, "onRefreshing");
        mRefreshHandler.onRefreshing();
    }

    @Override
    public void onRefreshComplete() {
        if (DEBUG)
            Log.d(LOG_TAG, "onRefreshComplete");
        mRefreshHandler.onRefreshComplete();
    }

    @Override
    public void onRefreshChange(int offset) {
        if (DEBUG)
            Log.d(LOG_TAG, "onRefreshChange offset:" + offset);
        mRefreshHandler.onRefreshChange(offset);
    }

    @Override
    public void onRefreshChangePercent(float percent) {
        if (DEBUG)
            Log.d(LOG_TAG, "onRefreshChangePercent percent:" + percent);
        mRefreshHandler.onRefreshChangePercent(percent);
    }

    @Override
    public void onReturnToStart() {
        if (DEBUG)
            Log.d(LOG_TAG, "onReturnToStart");
        mRefreshHandler.onReturnToStart();
    }

    @Override
    public View getHeaderView() {
        return mRefreshHandler.getHeaderView();
    }

    @Override
    public float spinnerFinalOffset() {
        return mRefreshHandler.spinnerFinalOffset();
    }

    @Override
    public float totalDragDistance() {
        return mRefreshHandler.totalDragDistance();
    }

    @Override
    public int originalOffsetTop() {
        return mRefreshHandler.originalOffsetTop();
    }

    @Override
    public int getHeaderHeight() {
        return mRefreshHandler.getHeaderHeight();
    }
}
