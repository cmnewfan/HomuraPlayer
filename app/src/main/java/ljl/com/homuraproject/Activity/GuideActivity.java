package ljl.com.homuraproject.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ljl.com.homuraproject.PlayService;
import ljl.com.homuraproject.R;

public class GuideActivity extends Activity {

    private ViewPager viewPager;
    private PagerTabStrip pagerTabStrip;
    private Button guide_button;
    private int image_view_source[] = {R.drawable.guide_1_1, R.drawable.guide_2_2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        guide_button = (Button) this.findViewById(R.id.guide_button);
        LinearLayout.LayoutParams imgvwDimens =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        InitViewAdapter();
        //InitPagerTabStrip();
        InitClickListener();
    }

    private Bitmap getLoacalBitmap(String url) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        InputStream stream = null;
        try {
            stream = new FileInputStream(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(stream);
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
            PlayService.release();
        }
        return false;
    }
}
