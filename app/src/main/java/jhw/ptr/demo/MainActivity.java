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
//        refreshHandler = new RefreshHeaderView(this);
        refreshHandler = new DropHeaderLayout(this);
        StoreHouseHeader header = new StoreHouseHeader(this);
        header.initWithPointList(getBaisiPointsList());
        refreshLayout.setHeaderView(header, true);
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


    public ArrayList<float[]> getBaisiPointsList() {
        List<Point> startPoints = new ArrayList<Point>();
        List<Point> endPoints = new ArrayList<Point>();
        // 百 begin
        startPoints.add(new Point(3, 8));// 1
        endPoints.add(new Point(60, 8));

        startPoints.add(new Point(32, 15));// 2
        endPoints.add(new Point(28, 21));

        startPoints.add(new Point(13, 17));// 3
        endPoints.add(new Point(13, 55));

        startPoints.add(new Point(21, 23));// 4
        endPoints.add(new Point(51, 23));

        startPoints.add(new Point(51, 23));// 5
        endPoints.add(new Point(51, 54));

        startPoints.add(new Point(15, 38));// 6
        endPoints.add(new Point(36, 38));

        startPoints.add(new Point(15, 54));// 7
        endPoints.add(new Point(51, 54));
        // 百 end


        // 思 begin
        startPoints.add(new Point(72, 12)); // 1
        endPoints.add(new Point(72, 33));

        startPoints.add(new Point(81, 8)); // 2
        endPoints.add(new Point(112, 8));

        startPoints.add(new Point(113, 8)); //3
        endPoints.add(new Point(113, 33));

        startPoints.add(new Point(72, 20)); // 4
        endPoints.add(new Point(105, 20));

        startPoints.add(new Point(93, 9)); // 5
        endPoints.add(new Point(93, 32));

        startPoints.add(new Point(72, 32)); // 6
        endPoints.add(new Point(113, 33));

        startPoints.add(new Point(73, 42)); // 7
        endPoints.add(new Point(68, 49));

        startPoints.add(new Point(82, 41)); // 8
        endPoints.add(new Point(82, 55));

        startPoints.add(new Point(82, 54)); // 9
        endPoints.add(new Point(105, 54));

        startPoints.add(new Point(105, 54)); // 10
        endPoints.add(new Point(108, 47));

        startPoints.add(new Point(113, 41)); // 11
        endPoints.add(new Point(118, 47));

        startPoints.add(new Point(94, 39)); // 12
        endPoints.add(new Point(96, 43));
        // 思 end

        // 不 begin
        startPoints.add(new Point(132, 8)); // 1
        endPoints.add(new Point(179, 8));

        startPoints.add(new Point(160, 15)); // 2
        endPoints.add(new Point(130, 40));

        startPoints.add(new Point(162, 26)); // 3
        endPoints.add(new Point(179, 38));

        startPoints.add(new Point(155, 31)); // 4
        endPoints.add(new Point(155, 58));
        // 不 end

        // 得 begin
        startPoints.add(new Point(205, 6)); // 1
        endPoints.add(new Point(191, 18));

        startPoints.add(new Point(205, 23)); // 2
        endPoints.add(new Point(194, 35));

        startPoints.add(new Point(200, 38)); // 3
        endPoints.add(new Point(200, 54));

        startPoints.add(new Point(215, 3)); // 4
        endPoints.add(new Point(215, 26));

        startPoints.add(new Point(220, 8)); // 5
        endPoints.add(new Point(237, 8));

        startPoints.add(new Point(237, 8)); // 6
        endPoints.add(new Point(237, 25));

        startPoints.add(new Point(216, 15)); // 7
        endPoints.add(new Point(230, 15));

        startPoints.add(new Point(216, 25)); // 8
        endPoints.add(new Point(238, 25));

        startPoints.add(new Point(212, 33)); // 9
        endPoints.add(new Point(241, 33));

        startPoints.add(new Point(208, 43)); // 10
        endPoints.add(new Point(246, 43));

        startPoints.add(new Point(232, 39)); // 11
        endPoints.add(new Point(232, 57));

        startPoints.add(new Point(232, 57)); // 12
        endPoints.add(new Point(219, 56));

        startPoints.add(new Point(215, 49)); // 13
        endPoints.add(new Point(217, 52));
        // 得 end

        // 姐 begin
        startPoints.add(new Point(251, 22)); // 1
        endPoints.add(new Point(275, 22));

        startPoints.add(new Point(260, 9)); // 2
        endPoints.add(new Point(255, 37));

        startPoints.add(new Point(255, 37)); // 3
        endPoints.add(new Point(269, 49));

        startPoints.add(new Point(274, 25)); // 4
        endPoints.add(new Point(258, 50));

        startPoints.add(new Point(280, 15)); // 5
        endPoints.add(new Point(280, 52));

        startPoints.add(new Point(283, 9)); // 6
        endPoints.add(new Point(302, 9));

        startPoints.add(new Point(302, 9)); // 7
        endPoints.add(new Point(302, 52));

        startPoints.add(new Point(283, 24)); // 8
        endPoints.add(new Point(294, 24));

        startPoints.add(new Point(283, 38)); // 9
        endPoints.add(new Point(292, 38));

        startPoints.add(new Point(270, 53)); // 10
        endPoints.add(new Point(310, 53));

        // 姐 end

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
