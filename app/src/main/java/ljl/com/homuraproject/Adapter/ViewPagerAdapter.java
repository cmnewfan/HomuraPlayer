package ljl.com.homuraproject.Adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by hzfd on 2016/7/5.
 */
public class ViewPagerAdapter extends PagerAdapter {
    private ArrayList<View> views;
    private ArrayList<String> titles;

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position,
                            Object object) {
        container.removeView(views.get(position));
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (titles != null) {
            return titles.get(position);
        } else {
            return "";
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position));
        return views.get(position);
    }

    public ViewPagerAdapter(ArrayList<View> view, ArrayList<String> title) {
        views = null;
        titles = null;
        views = view;
        titles = title;
    }

    public ViewPagerAdapter(ArrayList<View> view) {
        views = null;
        titles = null;
        views = view;
    }
}
