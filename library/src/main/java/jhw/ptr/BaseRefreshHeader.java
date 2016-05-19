package jhw.ptr;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by jihongwen on 16/5/19.
 */
public class BaseRefreshHeader extends FrameLayout implements RefreshHandler {

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
        if (refreshHandler.getView() != null && refreshHandler.getView() != mRefreshView) {
            if (mRefreshView != null) {
                removeView(mRefreshView);
            }
            mRefreshView = refreshHandler.getView();
        }
        addView(mRefreshView);
    }

    @Override
    public void onRefreshBegin() {
        mRefreshHandler.onRefreshBegin();
    }

    @Override
    public void onRefreshReady() {
        mRefreshHandler.onRefreshReady();
    }

    @Override
    public void onRefreshing() {
        mRefreshHandler.onRefreshing();
    }

    @Override
    public void onRefreshComplete() {
        mRefreshHandler.onRefreshComplete();
    }

    @Override
    public void onRefreshChange(int offset) {
        mRefreshHandler.onRefreshChange(offset);
    }

    @Override
    public void onRefreshChangePercent(float percent) {
        mRefreshHandler.onRefreshChangePercent(percent);
    }

    @Override
    public View getView() {
        return mRefreshHandler.getView();
    }
}
