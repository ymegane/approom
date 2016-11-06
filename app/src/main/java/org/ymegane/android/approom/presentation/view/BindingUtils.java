package org.ymegane.android.approom.presentation.view;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.ymegane.android.approom.R;
import org.ymegane.android.approomcommns.domain.model.AppModel;

public class BindingUtils {
    private BindingUtils(){}

    @BindingAdapter("loadAppIcon")
    public static void loadAppIcon(ImageView imageView, AppModel appModel) {
        Context context = imageView.getContext();

        Picasso.with(context)
                .load(appModel.getIconUrl())
                .placeholder(R.drawable.ic_launcher_failed)
                .error(R.drawable.ic_launcher_failed)
                .stableKey(appModel.getIconUrl().toString()+String.valueOf(appModel.getLastModify()))
                .into(imageView);
    }
}
