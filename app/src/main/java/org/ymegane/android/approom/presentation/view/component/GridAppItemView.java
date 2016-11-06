package org.ymegane.android.approom.presentation.view.component;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import org.ymegane.android.approom.R;

/**
 * GridItem
 */
public class GridAppItemView extends RelativeLayout implements Checkable {

    public GridAppItemView(Context context) {
        super(context);
    }

    public GridAppItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridAppItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean isChecked;

    @Override
    public void setChecked(boolean checked) {
        isChecked = checked;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(checked ? ContextCompat.getDrawable(getContext(),
                    R.drawable.grid_item_select) : null);
        } else {
            setBackground(checked ? ContextCompat.getDrawable(getContext(),
                    R.drawable.grid_item_select) : null);
        }
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked);
    }
}
