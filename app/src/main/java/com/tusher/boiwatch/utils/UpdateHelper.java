package com.tusher.boiwatch.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.tusher.boiwatch.BuildConfig;
import com.tusher.boiwatch.R;

import java.util.HashMap;
import java.util.Map;

public class UpdateHelper {
    private static final String TAG = "UpdateHelper";
    
    // Remote Config Keys
    private static final String KEY_LATEST_VERSION = "latest_version_code";
    private static final String KEY_UPDATE_URL = "update_url";
    private static final String KEY_FORCE_UPDATE = "is_force_update";

    public static void checkForUpdates(Context context) {
        checkForUpdates(context, false);
    }

    public static void checkForUpdates(Context context, boolean isManual) {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        
        // Settings for rapid testing (reduce minimum fetch interval)
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(isManual ? 0 : 3600) // 1 hour for auto, 0 for manual
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        // Set default values
        Map<String, Object> defaultValues = new HashMap<>();
        defaultValues.put(KEY_LATEST_VERSION, (long) BuildConfig.VERSION_CODE);
        defaultValues.put(KEY_UPDATE_URL, "");
        defaultValues.put(KEY_FORCE_UPDATE, false);
        mFirebaseRemoteConfig.setDefaultsAsync(defaultValues);

        if (isManual) {
            Toast.makeText(context, "Checking for updates...", Toast.LENGTH_SHORT).show();
        }

        mFirebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    long latestVersionCode = mFirebaseRemoteConfig.getLong(KEY_LATEST_VERSION);
                    String updateUrl = mFirebaseRemoteConfig.getString(KEY_UPDATE_URL);
                    boolean isForceUpdate = mFirebaseRemoteConfig.getBoolean(KEY_FORCE_UPDATE);

                    Log.d(TAG, "Latest Version Code: " + latestVersionCode);
                    Log.d(TAG, "Current Version Code: " + BuildConfig.VERSION_CODE);

                    if (latestVersionCode > BuildConfig.VERSION_CODE) {
                        showUpdateDialog(context, updateUrl, isForceUpdate);
                    } else if (isManual) {
                        Toast.makeText(context, "BoiWatch is up to date!", Toast.LENGTH_SHORT).show();
                    }
                } else if (isManual) {
                    Toast.makeText(context, "Update check failed. Please check your internet.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private static void showUpdateDialog(Context context, String url, boolean isForceUpdate) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.GlassmorphicDialog)
                .setTitle("Update Available")
                .setMessage("A new version of BoiWatch is available. Update now to enjoy latest features and bug fixes.")
                .setPositiveButton("Update Now", (dialog, which) -> {
                    if (url != null && !url.isEmpty()) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        context.startActivity(browserIntent);
                    }
                });

        if (!isForceUpdate) {
            builder.setNegativeButton("Later", null);
        } else {
            builder.setCancelable(false);
        }

        builder.show();
    }
}
