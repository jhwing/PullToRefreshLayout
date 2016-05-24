package jhw.ptr.demo;

import android.support.v4.app.Fragment;

/**
 * Created by jihongwen on 16/5/24.
 */
public class DemoTabs {
    public Fragment fragment;
    public String title;

    public DemoTabs(String title, Fragment fragment) {
        this.title = title;
        this.fragment = fragment;
    }
}
