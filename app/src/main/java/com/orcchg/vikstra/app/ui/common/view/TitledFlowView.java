package com.orcchg.vikstra.app.ui.common.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.domain.model.Keyword;

import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TitledFlowView extends FrameLayout {

    @BindView(R.id.block_root_container) ViewGroup rootContainer;
    @BindView(R.id.block_icon) ImageView iconView;
    @BindView(R.id.block_title) TextView titleView;
    @BindView(R.id.block_label) TextView labelView;
    @BindView(R.id.block_label_prefix) TextView labelPrefixView;
    @BindView(R.id.block_edit_button) ImageButton editButton;
    @BindView(R.id.block_container) KeywordsFlowLayout keywordsFlowLayout;
    @BindView(R.id.line_selector) View lineSelectorView;
    @BindView(R.id.overlay_selector) View overlaySelectorView;

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
        lineSelectorView.setVisibility(isSelected ? VISIBLE : INVISIBLE);
        overlaySelectorView.setVisibility(isSelected ? VISIBLE : INVISIBLE);
    }

    public boolean isEditable() {
        return isEditable;
    }

    public boolean getSelection() {
        return isSelected;
    }

    public KeywordsFlowLayout getFlowLayout() {
        return keywordsFlowLayout;
    }

    /* Content */
    // --------------------------------------------------------------------------------------------
    public void setTitle(String text) {
        if (titleView != null) {
            if (TextUtils.isEmpty(text)) {
                titleView.setVisibility(View.GONE);
            } else {
                titleView.setVisibility(View.VISIBLE);
                titleView.setText(text);
            }
        }
    }
    public void setTitle(@StringRes int resId) {
        if (titleView != null) {
            if (resId <= 0) {
                titleView.setVisibility(View.GONE);
            } else {
                titleView.setVisibility(View.VISIBLE);
                titleView.setText(resId);
            }
        }
    }

    public void setLabel(String text) {
        if (labelView != null) {
            if (TextUtils.isEmpty(text)) {
                labelView.setVisibility(View.GONE);
            } else {
                labelView.setVisibility(View.VISIBLE);
                labelView.setText(text);
            }
        }
    }
    public void setLabel(@StringRes int resId) {
        if (labelView != null) {
            if (resId <= 0) {
                labelView.setVisibility(View.GONE);
            } else {
                labelView.setVisibility(View.VISIBLE);
                labelView.setText(resId);
            }
        }
    }

    public void setLabelPrefix(String text) {
        if (labelPrefixView != null) {
            if (TextUtils.isEmpty(text)) {
                labelPrefixView.setVisibility(View.GONE);
            } else {
                labelPrefixView.setVisibility(View.VISIBLE);
                labelPrefixView.setText(text);
            }
        }
    }
    public void setLabelPrefix(@StringRes int resId) {
        if (labelPrefixView != null) {
            if (resId <= 0) {
                labelPrefixView.setVisibility(View.GONE);
            } else {
                labelPrefixView.setVisibility(View.VISIBLE);
                labelPrefixView.setText(resId);
            }
        }
    }

    public void setKeywords(@NonNull Collection<Keyword> keywords) {
        if (keywordsFlowLayout != null) keywordsFlowLayout.setKeywords(keywords);
    }

    /* Listener */
    // --------------------------------------------------------------------------------------------
    public void setOnKeywordItemClickListener(KeywordsFlowLayout.OnKeywordItemClickListener listener) {
        if (keywordsFlowLayout != null) keywordsFlowLayout.setOnKeywordItemClickListener(listener);
    }

    public void setOnEditClickListener(View.OnClickListener listener) {
        if (editButton != null) editButton.setOnClickListener(listener);
    }
}
