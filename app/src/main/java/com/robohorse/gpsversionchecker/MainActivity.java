package com.robohorse.gpsversionchecker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.robohorse.gpversionchecker.GPVersionChecker;
import com.robohorse.gpversionchecker.base.CheckingStrategy;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new GPVersionChecker.Builder(this)
                .setCheckingStrategy(CheckingStrategy.ALWAYS)
                .showDialog(true)
                .setCustomPackageName("net.kibotu.android.deviceinfo")
                .setVersionInfoListener(version -> Log.v(TAG, "version=" + version))
                .create();
    }
}
