package org.ymegane.android.approom.presentation.view.component;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.ymegane.android.approom.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * GridItem
 */
public class GridAppItemView extends RelativeLayout implements Checkable {

    @BindView(R.id.textAppName)
    public TextView textAppName;
    @BindView(R.id.imageAppIcon)
    public ImageView imageIcon;

    public GridAppItemView(Context context) {
        super(context);
    }

    public GridAppItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridAppItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
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
