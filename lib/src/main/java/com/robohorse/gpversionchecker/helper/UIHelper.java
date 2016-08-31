package com.robohorse.gpversionchecker.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.robohorse.gpversionchecker.GPVersionChecker;
import com.robohorse.gpversionchecker.R;
import com.robohorse.gpversionchecker.domain.Version;

import static android.text.TextUtils.isEmpty;

/**
 * Created by robohorse on 06.03.16.
 */
public class UIHelper {

    public void showInfoView(final Activity activity, final Version version) {
        activity.runOnUiThread(() -> showDialogOnUIThread(activity, version));
    }

    private void showDialogOnUIThread(final Context context, Version version) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.gpvch_layout_dialog, null);
        bindVersionData(view, version, context);

        final AlertDialog dialog = new AlertDialog.Builder(context).setTitle(R.string.gpvch_header)
                .setCancelable(!GPVersionChecker.forceUpdate)
                .setView(view)
                .setPositiveButton(R.string.gpvch_button_positive, (dialogInterface, i) -> {
                })
                .setNegativeButton(R.string.gpvch_button_negative, (dialogInterface, i) -> {

                })
                .setOnKeyListener((dialog1, keyCode, event) -> GPVersionChecker.forceUpdate)
                .create();

        dialog.setOnShowListener(dialogInterface -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        if (!GPVersionChecker.forceUpdate)
                            dialog.dismiss();

                        openGooglePlay(context);
                    });

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(GPVersionChecker.forceUpdate ? View.GONE : View.VISIBLE);
        });

        dialog.show();
    }

    private void bindVersionData(View view, Version version, Context context) {
        TextView tvVersion = (TextView) view.findViewById(R.id.tvVersionCode);
        tvVersion.setText(context.getString(R.string.app_name) + ": " + version.getNewVersionCode());

        TextView tvNews = (TextView) view.findViewById(R.id.tvChanges);
        String lastChanges = version.getChanges();

        if (!isEmpty(lastChanges)) {
            tvNews.setText(lastChanges);
        } else {
            view.findViewById(R.id.lnChangesInfo).setVisibility(View.GONE);
        }
    }

    private void openGooglePlay(Context context) {
        final String packageName = isEmpty(GPVersionChecker.packageName) ? context.getApplicationContext().getPackageName() : GPVersionChecker.packageName;
        final String url = context.getString(R.string.gpvch_google_play_url) + packageName;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}
