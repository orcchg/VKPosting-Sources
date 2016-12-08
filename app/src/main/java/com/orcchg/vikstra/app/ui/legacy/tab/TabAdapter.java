package com.orcchg.vikstra.app.ui.legacy.tab;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.orcchg.vikstra.domain.model.Genre;
import com.orcchg.vikstra.app.ui.legacy.list.ListFragment;

import java.util.ArrayList;
import java.util.List;

class TabAdapter extends FragmentStatePagerAdapter {

    private final List<Genre> tabs;

    TabAdapter(FragmentManager fm) {
        super(fm);
        tabs = new ArrayList<>();
    }

    void setTabs(List<Genre> genres) {
        tabs.clear();
        tabs.addAll(genres);
    }

    @Override
    public Fragment getItem(int position) {
        return ListFragment.newInstance((ArrayList<String>) tabs.get(position).getGenres());
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.isEmpty() ? "" : tabs.get(position).getName();
    }
}
