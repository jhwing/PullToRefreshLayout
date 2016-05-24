package jhw.ptr.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.astuetz.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<DemoTabs> tabses = new ArrayList<>();

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private MyPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabses.add(new DemoTabs("Classic", new ClassicRefreshFragment()));
        tabses.add(new DemoTabs("StoreHouse", new StoreHouseFragment()));
        tabses.add(new DemoTabs("DropView", new DropViewFragment()));

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        tabs.setViewPager(pager);

    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabses.get(position).title;
        }

        @Override
        public int getCount() {
            return tabses.size();
        }

        @Override
        public Fragment getItem(int position) {
            return tabses.get(position).fragment;
        }

    }

}
