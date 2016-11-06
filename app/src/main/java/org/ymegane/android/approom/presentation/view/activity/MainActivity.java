package org.ymegane.android.approom.presentation.view.activity;

import org.ymegane.android.approom.presentation.view.fragment.AppDisplayFragment;
import org.ymegane.android.approom.presentation.view.fragment.AppDisplayFragment.OnAppInfoClickListener;
import org.ymegane.android.approom.presentation.view.fragment.DetailPreferenceFragment;
import org.ymegane.android.approom.R;
import org.ymegane.android.approomcommns.domain.model.AppInfo;
import org.ymegane.android.approomcommns.AppLinkBase;

import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements OnAppInfoClickListener {
    private static final String TAG = "MainActivity";

    private static final String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Needs to be called before setting the content view
        setContentView(R.layout.main);

        WallpaperManager wallpaper = WallpaperManager.getInstance(this);
        Drawable d = wallpaper.getFastDrawable().mutate();
        ImageView imageView = (ImageView) findViewById(R.id.imageWallpaper);
        imageView.setImageDrawable(d);
        if(savedInstanceState == null) {
            AppDisplayFragment fragment = new AppDisplayFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.container, fragment, AppDisplayFragment.TAG);
            ft.commit();
        }
    }

    @Override
    public void onItemClick(View view, AppInfo info) {
        String action = getIntent().getAction();

        if(action != null && action.contains(ACTION_INTERCEPT)){
            Bundle args = new Bundle();
            args.putString("appName", info.appName);
            args.putString("packageName", info.appInfo.packageName);
            LinkSelectDialog dialog = new LinkSelectDialog();
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "LinkSelectDialog");
        }else {
            // 詳細画面を表示する
            Pair<View, String> sharedIcon = new Pair<>(view.findViewById(R.id.imageAppIcon), DetailActivity.TRANSITION_ICON);
            Pair<View, String> sharedLabel = new Pair<>(view.findViewById(R.id.textAppName), DetailActivity.TRANSITION_LABEL);
            DetailActivity.launch(this, info, sharedIcon, sharedLabel);
        }
    }

    @Override
    public boolean isMashroom() {
        String action = getIntent().getAction();
        return action != null && action.contains(ACTION_INTERCEPT);
    }

    @Override
    public void onOpenSetting() {
        DetailPreferenceFragment fragment = new DetailPreferenceFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //ft.replace(android.R.id.content, fragment, "DetailPreferenceFragment");
        ft.addToBackStack("DetailPreferenceFragment");
        ft.commit();
    }

    public static class LinkSelectDialog extends DialogFragment implements OnClickListener {
        private String[] linkItems;
        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String appName = getArguments().getString("appName");
            String packageName = getArguments().getString("packageName");
            linkItems = new String[]{
                    appName + ":" + AppLinkBase.LINK_HTTP_DETAIL + packageName,
                    appName + ":" + AppLinkBase.LINK_MARKET_DETAIL + packageName};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.select_type);
            builder.setItems(linkItems, this);
            return builder.create();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent data = new Intent();
            data.putExtra("replace_key", linkItems[which]);
            getActivity().setResult(RESULT_OK, data);
            getActivity().finish();
        }
    }
}