package microsoft.prototype.cvprototype.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public enum PermissionsManager {
    INSTANCE;

    public enum Permission {
        CAMERA(0x01, Manifest.permission.CAMERA),
        WRITE_EXTERNAL_STORAGE(0x05, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        int requestCode;
        String systemTag;

        Permission(int requestCode, String systemTag) {
            this.requestCode = requestCode;
            this.systemTag = systemTag;
        }
    }

    /**
     * check if the required permissions are granted or not. If not, request the missing permissions
     *
     * @param context
     * @param permissions
     * @return true if all permissions have been granted, false otherwise
     */
    public boolean requestPermissionsIfNotGranted(Activity context, Permission... permissions) {
        int requestCode = 0;
        List<String> requestedPermissions = new ArrayList<>();

        boolean granted = true;

        for (Permission permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission.systemTag)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCode |= permission.requestCode;
                requestedPermissions.add(permission.systemTag);
                granted = false;
            }
        }

        if (!granted) {
            ActivityCompat.requestPermissions(context,
                    requestedPermissions.toArray(new String[requestedPermissions.size()]),
                    requestCode);
        }

        return granted;
    }
}
