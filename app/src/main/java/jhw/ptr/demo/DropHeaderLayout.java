package jhw.ptr.demo;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import jhw.ptr.RefreshHandler;

/**
 * Created by jihongwen on 16/5/20.
 */
public class DropHeaderLayout extends RelativeLayout implements RefreshHandler {

    DropView mDropView;

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
        addView(mDropView);
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) mDropView.getLayoutParams();
        layoutParams.width = (int) (200 * metrics.density);
        layoutParams.height = 0;
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mDropView.setLayoutParams(layoutParams);
        mDropView.setColor(Color.RED);

    }

    @Override
    public void onRefreshBegin() {
        mDropView.reset();
        mDropView.showLoadingIcon(true);
    }

    @Override
    public void onRefreshReady() {

    }

    @Override
    public void onRefreshing() {
        mDropView.onLoading();
    }

    @Override
    public void onRefreshComplete() {
//        mDropView.reset();
    }

    int myOffset = 0;

    @Override
    public void onRefreshChange(int offset) {
        myOffset += offset;
        Log.d("jihongwen", "myOffset:" + myOffset);
        int h = mDropView.getLayoutParams().height;
        mDropView.getLayoutParams().height = h + offset;
        mDropView.setDistanceY(-((int) ((myOffset - 150) * 0.9f)));
        mDropView.requestLayout();
    }

    @Override
    public void onRefreshChangePercent(float percent) {

    }

    @Override
    public View getView() {
        return this;
    }
}
