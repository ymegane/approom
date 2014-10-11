package org.ymegane.android.approom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by y on 2014/10/05.
 */
public class GridAppItemView extends RelativeLayout implements Checkable {

    TextView textAppName;
    ImageView imageIcon;

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

        textAppName = (TextView) findViewById(R.id.textAppName);
        imageIcon = (ImageView) findViewById(R.id.imageAppIcon);
    }

    private boolean isChecked;

    @Override
    public void setChecked(boolean checked) {
        isChecked = checked;
        setBackgroundDrawable(checked ? getResources().getDrawable(
                R.drawable.grid_item_select) : null);
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
