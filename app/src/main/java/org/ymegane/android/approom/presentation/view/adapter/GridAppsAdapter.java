package org.ymegane.android.approom.presentation.view.adapter;

import java.util.List;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;

import com.squareup.picasso.Picasso;

import org.ymegane.android.approom.presentation.view.component.GridAppItemView;
import org.ymegane.android.approom.R;
import org.ymegane.android.approomcommns.domain.model.AppInfo;

/**
 * グリッド表示時のアダプター
 * @author y
 *
 */
public class GridAppsAdapter extends ArrayAdapter<AppInfo> implements Filterable {
    private static final String TAG = "GridAppsAdapter";

    private LayoutInflater inflater;

    private SparseBooleanArray checkedArray = new SparseBooleanArray();

    public GridAppsAdapter(Context context, List<AppInfo> objects) {
        super(context, -1, objects);
        inflater = LayoutInflater.from(context);
    }

    public void resetCheckedState() {
        checkedArray.clear();
    }

    public void setCheckedState(int position, boolean checked) {
        if (checked) {
            checkedArray.put(position, true);
        } else {
            checkedArray.delete(position);
        }
    }

    public void toggleCheckState(int position) {
        boolean current = checkedArray.get(position);
        setCheckedState(position, !current);
    }

    public SparseBooleanArray getCheckedStates() {
        return checkedArray;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GridAppItemView itemView = (GridAppItemView) convertView;
        if(convertView == null) {
            itemView = (GridAppItemView) inflater.inflate(R.layout.adapter_grid_app_item, null);
        }

        AppInfo appData = getItem(position);

        itemView.textAppName.setText(appData.appName);

        if(appData.iconUrl != null) {
            Picasso.with(getContext())
                    .load(appData.iconUrl)
                    .placeholder(R.drawable.ic_launcher_failed)
                    .error(R.drawable.ic_launcher_failed)
                    .stableKey(appData.iconUrl.toString()+String.valueOf(appData.lastModify))
                    .into(itemView.imageIcon);
        } else {
            itemView.imageIcon.setImageResource(R.drawable.ic_launcher_failed);
        }

        if (checkedArray.get(position)) {
            itemView.setBackgroundResource(R.drawable.grid_item_select);
        } else {
            itemView.setBackgroundResource(0);
        }

        return itemView;
    }
}
