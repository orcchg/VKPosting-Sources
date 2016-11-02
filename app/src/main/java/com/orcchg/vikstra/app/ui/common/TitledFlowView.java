package com.orcchg.vikstra.app.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.orcchg.vikstra.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TitledFlowView extends FrameLayout {

    @BindView(R.id.block_root_container) ViewGroup rootContainer;
    @BindView(R.id.block_icon) ImageView iconView;
    @BindView(R.id.block_title) TextView titleView;
    @BindView(R.id.block_edit_button) ImageButton editButton;
    @BindView(R.id.block_container) KeywordsFlowLayout keywordsFlowLayout;

    private boolean isEditable;
    private boolean isSelected;

    public TitledFlowView(Context context) {
        this(context, null, 0);
    }

    public TitledFlowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitledFlowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.titled_flow_view, this, true);
        ButterKnife.bind(this, rootView);

        setEditable(isEditable);
        setSelection(isSelected);
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
        if (editButton != null) editButton.setVisibility(isEditable ? VISIBLE : GONE);
    }

    public void setSelection(boolean isSelected) {
        this.isSelected = isSelected;
        // TODO: selection overlay
    }
}
