package ljl.com.homuraproject.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import ljl.com.homuraproject.R;

public class GuideActivity extends Activity {

    private SharedPreferences sharedPreferences;
    private ViewPager viewPager;
    private PagerTabStrip pagerTabStrip;
    private Button guide_button;
    private int image_view_source[] = {R.drawable.guide_1_source, R.drawable.guide_3_source,R.drawable.guide_2_source};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        guide_button = (Button) this.findViewById(R.id.guide_button);
        InitViewAdapter();
        InitClickListener();
    }

    private void InitClickListener() {
        guide_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent();
                intent.setClass(GuideActivity.this, FileActivity.class);
                startActivity(intent);
            }
        });
    }

    /*private void InitPagerTabStrip() {
        pagerTabStrip = (PagerTabStrip) findViewById(R.id.guide_pagertab);
        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.material_blue_grey_800));
        pagerTabStrip.setDrawFullUnderline(false);
        pagerTabStrip.setTextSpacing(50);
    }*/

    private void InitViewAdapter() {
        final ArrayList<String> titleList = new ArrayList<String>();
        titleList.add("");
        titleList.add("");
        viewPager = (ViewPager) this.findViewById(R.id.guide_viewpager);
        PagerAdapter guide_pager_adapter = new PagerAdapter() {
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == ((LinearLayout) arg1);
            }

            @Override
            public int getCount() {
                return image_view_source.length;
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                container.removeView((LinearLayout) object);
            }

            @Override
            public int getItemPosition(Object object) {
                return super.getItemPosition(object);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View itemView = getLayoutInflater().inflate(R.layout.guide_image_1, container, false);
                ImageView imageView = (ImageView) itemView.findViewById(R.id.guide_image_1);
                imageView.setImageResource(image_view_source[position]);
                container.addView(itemView);
                return itemView;
            }
        };
        viewPager.setAdapter(guide_pager_adapter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        this.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        return false;
    }
}
