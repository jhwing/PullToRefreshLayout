package jhw.ptr.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jhw.ptr.RefreshHandler;

/**
 * Created by jihongwen on 16/5/12.
 */
public class RefreshHeaderView extends RelativeLayout implements RefreshHandler {


    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;
    private int mState = STATE_NORMAL;

    private final int ROTATE_ANIM_DURATION = 180;

    TextView refreshTip;
    TextView refreshTime;

    ImageView refreshArrow;
    ProgressBar refreshProgress;

    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;

    public RefreshHeaderView(Context context) {
        this(context, null);
    }

    public RefreshHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.refresh_header_view, this);
        refreshTip = (TextView) findViewById(R.id.refreshTip);
        refreshTime = (TextView) findViewById(R.id.refreshTime);
        String formatTime = getResources().getString(R.string.refreshTime);
        refreshTime.setText(String.format(formatTime, 1));
        refreshArrow = (ImageView) findViewById(R.id.refreshArrow);
        refreshProgress = (ProgressBar) findViewById(R.id.refreshProgress);
        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);

    }

    @Override
    public void onRefreshBegin() {
        if (mState == STATE_NORMAL) {
            return;
        }
        mState = STATE_NORMAL;
        refreshTip.setText(getResources().getString(R.string.refreshTip));
        refreshArrow.clearAnimation();
        refreshArrow.startAnimation(mRotateDownAnim);
    }

    @Override
    public void onRefreshReady() {
        if (mState == STATE_READY) {
            return;
        }
        mState = STATE_READY;
        refreshTip.setText(getResources().getString(R.string.refreshTip_ready));
        refreshArrow.clearAnimation();
        refreshArrow.startAnimation(mRotateUpAnim);
    }

    @Override
    public void onRefreshing() {
        refreshTip.setText(getResources().getString(R.string.refreshTip_refreshing));
    }

    @Override
    public void onRefreshComplete() {

    }

    /**
     * 刷新移动偏移量
     *
     * @param offset
     */
    @Override
    public void onRefreshChange(int offset) {

    }

    /**
     * 刷新距离百分比
     *
     * @param dragPercent
     */
    @Override
    public void onRefreshChangePercent(float dragPercent) {
        float adjustedPercent = (float) Math.min(Math.max(dragPercent - .3, 0) * 2, 1f);
        Log.d("jihongwen", "adjustedPercent:" + adjustedPercent);
        refreshArrow.setScaleY(adjustedPercent);
        refreshArrow.setScaleX(adjustedPercent);
    }

    @Override
    public View getView() {
        return this;
    }
}
