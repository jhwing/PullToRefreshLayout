package jhw.ptr.demo;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import jhw.ptr.RefreshHandler;

/**
 * Created by jihongwen on 16/5/20.
 */
public class DropHeaderLayout extends RelativeLayout implements RefreshHandler {

    DropView mDropView;

    int h;

    public DropHeaderLayout(Context context) {
        super(context);
        init(context);
    }

    public DropHeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DropHeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        final DisplayMetrics metrics = getResources().getDisplayMetrics();

        mDropView = new DropView(context);
        addView(mDropView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) mDropView.getLayoutParams();
        layoutParams.width = (int) (60 * metrics.density);
        layoutParams.height = (int) (60 * metrics.density);
        h = (int) (60 * metrics.density);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mDropView.setLayoutParams(layoutParams);
        mDropView.setColor(Color.RED);
    }

    @Override
    public void onRefreshPullBegin() {
        mDropView.reset();
        mDropView.showLoadingIcon(true);
        mDropView.requestLayout();
    }

    @Override
    public void onRefreshPrepare() {
    }

    @Override
    public void onRefreshing() {
        mDropView.onLoading();
    }

    @Override
    public void onRefreshComplete() {
    }

    int myOffset = 0;

    @Override
    public void onRefreshChange(int offset) {
        myOffset += offset;
        if (getTop() >= 0) {
            int hh = mDropView.getLayoutParams().height;
            mDropView.getLayoutParams().height = hh + offset;
            LayoutParams layoutParams = (LayoutParams) mDropView.getLayoutParams();
            mDropView.setDistanceY(-((int) ((myOffset - 150) * 0.9f)));
            mDropView.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onRefreshChangePercent(float percent) {

    }

    @Override
    public void onReturnToStart() {
        mDropView.reset();
        mDropView.getLayoutParams().height = h;
    }

    @Override
    public View getHeaderView() {
        return this;
    }

    @Override
    public float spinnerFinalOffset() {
        return (float) (h + 50);
    }

    @Override
    public float totalDragDistance() {
        return (float) (h + 50);
    }

    @Override
    public int originalOffsetTop() {
        return -h;
    }

    @Override
    public int getHeaderHeight() {
        return h;
    }
}
