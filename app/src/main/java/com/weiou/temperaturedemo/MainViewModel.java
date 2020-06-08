package com.weiou.temperaturedemo;

import android.app.Application;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.AndroidViewModel;

import com.weiou.lib_temp.utils.DataUtils;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainViewModel extends AndroidViewModel {

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    void setSetting(String unit, double offset, double warmTemp, String funBool, int sleepTime, TextView tvSetting) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(" Temperature unit: ");
            if ("00".equals(unit)) {
                stringBuilder.append("Celsius ");
            } else {
                stringBuilder.append("Fahrenheit");
            }
            stringBuilder.append(" Offset value: ");
            stringBuilder.append(offset);
            if ("00".equals(unit)) {
                stringBuilder.append("°C");
            } else {
                stringBuilder.append("°F");
            }

            stringBuilder.append(" Alarm value: ");
            stringBuilder.append(warmTemp);
            if ("00".equals(unit)) {
                stringBuilder.append("°C");
            } else {
                stringBuilder.append("°F");
            }

            stringBuilder.append(" Buzzer: ");
            if ("00".equals(funBool)) {
                stringBuilder.append("Turn/is on");
            } else {
                stringBuilder.append("Turn/is off");
            }

            stringBuilder.append(" Sleep time: ");
            stringBuilder.append(sleepTime);
            stringBuilder.append("second");
            emitter.onNext(stringBuilder.toString());
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tvSetting::setText);
    }


    void setTemp(int model, double temp, AppCompatTextView tvTemp) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            String mModel;
            if (model == DataUtils.MODE_SURFACE) {
                mModel = "Surface mode";
            } else if (model == DataUtils.MODE_ADULT) {
                mModel = "Adult mode";
            } else {
                mModel = "Child mode";
            }
            String s = "Current Mode:" + mModel + ";   Current Temperature: " + temp + "°C";
            emitter.onNext(s);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tvTemp::setText);
    }

    void setDeviceMessage(Integer bit0, Integer bit1, Integer bit2, AppCompatTextView tvDeviceMessage) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            StringBuilder stringBuilder = new StringBuilder("The device contains:");
            if (bit0 == 1) {
                stringBuilder.append("Surface mode ");
            }
            if (bit0 == 1) {
                stringBuilder.append("Adult mode ");
            }
            if (bit0 == 1) {
                stringBuilder.append("Child mode ");
            }
            emitter.onNext(stringBuilder.toString());
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tvDeviceMessage::setText);
    }
}
