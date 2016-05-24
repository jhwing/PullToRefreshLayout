package jhw.ptr.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jhw.ptr.PullToRefreshLayout;
import jhw.ptr.RefreshHandler;

/**
 * Created by jihongwen on 16/5/24.
 */
public abstract class BaseRefreshFragment extends Fragment {

    List<String> testDatas = new ArrayList<>();

    PullToRefreshLayout ptrLayout;
    RecyclerView recyclerList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_base_refresh, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getTestDatas();
        ptrLayout = (PullToRefreshLayout) view.findViewById(R.id.ptrLayout);
        recyclerList = (RecyclerView) view.findViewById(R.id.recyclerList);
        ptrLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ptrLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ptrLayout.setRefreshing(false);
                    }
                }, 1500);
            }
        });

        recyclerList.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerList.setAdapter(new XAdapter());

    }

    protected void setHeaderView(RefreshHandler refreshHandler, boolean bringToFront) {
        ptrLayout.setHeaderView(refreshHandler, bringToFront);
    }

    private void getTestDatas() {
        testDatas.add("test");
        testDatas.add("test");
        testDatas.add("test");
        testDatas.add("test");
        testDatas.add("test");
        testDatas.add("test");
        testDatas.add("test");
        testDatas.add("test");
        testDatas.add("test");
    }

    class XAdapter extends RecyclerView.Adapter<XViewHolder> {

        @Override
        public XViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new XViewHolder(getActivity().getLayoutInflater().inflate(R.layout.adapter_test_item_view, parent, false));
        }

        @Override
        public void onBindViewHolder(XViewHolder holder, int position) {
            holder.textView.setText(testDatas.get(position));
        }

        @Override
        public int getItemCount() {
            return testDatas.size();
        }
    }

    class XViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public XViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.title);
        }
    }
}
