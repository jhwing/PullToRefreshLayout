package jhw.ptr.demo;

import android.graphics.Point;
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
        StoreHouseHeader header = new StoreHouseHeader(this);
        header.initWithPointList(getPointList());
        refreshLayout.setHeaderView(header, false);
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


    private ArrayList<float[]> getPointList() {
        // this point is taken from https://github.com/cloay/CRefreshLayout
        List<Point> startPoints = new ArrayList<Point>();
        startPoints.add(new Point(240, 80));
        startPoints.add(new Point(270, 80));
        startPoints.add(new Point(265, 103));
        startPoints.add(new Point(255, 65));
        startPoints.add(new Point(275, 80));
        startPoints.add(new Point(275, 80));
        startPoints.add(new Point(302, 80));
        startPoints.add(new Point(275, 107));

        startPoints.add(new Point(320, 70));
        startPoints.add(new Point(313, 80));
        startPoints.add(new Point(330, 63));
        startPoints.add(new Point(315, 87));
        startPoints.add(new Point(330, 80));
        startPoints.add(new Point(315, 100));
        startPoints.add(new Point(330, 90));
        startPoints.add(new Point(315, 110));
        startPoints.add(new Point(345, 65));
        startPoints.add(new Point(357, 67));
        startPoints.add(new Point(363, 103));

        startPoints.add(new Point(375, 80));
        startPoints.add(new Point(375, 80));
        startPoints.add(new Point(425, 80));
        startPoints.add(new Point(380, 95));
        startPoints.add(new Point(400, 63));

        List<Point> endPoints = new ArrayList<Point>();
        endPoints.add(new Point(270, 80));
        endPoints.add(new Point(270, 110));
        endPoints.add(new Point(270, 110));
        endPoints.add(new Point(250, 110));
        endPoints.add(new Point(275, 107));
        endPoints.add(new Point(302, 80));
        endPoints.add(new Point(302, 107));
        endPoints.add(new Point(302, 107));

        endPoints.add(new Point(340, 70));
        endPoints.add(new Point(360, 80));
        endPoints.add(new Point(330, 80));
        endPoints.add(new Point(340, 87));
        endPoints.add(new Point(315, 100));
        endPoints.add(new Point(345, 98));
        endPoints.add(new Point(330, 120));
        endPoints.add(new Point(345, 108));
        endPoints.add(new Point(360, 120));
        endPoints.add(new Point(363, 75));
        endPoints.add(new Point(345, 117));

        endPoints.add(new Point(380, 95));
        endPoints.add(new Point(425, 80));
        endPoints.add(new Point(420, 95));
        endPoints.add(new Point(420, 95));
        endPoints.add(new Point(400, 120));
        ArrayList<float[]> list = new ArrayList<float[]>();

        int offsetX = Integer.MAX_VALUE;
        int offsetY = Integer.MAX_VALUE;

        for (int i = 0; i < startPoints.size(); i++) {
            offsetX = Math.min(startPoints.get(i).x, offsetX);
            offsetY = Math.min(startPoints.get(i).y, offsetY);
        }
        for (int i = 0; i < endPoints.size(); i++) {
            float[] point = new float[4];
            point[0] = startPoints.get(i).x - offsetX;
            point[1] = startPoints.get(i).y - offsetY;
            point[2] = endPoints.get(i).x - offsetX;
            point[3] = endPoints.get(i).y - offsetY;
            list.add(point);
        }
        return list;
    }
}
