package jhw.ptr.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jhw.ptr.PullToRefreshLayout;
import jhw.ptr.RefreshHandler;

public class MainActivity extends AppCompatActivity {

    List<String> items = new ArrayList<>();

    PullToRefreshLayout refreshLayout;
    RecyclerView recyclerView;

    RefreshHandler refreshHandler;

    boolean is = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshLayout = (PullToRefreshLayout) findViewById(R.id.ptrLayout);
        refreshLayout.setPullRefresh(false);
        refreshHandler = new RefreshHeaderView(this);
        refreshLayout.setHeaderView(refreshHandler, false);
        refreshLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }, 1500);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        items.add("test");
        items.add("test");
        items.add("test");
        items.add("test");
        items.add("test");
        items.add("test");
        items.add("test");
        items.add("test");
        items.add("test");
        recyclerView.setAdapter(new XAdapter());
    }

    class XAdapter extends RecyclerView.Adapter<XViewHolder> {

        @Override
        public XViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new XViewHolder(getLayoutInflater().inflate(R.layout.adapter_test_item_view, parent, false));
        }

        @Override
        public void onBindViewHolder(XViewHolder holder, int position) {
            holder.textView.setText(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
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
