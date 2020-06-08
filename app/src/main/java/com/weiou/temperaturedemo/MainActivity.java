package com.weiou.temperaturedemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.weiou.lib_temp.ReportBean;
import com.weiou.lib_temp.TempBle;
import com.weiou.lib_temp.TempBleCallback;
import com.weiou.lib_temp.utils.DataUtils;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity
        implements DeviceAdapter.DeviceCallback, TempBleCallback {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.tvDeviceMessage)
    AppCompatTextView tvDeviceMessage;
    @BindView(R.id.tvTemp)
    AppCompatTextView tvTemp;
    @BindView(R.id.tvSetting)
    AppCompatTextView tvSetting;
    @BindView(R.id.tvHistory)
    TextView tvHistory;

    private TempBle tempBle;
    private String TAG = "MainActivity";
    private DeviceAdapter adapter;
    private MainViewModel model;
    private String unit = "";

    public static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1002;
    public static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        tempBle = new TempBle(BluetoothAdapter.getDefaultAdapter(), this);
        model = new ViewModelProvider(this).get(MainViewModel.class);
        adapter = new DeviceAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_ACCESS_COARSE_LOCATION);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_FINE_LOCATION);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
            }
            case REQUEST_CODE_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onItemClick(String mac) {
        tempBle.disconnect();
        tempBle.stopScan(scanCallback);
        tempBle.connectBle(mac);
    }

    @Override
    public void onConnectSuccess(String mac) {
        adapter.updateStatus(mac);
    }

    @Override
    public void onDisconnect() {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "BLE Disconnected", Toast.LENGTH_SHORT).show());
    }

    /**
     * @param model Current forehead gun mode
     *              DataUtils.MODE_SURFACE
     *              DataUtils.MODE_ADULT
     *              DataUtils.MODE_CHILD
     * @param temp  Forehead gun return temperature
     */
    @Override
    public void onTempGet(int model, double temp) {
        this.model.setTemp(model, temp, tvTemp);
    }

    /**
     * Modes included in current forehead gun equipment
     *
     * @param bit0 Surface mode    0 is not included 1 is included
     * @param bit1 Adult mode    0 is not included 1 is included
     * @param bit2 Child mode    0 is not included 1 is included
     */
    @Override
    public void onDeviceMessageGet(Integer bit0, Integer bit1, Integer bit2) {
        model.setDeviceMessage(bit0, bit1, bit2, tvDeviceMessage);

    }

    /**
     * Device sleep
     */
    @Override
    public void onOffline() {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Device is offline", Toast.LENGTH_SHORT).show());
    }

    /**
     * The device is woken up
     */
    @Override
    public void onWakeUp() {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "The device has woken up", Toast.LENGTH_SHORT).show());
    }

    /**
     * Bluetooth exception code return
     *
     * @param i Details https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/android-cts-5.1_r17/stack/include/gatt_api.h
     */
    @Override
    public void onGATTErr(int i) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Exception code: " + i, Toast.LENGTH_SHORT).show());
    }

    /**
     * Get NFC return data
     *
     * @param cardType Card type
     * @param cardSize Card number length
     * @param cardNo   Card number
     * @param target   00：Surface mode
     *                 01：Adult mode
     *                 02：Child mode
     * @param temp     Temperature
     * @param unit     00: Celsius
     *                 01：Fahrenheit
     */
    @Override
    public void onNFCGet(String cardType, int cardSize, String cardNo, String target, double temp, String unit) {
        runOnUiThread(() -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Card type:");
            stringBuilder.append(cardType);
            stringBuilder.append("\n");
            stringBuilder.append("Card length:");
            stringBuilder.append(cardSize);
            stringBuilder.append("\n");
            stringBuilder.append("target:");
            stringBuilder.append(target);
            stringBuilder.append("\n");
            stringBuilder.append("cardNo:");
            stringBuilder.append(cardNo);
            stringBuilder.append("\n");
            stringBuilder.append("unit:");
            stringBuilder.append(temp);
            stringBuilder.append("\n");
            stringBuilder.append("unit:");
            stringBuilder.append(unit);

            Toast.makeText(MainActivity.this, stringBuilder.toString(), Toast.LENGTH_SHORT).show();
        });
    }


    @Override
    public void onDeviceOffline() {
        Toast.makeText(this, "Please wake up the device first", Toast.LENGTH_SHORT).show();
    }

    /**
     * @param unit      "00"Degrees Celsius    "01"Fahrenheit
     * @param offset    Offset value
     * @param warmTemp  Warning temperature
     * @param funBool   Buzzer switch      "00" for open   "01" for close
     * @param sleepTime Sleep time
     */
    @Override
    public void onSettingGet(String unit, double offset, double warmTemp, String funBool, int sleepTime) {
        model.setSetting(unit, offset, warmTemp, funBool, sleepTime, tvSetting);
        this.unit = unit;
    }


    /**
     * History list
     * @param reportBeans
     */
    @Override
    public void onHistoryGet(List<ReportBean> reportBeans) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (ReportBean reportBean : reportBeans) {
                stringBuilder.append(reportBean.getTemp());
                stringBuilder.append("\n");
            }
            emitter.onNext(stringBuilder.toString());
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> tvHistory.setText(s));
    }

    //Scan callback
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice() != null
                    && !TextUtils.isEmpty(result.getDevice().getName())) {
                Log.e(TAG, "onScanResult: " + result.getDevice().getName() + " ," + result.getDevice().getAddress());
                adapter.addDevice(result.getDevice());
            }
        }
    };

    @OnClick({R.id.btnGetDeviceMessage, R.id.btnGetSetting, R.id.btnHistory})
    public void getMessage(View view) {
        switch (view.getId()) {
            case R.id.btnGetDeviceMessage:
                int deviceMessage = tempBle.getDeviceMessage();
                if (deviceMessage == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "Sent successfully", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnGetSetting:
                int setting = tempBle.getSetting();
                if (setting == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "Sent successfully", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnHistory:
                int history = tempBle.getDeviceListsMessage(DataUtils.MODE_SURFACE);
                if (history == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "Sent successfully", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @OnClick({R.id.btnScan, R.id.btnStopScan})
    public void blueToothAction(View view) {
        switch (view.getId()) {
            case R.id.btnScan:
                tempBle.startScan(scanCallback);
                break;
            case R.id.btnStopScan:
                tempBle.stopScan(scanCallback);
                break;
        }
    }


    @OnClick({R.id.btnChangeC, R.id.btnChangeF, R.id.btnOpenBuzzer, R.id.btnCloseBuzzer
            , R.id.btnOnOffDown, R.id.btnOnOffUp,
            R.id.btnTempDown, R.id.btnTempUp,
            R.id.btnOffsetDown, R.id.btnOffsetUp})
    public void setting(View view) {
        switch (view.getId()) {
            case R.id.btnChangeC:
                int setTempC = tempBle.setTempC();
                if (setTempC == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "Sent successfully", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnChangeF:
                int setTempF = tempBle.setTempF();
                if (setTempF == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "Sent successfully", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnOpenBuzzer:
                int openBuzzer = tempBle.openBuzzer();
                if (openBuzzer == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "Sent successfully", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnCloseBuzzer:
                int closeBuzzer = tempBle.closeBuzzer();
                if (closeBuzzer == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "Sent successfully", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnOnOffDown:
                int onOffDown = tempBle.onOffDown();
                if (onOffDown == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "Sent successfully", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnOnOffUp:
                int onOffUp = tempBle.onOffUp();
                if (onOffUp == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "Sent successfully", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnTempDown:
                if (TextUtils.isEmpty(unit)) {
                    Toast.makeText(this, "Please get device information first", Toast.LENGTH_SHORT).show();
                    return;
                }
                int tempDown = tempBle.tempDown(unit);
                if (tempDown == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "Sent successfully", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnTempUp:
                if (TextUtils.isEmpty(unit)) {
                    Toast.makeText(this, "Please get device information first", Toast.LENGTH_SHORT).show();
                    return;
                }
                int tempUp = tempBle.tempUp(unit);
                if (tempUp == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "Sent successfully", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnOffsetDown:
                if (TextUtils.isEmpty(unit)) {
                    Toast.makeText(this, "Please get device information first", Toast.LENGTH_SHORT).show();
                    return;
                }
                int offsetDown = tempBle.offsetDown(unit);
                if (offsetDown == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "Sent successfully", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnOffsetUp:
                if (TextUtils.isEmpty(unit)) {
                    Toast.makeText(this, "Please get device information first", Toast.LENGTH_SHORT).show();
                    return;
                }
                int offsetUp = tempBle.offsetUp(unit);
                if (offsetUp == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "Sent successfully", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
