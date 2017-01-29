package com.orcchg.vikstra.app.ui.group.list.listview.child;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.adapter.expandable.base.BaseChildViewHolder;
import com.orcchg.vikstra.app.ui.group.list.OnGroupClickListener;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupChildViewHolder extends BaseChildViewHolder<GroupChildItem> {

    @BindView(R.id.tv_title) TextView titleTextView;
    @BindView(R.id.tv_count) TextView countTextView;
    @BindView(R.id.switcher) Switch switcher;

    private OnGroupClickListener onGroupClickListener;
    private CompoundButton.OnCheckedChangeListener switcherListener;

    public GroupChildViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setOnGroupClickListener(OnGroupClickListener listener) {
        onGroupClickListener = listener;
    }

    public void setSwitcherListener(CompoundButton.OnCheckedChangeListener switcherListener) {
        this.switcherListener = switcherListener;
        if (switcher != null) switcher.setOnCheckedChangeListener(switcherListener);
    }

    @Override
    public void bind(GroupChildItem model) {
        titleTextView.setText(model.getName());
        countTextView.setText(String.format(Locale.ENGLISH, "%s", model.getCount()));

        SupplyData data = new SupplyData.Builder()
                .setModel(getChild())
                .setParentAdapterPosition(getParentAdapterPosition())
                .setChildAdapterPosition(getChildAdapterPosition())
                .setSwitcherListener(switcherListener)
                .build();
        switcher.setTag(data);
        setCheckedSafe(model.isSelected());
        setSwitcherListener(switcherListener);

        itemView.setOnClickListener((view) -> {
            if (onGroupClickListener != null) onGroupClickListener.onGroupClick(model.getId());
        });
    }

    /* Support */
    // ------------------------------------------
    public static class SupplyData {
        private GroupChildItem model;
        private int parentAdapterPosition;
        private int childAdapterPosition;
        private CompoundButton.OnCheckedChangeListener switcherListener;

        SupplyData(Builder builder) {
            this.model = builder.model;
            this.parentAdapterPosition = builder.parentAdapterPosition;
            this.childAdapterPosition = builder.childAdapterPosition;
            this.switcherListener = builder.switcherListener;
        }

        static class Builder {
            private GroupChildItem model;
            private int parentAdapterPosition;
            private int childAdapterPosition;
            private CompoundButton.OnCheckedChangeListener switcherListener;

            Builder setModel(GroupChildItem model) {
                this.model = model;
                return this;
            }

            Builder setParentAdapterPosition(int parentAdapterPosition) {
                this.parentAdapterPosition = parentAdapterPosition;
                return this;
            }

            Builder setChildAdapterPosition(int childAdapterPosition) {
                this.childAdapterPosition = childAdapterPosition;
                return this;
            }

            Builder setSwitcherListener(CompoundButton.OnCheckedChangeListener switcherListener) {
                this.switcherListener = switcherListener;
                return this;
            }

            SupplyData build() {
                return new SupplyData(this);
            }
        }

        public GroupChildItem getModel() {
            return model;
        }

        public int getParentAdapterPosition() {
            return parentAdapterPosition;
        }

        public int getChildAdapterPosition() {
            return childAdapterPosition;
        }

        public CompoundButton.OnCheckedChangeListener getSwitcherListener() {
            return switcherListener;
        }
    }

    /* Internal */
    // ------------------------------------------
    private void setCheckedSafe(boolean isSelected) {
        switcher.setOnCheckedChangeListener(null);  // remove listener to select safely
        switcher.setChecked(isSelected);
    }
}
