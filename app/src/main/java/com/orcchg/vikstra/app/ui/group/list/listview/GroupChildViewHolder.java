package com.orcchg.vikstra.app.ui.group.list.listview;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.group.list.OnGroupClickListener;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.data;

public class GroupChildViewHolder extends ChildViewHolder<GroupChildItem> {

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

    public void bind(GroupChildItem model) {
        titleTextView.setText(model.getName());
        countTextView.setText(String.format(Locale.ENGLISH, "%s", model.getCount()));

        SupplyData data = new SupplyData.Builder()
                .setModel(getChild())
                .setParentAdapterPosition(getParentAdapterPosition())
                .setChildAdapterPosition(getChildAdapterPosition())
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

        SupplyData(Builder builder) {
            this.model = builder.model;
            this.parentAdapterPosition = builder.parentAdapterPosition;
            this.childAdapterPosition = builder.childAdapterPosition;
        }

        static class Builder {
            private GroupChildItem model;
            private int parentAdapterPosition;
            private int childAdapterPosition;

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
    }

    /* Internal */
    // ------------------------------------------
    private void setCheckedSafe(boolean isSelected) {
        switcher.setOnCheckedChangeListener(null);  // remove listener to select safely
        switcher.setChecked(isSelected);
    }
}
