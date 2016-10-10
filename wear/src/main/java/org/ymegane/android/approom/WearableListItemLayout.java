/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ymegane.android.approom;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.ymegane.android.approomcommns.AppInfo;

public class WearableListItemLayout extends LinearLayout implements WearableListView.OnCenterProximityListener  {

    private static final int ANIMATION_DURATION_MS = 150;

    private final float mFadedTextAlpha;
    private final int mFadedCircleColor;
    private ImageView mCircle;
    private TextView mName;

    public WearableListItemLayout(Context context) {
        this(context, null);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mFadedTextAlpha = getResources().getInteger(R.integer.action_text_faded_alpha) / 100f;
        mFadedCircleColor = ContextCompat.getColor(context, R.color.wl_gray);
    }

    public void bindView(AppInfo info) {
        mName.setText(info.appName);
        mCircle.setTag(info);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCircle = (ImageView) findViewById(R.id.circle);
        mName = (TextView) findViewById(R.id.name);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        mName.setAlpha(1f);
        AppInfo info = (AppInfo) mCircle.getTag();
        ((GradientDrawable) mCircle.getDrawable()).setColor(info.palette);

        if (animate) {
            mCircle.animate().scaleX(1.6f).scaleY(1.6f).setDuration(ANIMATION_DURATION_MS);
        } else {
            mCircle.setScaleX(1.6f);
            mCircle.setScaleY(1.6f);
        }
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        ((GradientDrawable) mCircle.getDrawable()).setColor(mFadedCircleColor);
        mName.setAlpha(mFadedTextAlpha);

        if (animate) {
            mCircle.animate().scaleY(1f).scaleX(1f).setDuration(ANIMATION_DURATION_MS);
        } else {
            mCircle.setScaleX(1f);
            mCircle.setScaleY(1f);
        }
    }
}
