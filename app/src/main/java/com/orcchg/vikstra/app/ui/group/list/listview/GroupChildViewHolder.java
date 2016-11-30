package com.orcchg.vikstra.app.ui.group.list.listview;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.orcchg.vikstra.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupChildViewHolder extends ChildViewHolder<GroupChildItem> {

    @BindView(R.id.tv_title) TextView titleTextView;
    @BindView(R.id.tv_count) TextView countTextView;
    @BindView(R.id.switcher) Switch switcher;

    private CompoundButton.OnCheckedChangeListener switcherListener;

    public GroupChildViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setSwitcherListener(CompoundButton.OnCheckedChangeListener switcherListener) {
        this.switcherListener = switcherListener;
        if (switcher != null) switcher.setOnCheckedChangeListener(switcherListener);
    }

    public void bind(GroupChildItem model) {
        titleTextView.setText(model.getName());
        countTextView.setText(Integer.toString(model.getCount()));
        setSwitcherListener(switcherListener);
    }
}
