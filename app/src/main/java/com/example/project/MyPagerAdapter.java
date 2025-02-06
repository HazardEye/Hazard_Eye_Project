package com.example.project;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class MyPagerAdapter extends FragmentPagerAdapter {
    private Context context;
    private int[] fragmentLayouts = {R.layout.fragment1_layout, R.layout.fragment2_layout, R.layout.fragment3_layout, R.layout.fragment4_layout};


    public MyPagerAdapter(FragmentManager fm, Context context, ViewPager viewPager) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return new CustomFragment(fragmentLayouts[position]);
    }

    @Override
    public int getCount() {
        return fragmentLayouts.length;
    }

}
