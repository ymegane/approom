package org.ymegane.android.approom.presentation.view.adapter;

import java.util.List;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;

import org.ymegane.android.approom.databinding.AdapterGridAppItemBinding;
import org.ymegane.android.approom.R;
import org.ymegane.android.approomcommns.domain.model.AppModel;

/**
 * グリッド表示時のアダプター
 * @author y
 *
 */
public class GridAppsAdapter extends ArrayAdapter<AppModel> implements Filterable {
    private static final String TAG = "GridAppsAdapter";

    private LayoutInflater inflater;

    private SparseBooleanArray checkedArray = new SparseBooleanArray();

    public GridAppsAdapter(Context context, List<AppModel> objects) {
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
        AdapterGridAppItemBinding binding;
        if(convertView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.adapter_grid_app_item, parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (AdapterGridAppItemBinding) convertView.getTag();
        }

        binding.setAppModel(getItem(position));

        if (checkedArray.get(position)) {
            convertView.setBackgroundResource(R.drawable.grid_item_select);
        } else {
            convertView.setBackgroundResource(0);
        }

        return convertView;
    }
}
