package jhw.ptr;

import android.view.View;

/**
 * Created by jihongwen on 16/5/19.
 */
public interface RefreshHandler {

    /**
     * 开始下拉
     */
    void onRefreshPullBegin();

    /**
     * 准备刷新
     */
    void onRefreshPrepare();

    /**
     * 刷新中
     */
    void onRefreshing();

    /**
     * 刷新完成
     */
    void onRefreshComplete();

    /**
     * 下拉过程中每次view移动的偏移量
     *
     * @param offset
     */
    void onRefreshChange(int offset);

    /**
     * 下拉刷新变化的百分比,下拉偏移距离/下拉刷新总偏移量
     *
     * @param percent
     */
    void onRefreshChangePercent(float percent);

    /**
     * 回到原始的位置
     */
    void onReturnToStart();

    /**
     * 获取header view
     *
     * @return
     */
    View getHeaderView();

    float spinnerFinalOffset();

    float totalDragDistance();

    int originalOffsetTop();

    int getHeaderHeight();

}
