package com.weiou.temperaturedemo;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class BaseActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BaseActivityPermissionsDispatcher.initPermissionWithPermissionCheck(this);

    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void initPermission() {

    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    void showRationale(PermissionRequest request) {
        request.proceed();
        Log.e(TAG, "Explain what this authority does: ");
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    void onDenied() {
        Log.e(TAG, "User rejected: ");
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    void onNeverAskAgain() {
        Log.e(TAG, "User chooses not to prompt again: ");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Agent permission processing to automatic generation method
        BaseActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
