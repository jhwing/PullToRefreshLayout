package jhw.ptr.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import jhw.ptr.header.DropHeaderLayout;

/**
 * Created by jihongwen on 16/5/24.
 */
public class DropViewFragment extends BaseRefreshFragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHeaderView(new DropHeaderLayout(getActivity()), true);
    }
}
