package org.ymegane.android.approom.presentation.view.fragment;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import org.ymegane.android.approom.R;

/**
 * 設定画面
 */
public class DetailPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference);

        initActionBar();
    }

    private void initActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
