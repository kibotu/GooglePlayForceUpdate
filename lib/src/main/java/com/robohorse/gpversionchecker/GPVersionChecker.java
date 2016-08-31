package com.robohorse.gpversionchecker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;

import com.robohorse.gpversionchecker.base.CheckingStrategy;
import com.robohorse.gpversionchecker.base.VersionInfoListener;
import com.robohorse.gpversionchecker.debug.ALog;
import com.robohorse.gpversionchecker.domain.Version;
import com.robohorse.gpversionchecker.helper.SharedPrefsHelper;
import com.robohorse.gpversionchecker.helper.UIHelper;

import java.lang.ref.WeakReference;

/**
 * Created by robohorse on 06.03.16.
 */
public class GPVersionChecker {

    private static WeakReference<Activity> activityWeakReference;
    private static VersionInfoListener versionInfoListener;
    private static CheckingStrategy strategy;
    public static boolean useLog = false;
    public static String packageName;
    private static UIHelper uiHelper;
    private static SharedPrefsHelper sharedPrefsHelper;
    private static boolean showDialog = true;
    public static boolean forceUpdate = false;

    private static void proceed() {
        Activity activity = activityWeakReference.get();
        if (null == activity) {
            throw new RuntimeException("Activity cannot be null for GPVersionChecker context");
        }

        if (strategy == CheckingStrategy.ALWAYS ||
                (strategy == CheckingStrategy.ONE_PER_DAY && sharedPrefsHelper.needToCheckVersion(activity))) {
            startService(activity);
        } else {
            ALog.d("Skipped");
        }
    }

    private static void startService(Activity activity) {
        Intent service = new Intent(activity, VersionCheckerService.class);
        service.putExtra(VersionCheckerService.CUSTOM_PACKAGE, packageName);
        activity.startService(service);
    }

    protected static void onResponseReceived(final Version version) {
        if (null == activityWeakReference) {
            return;
        }
        Activity activity = activityWeakReference.get();

        if (null == activity || activity.isFinishing()) {
            return;
        }
        sharedPrefsHelper.saveCurrentDate(activity);

        if (null != versionInfoListener) {
            activity.runOnUiThread(() -> versionInfoListener.onResulted(version));
        }

        if (showDialog && version.isNeedToUpdate()) {
            uiHelper.showInfoView(activity, version);
        }
    }

    private static void resetState(Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
        versionInfoListener = null;
        strategy = CheckingStrategy.ALWAYS;
    }

    public static class Builder {

        public Builder(Activity activity) {
            resetState(activity);
            uiHelper = new UIHelper();
            sharedPrefsHelper = new SharedPrefsHelper();
        }

        protected Builder(Activity activity, UIHelper uiHelper, SharedPrefsHelper sharedPrefsHelper) {
            resetState(activity);
            GPVersionChecker.uiHelper = uiHelper;
            GPVersionChecker.sharedPrefsHelper = sharedPrefsHelper;
        }

        /**
         * Set custom version-response subscriber. This subscriber will disable default dialog window.
         *
         * @return Builder
         */
        public Builder setVersionInfoListener(VersionInfoListener versionInfoListener) {
            GPVersionChecker.versionInfoListener = versionInfoListener;
            return this;
        }

        /**
         * Set logging. Default <code>{@value GPVersionChecker#useLog}</code>
         *
         * @return Builder
         */
        public Builder setLoggingEnable(boolean useLog) {
            GPVersionChecker.useLog = useLog;
            return this;
        }

        /**
         * Show Dialog.
         *
         * @return Builder
         */
        public Builder showDialog(boolean showDialog) {
            GPVersionChecker.showDialog = showDialog;
            return this;
        }

        /**
         * Set strategy of version-checking: ALWAYS, ONE_PER_DAY
         *
         * @return Builder
         */
        public Builder setCheckingStrategy(CheckingStrategy strategy) {
            GPVersionChecker.strategy = strategy;
            return this;
        }

        /**
         * Create checking request
         *
         * @return Builder
         */
        public Builder create() {
            proceed();
            return this;
        }

        /**
         * Sets custom package name for testing purposes.
         *
         * Default is {@link android.content.pm.PackageInfo#versionName}
         *
         * @param packageName Custom package name.
         * @return Builder
         */
        public Builder setCustomPackageName(String packageName) {
            GPVersionChecker.packageName = packageName;
            return this;
        }

        /**
         * Removes possibility to skip the dialog. Default <code>{@value GPVersionChecker#forceUpdate}</code>
         *
         * @param forceUpdate true for forcing
         * @return Builder
         */
        public Builder forceUpdate(boolean forceUpdate) {
            GPVersionChecker.forceUpdate = forceUpdate;
            return this;
        }
    }
}
