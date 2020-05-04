package com.weiou.temperaturedemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.weiou.lib_temp.ReportBean;
import com.weiou.lib_temp.TempBle;
import com.weiou.lib_temp.TempBleCallback;
import com.weiou.lib_temp.utils.DataUtils;

import java.util.List;

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
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "BLE 已断开连接", Toast.LENGTH_SHORT).show());
    }

    /**
     * @param model 当前额温枪模式
     *              DataUtils.MODE_SURFACE
     *              DataUtils.MODE_ADULT
     *              DataUtils.MODE_CHILD
     * @param temp  额温枪返回温度
     */
    @Override
    public void onTempGet(int model, double temp) {
        this.model.setTemp(model, temp, tvTemp);
    }

    /**
     * 当前额温枪设备含有的模式
     *
     * @param bit0 表面模式    0为不含 1为含有
     * @param bit1 成人模式    0为不含 1为含有
     * @param bit2 儿童模式    0为不含 1为含有
     */
    @Override
    public void onDeviceMessageGet(Integer bit0, Integer bit1, Integer bit2) {
        model.setDeviceMessage(bit0, bit1, bit2, tvDeviceMessage);

    }

    /**
     * 设备休眠
     */
    @Override
    public void onOffline() {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "设备已离线", Toast.LENGTH_SHORT).show());
    }

    /**
     * 设备被唤醒
     */
    @Override
    public void onWakeUp() {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "设备已唤醒", Toast.LENGTH_SHORT).show());
    }

    /**
     * 蓝牙异常代码返回
     *
     * @param i 详情https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/android-cts-5.1_r17/stack/include/gatt_api.h
     */
    @Override
    public void onGATTErr(int i) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "异常码: " + i, Toast.LENGTH_SHORT).show());
    }

    /**
     * 获取NFC返回数据
     *
     * @param cardType 卡类型
     * @param cardSize 卡号长度
     * @param cardNo   卡号
     * @param target   00：表面模式
     *                 01：成人模式
     *                 02：儿童模式
     * @param temp     温度
     * @param unit     00: 摄氏度
     *                 01：华氏度
     */
    @Override
    public void onNFCGet(String cardType, int cardSize, String cardNo, String target, double temp, String unit) {
        runOnUiThread(() -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("卡类型:");
            stringBuilder.append(cardType);
            stringBuilder.append("\n");
            stringBuilder.append("卡长度:");
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
        Toast.makeText(this, "请先唤醒设备", Toast.LENGTH_SHORT).show();
    }

    /**
     * @param unit      "00"为摄氏度    "01"为华氏度
     * @param offset    偏移值
     * @param warmTemp  警告温度
     * @param funBool   蜂鸣器开关      "00"为开   "01"为关
     * @param sleepTime 休眠时间
     */
    @Override
    public void onSettingGet(String unit, double offset, double warmTemp, String funBool, int sleepTime) {
        model.setSetting(unit, offset, warmTemp, funBool, sleepTime, tvSetting);
        this.unit = unit;
    }


    /**
     * 历史记录列表
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

    //扫描回调
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
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnGetSetting:
                int setting = tempBle.getSetting();
                if (setting == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnHistory:
                int history = tempBle.getDeviceListsMessage(DataUtils.MODE_SURFACE);
                if (history == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnChangeF:
                int setTempF = tempBle.setTempF();
                if (setTempF == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnOpenBuzzer:
                int openBuzzer = tempBle.openBuzzer();
                if (openBuzzer == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnCloseBuzzer:
                int closeBuzzer = tempBle.closeBuzzer();
                if (closeBuzzer == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnOnOffDown:
                int onOffDown = tempBle.onOffDown();
                if (onOffDown == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnOnOffUp:
                int onOffUp = tempBle.onOffUp();
                if (onOffUp == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnTempDown:
                if (TextUtils.isEmpty(unit)) {
                    Toast.makeText(this, "请先获取设备信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                int tempDown = tempBle.tempDown(unit);
                if (tempDown == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnTempUp:
                if (TextUtils.isEmpty(unit)) {
                    Toast.makeText(this, "请先获取设备信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                int tempUp = tempBle.tempUp(unit);
                if (tempUp == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnOffsetDown:
                if (TextUtils.isEmpty(unit)) {
                    Toast.makeText(this, "请先获取设备信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                int offsetDown = tempBle.offsetDown(unit);
                if (offsetDown == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnOffsetUp:
                if (TextUtils.isEmpty(unit)) {
                    Toast.makeText(this, "请先获取设备信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                int offsetUp = tempBle.offsetUp(unit);
                if (offsetUp == DataUtils.CODE_SUCCESS) {
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
