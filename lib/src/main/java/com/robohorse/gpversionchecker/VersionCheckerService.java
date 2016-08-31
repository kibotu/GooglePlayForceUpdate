package com.robohorse.gpversionchecker;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.robohorse.gpversionchecker.debug.ALog;
import com.robohorse.gpversionchecker.domain.Version;
import com.robohorse.gpversionchecker.helper.DataParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;

/**
 * Created by robohorse on 06.03.16.
 */
public class VersionCheckerService extends IntentService {
    private static final String REFERRER = "http://www.google.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0";
    public static final String CUSTOM_PACKAGE = "CUSTOM_PACKAGE";
    private static final int CONNECTION_TIMEOUT = 30000;
    private String customPackageName;

    public VersionCheckerService() {
        super("GPVersionChecker");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle bundle = intent.getExtras();
        if (bundle != null)
            customPackageName = bundle.getString(CUSTOM_PACKAGE);

        Version version = obtainDataFromGooglePlay();
        if (null != version) {
            ALog.d("Response received: " + version.toString());
            GPVersionChecker.onResponseReceived(version);
        }
        stopSelf();
    }

    private Version obtainDataFromGooglePlay() {
        try {
            return obtainDataFromGooglePlayWithException();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Version obtainDataFromGooglePlayWithException()
            throws IOException, NumberFormatException, PackageManager.NameNotFoundException {


        Context context = getApplicationContext();
        final String packageName = isEmpty(customPackageName) ? context.getPackageName() : customPackageName;
        final String currentVersion = context.getPackageManager().getPackageInfo(packageName, 0).versionName;
        final String language = Locale.getDefault().getLanguage();

        final String url = context.getString(R.string.gpvch_google_play_url) + packageName + "&hl=" + language;
        ALog.d("request params: package - " + packageName + ", current app version: " + currentVersion);

        final Document document = Jsoup.connect(url)
                .timeout(CONNECTION_TIMEOUT)
                .userAgent(USER_AGENT)
                .referrer(REFERRER)
                .get();

        return new DataParser().parse(document, currentVersion, url);
    }
}