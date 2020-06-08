## 使用教程

------

[TOC]



#### 引入jar包

1. 将jar包放入 '/项目目录/app/libs' 下

   ![](https://gitee.com/jChenys/TemperatureDemo/blob/master/img/1586317587(1).jpg)

2. 在**app**的**build.gradle**中添加依赖

   ```java
   dependencies {
       ...
       implementation files('libs\\temp_v1.1.jar')
   }
   ```



#### 添加权限（AndroidManifest.xml）

```java
<!-- 基本蓝牙权限 -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

<!-- 5.0之后申请GPS硬件功能 -->
<uses-feature android:name="android.hardware.location.gps" />
<!-- 6.0之后不打开扫描不能使用 -->

<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- 支持蓝牙的手机才能安装 -->
<uses-feature
    android:name="android.hardware.bluetooth_le"
    android:required="true" />
```



#### 初始化jar包

1. 创建自己的Application

   ```java
   public class MyApp extends Application {
       @Override
       public void onCreate() {
           super.onCreate();
           //初始化
           CommonModule.init(this);
       }
   }
   ```

2. AndroidManifest.xml 中添加 Application

   ```java
   <application
       android:name=".MyApp"
       ...>
   ```



#### 动态申请权限（ACCESS_FINE_LOCATION ， 可以参考demo的BaseActivity）

> ACCESS_FINE_LOCATION  无该权限可能会无法扫描出设备



------



## Api介绍

### 初始化TempBle对象

```java
tempBle = new TempBle(BluetoothAdapter.getDefaultAdapter(), this);
```



### 蓝牙相关

#### 1. 蓝牙开启扫描

```java
//scanCallback为扫描回调
tempBle.startScan(scanCallback);
```

#### 2.蓝牙停止扫描

```java
//scanCallback为扫描回调
tempBle.stopScan(scanCallback);
```

#### 3.蓝牙连接BLE

```java
//mac为蓝牙MAC地址
tempBle.connectBle(mac);
```

#### 4.蓝牙断开BLE连接

```java
tempBle.disconnect();
```

#### 5.蓝牙扫描回调例子

```java
//扫描回调
private ScanCallback scanCallback = new ScanCallback() {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        if (result.getDevice() != null
                && !TextUtils.isEmpty(result.getDevice().getName())) {
            Log.e(TAG, "onScanResult: " + result.getDevice().getName() + " ," + result.getDevice().getAddress());
        }
    }
};
```



### 蓝牙交互相关

#### 1.Get device information

```java
tempBle.getDeviceMessage()
```

#### 2.获取系统设置

```java
tempBle.getSetting();
```

#### 3.获取历史列表数据

```java
/**
*	DataUtils.MODE_SURFACE Surface mode
*	DataUtils.MODE_ADULT Adult mode
*	DataUtils.MODE_CHILD Child mode
*/
tempBle.getDeviceListsMessage(DataUtils.MODE_SURFACE)
```

#### 4.设置摄氏度单位

```java
tempBle.setTempC();
```

#### 5.设置华氏度单位

```java
tempBle.setTempF();
```

#### 6.Turn on the buzzer

```java
tempBle.openBuzzer();
```

#### 7.Turn off the buzzer

```java
tempBle.closeBuzzer();
```

#### 8.减小休眠时间

```java
tempBle.onOffDown();
```

#### 9.Increase sleep time

```java
tempBle.onOffUp();
```

#### 10.Reduce warning temperature

```java
/**
* "00" 	Celsius
* "01"	Fahrenheit
*/
tempBle.tempDown(unit);
```

#### 11.Increase warning temperature

```java
/**
* "00" 	Celsius
* "01"	Fahrenheit
*/
tempBle.tempUp(unit);
```

#### 12.Decrease the offset value

```java
/**
* "00" 	Celsius
* "01"	Fahrenheit
*/
tempBle.offsetDown(unit);
```

#### 13.Increase offset value

```java
/**
* "00" 	Celsius
* "01"	Fahrenheit
*/
tempBle.offsetUp(unit)
```

### 

### 返回值说明

> 上面的方法都有一个返回值
>
> ```java
> // DataUtils.CODE_SUCCESS		Sent successfully
> // DataUtils.CODE_GATT_NULL		GATT为空
> // DataUtils.CODE_CHARACTERISTIC_NULL		特征值为空
> ```



### 蓝牙通知

#### TempBleCallback 接口

#### 1.BLE连接成功

```java
@Override
public void onConnectSuccess(String mac) {
    //mac 蓝牙地址
}
```

#### 2.BLE断开连接

```java
@Override
public void onDisconnect() {
    runOnUiThread(() -> Toast.makeText(MainActivity.this, "BLE Disconnected", Toast.LENGTH_SHORT).show());
}
```

#### 3.额温枪温度返回

```java
    /**
     *
     * @param model Current forehead gun mode
     *                  DataUtils.MODE_SURFACE
     *                  DataUtils.MODE_ADULT
     *                  DataUtils.MODE_CHILD
     * @param temp  Forehead gun return temperature
     */
    @Override
    public void onTempGet(int model, double temp) {
        this.model.setTemp(model, temp, tvTemp);
    }
```

#### 4.额温枪返回设备信息

```java
    /**
     * Modes included in current forehead gun equipment
     * @param bit0  Surface mode    0 is not included 1 is included
     * @param bit1  Adult mode    0 is not included 1 is included
     * @param bit2  Child mode    0 is not included 1 is included
     */
    @Override
    public void onDeviceMessageGet(Integer bit0, Integer bit1, Integer bit2) {
    }
```

#### 5.额温枪返回设置信息

```java
/**
 * 
 * @param unit      "00"Degrees Celsius    "01"Fahrenheit
 * @param offset    Offset value
 * @param warmTemp  Warning temperature
 * @param funBool   Buzzer switch      "00"为开   "01"为关
 * @param sleepTime Sleep time
 */
@Override
public void onSettingGet(String unit, double offset, double warmTemp, String funBool, int sleepTime) {
    model.setSetting(unit, offset, warmTemp, funBool, sleepTime, tvSetting);
    this.unit = unit;
}
```

#### 6.额温枪返回历史列表

```java
/**
*	reportBeans 历史数据列表
*/
@Override
public void onHistoryGet(List<ReportBean> reportBeans) {
    
}
```

#### 7.当发送数据给蓝牙时 额温枪处于休眠状态

```java
@Override
public void onDeviceOffline() {
    Toast.makeText(this, "请先唤醒设备", Toast.LENGTH_SHORT).show();
}
```

#### 8.额温枪休眠

```java
@Override
public void onOffline() {
    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Device is offline", Toast.LENGTH_SHORT).show());
}
```

#### 9.额温枪被唤醒

```java
@Override
public void onWakeUp() {
    runOnUiThread(() -> Toast.makeText(MainActivity.this, "设备已唤醒", Toast.LENGTH_SHORT).show());
}
```

#### 10.蓝牙异常代码返回

```java
/**
 * 蓝牙异常代码返回
 *
 * @param i 详情https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/android-cts-5.1_r17/stack/include/gatt_api.h
 */
@Override
public void onGATTErr(int i) {
    runOnUiThread(() -> Toast.makeText(MainActivity.this, "异常码: " + i, Toast.LENGTH_SHORT).show());
}
```

11.NFC上传数据

```java
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
   
}
```

