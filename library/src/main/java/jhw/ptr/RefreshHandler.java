package jhw.ptr;

import android.view.View;

/**
 * Created by jihongwen on 16/5/19.
 */
public interface RefreshHandler {

    void onRefreshBegin();

    void onRefreshReady();

    void onRefreshing();

    void onRefreshComplete();

    void onRefreshChange(int offset);

    void onRefreshChangePercent(float percent);

    View getView();
}
