
package com.example.androidtranscoder.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class PermissionUtils {

    private static final int PERMISSION_REQ_ID = 1001;
    private static final String REQUIRED_PERMS = Manifest.permission.READ_MEDIA_VIDEO;
    private static final String NOTICE_PERM = Manifest.permission.POST_NOTIFICATIONS;
    private static final String AUDIO_PERM = Manifest.permission.READ_MEDIA_AUDIO;
    private static final String VISUAL_USER_SELECT = Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED;

    public static void requestPermissions(AppCompatActivity activity) {

        final ArrayList<String> unsatisfiedPermissions = getUnsatisfiedPermissions(activity, new String[] {REQUIRED_PERMS});
        if (activity.checkSelfPermission(VISUAL_USER_SELECT) != PackageManager.PERMISSION_GRANTED) {
            unsatisfiedPermissions.add(VISUAL_USER_SELECT);
        }
        if (activity.checkSelfPermission(NOTICE_PERM) != PackageManager.PERMISSION_GRANTED) {
            unsatisfiedPermissions.add(NOTICE_PERM);
        }
        if (activity.checkSelfPermission(AUDIO_PERM) != PackageManager.PERMISSION_GRANTED) {
            unsatisfiedPermissions.add(AUDIO_PERM);
        }
        if (unsatisfiedPermissions.isEmpty()) {
            return;
        }
        for (String permission : unsatisfiedPermissions) {
            activity.shouldShowRequestPermissionRationale(permission);
        }
        activity.requestPermissions(unsatisfiedPermissions.toArray(new String[0]), PERMISSION_REQ_ID);
    }


    public static ArrayList<String> getUnsatisfiedPermissions(Context context, String[] requiredPermissions) {
        ArrayList<String> notAllowedPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                notAllowedPermissions.add(permission);
            }
        }
        return notAllowedPermissions;
    }
}

