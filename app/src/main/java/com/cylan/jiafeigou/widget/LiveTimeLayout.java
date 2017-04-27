package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan-hunt on 16-12-23.
 */

public class LiveTimeLayout extends FrameLayout implements LiveTimeSetter {

    private TextView textView;

    public LiveTimeLayout(Context context) {
        this(context, null);
    }

    public LiveTimeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveTimeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View v = LayoutInflater.from(context).inflate(R.layout.layout_live_time, this);
        textView = (TextView) v.findViewById(R.id.tv_live_time);
    }

    @Override
    public void setContent(String content) {
        if (!isShown()) setVisibility(VISIBLE);
        if (!textView.isShown()) textView.setVisibility(View.VISIBLE);
        textView.setText(content);
    }
}

