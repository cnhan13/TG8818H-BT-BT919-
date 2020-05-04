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
        Log.e(TAG, "说明这个权限来干嘛的: ");
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    void onDenied() {
        Log.e(TAG, "用户拒绝: ");
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    void onNeverAskAgain() {
        Log.e(TAG, "用户选择不再提示: ");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 代理权限处理到自动生成的方法
        BaseActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
