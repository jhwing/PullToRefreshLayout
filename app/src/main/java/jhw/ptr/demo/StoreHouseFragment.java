package jhw.ptr.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import jhw.ptr.header.StoreHouseHeader;

/**
 * Created by jihongwen on 16/5/24.
 */
public class StoreHouseFragment extends BaseRefreshFragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StoreHouseHeader header = new StoreHouseHeader(getActivity());
        header.initWithPointList(StoreHousePoints.getBaisiPointsList());
        setHeaderView(header, false);
    }
}
