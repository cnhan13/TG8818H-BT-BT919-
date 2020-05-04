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
            stringBuilder.append(" 温度单位: ");
            if ("00".equals(unit)) {
                stringBuilder.append("摄氏度 ");
            } else {
                stringBuilder.append("华氏度");
            }
            stringBuilder.append(" 偏移值: ");
            stringBuilder.append(offset);
            if ("00".equals(unit)) {
                stringBuilder.append("°C");
            } else {
                stringBuilder.append("°F");
            }

            stringBuilder.append(" 报警值: ");
            stringBuilder.append(warmTemp);
            if ("00".equals(unit)) {
                stringBuilder.append("°C");
            } else {
                stringBuilder.append("°F");
            }

            stringBuilder.append(" 蜂鸣器: ");
            if ("00".equals(funBool)) {
                stringBuilder.append("开");
            } else {
                stringBuilder.append("关");
            }

            stringBuilder.append(" 休眠时间: ");
            stringBuilder.append(sleepTime);
            stringBuilder.append("秒");
            emitter.onNext(stringBuilder.toString());
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tvSetting::setText);
    }


    void setTemp(int model, double temp, AppCompatTextView tvTemp) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            String mModel;
            if (model == DataUtils.MODE_SURFACE) {
                mModel = "表面模式";
            } else if (model == DataUtils.MODE_ADULT) {
                mModel = "成人模式";
            } else {
                mModel = "儿童模式";
            }
            String s = "当前模式:" + mModel + ";   当前温度: " + temp + "°C";
            emitter.onNext(s);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tvTemp::setText);
    }

    void setDeviceMessage(Integer bit0, Integer bit1, Integer bit2, AppCompatTextView tvDeviceMessage) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            StringBuilder stringBuilder = new StringBuilder("该设备包含:");
            if (bit0 == 1) {
                stringBuilder.append("表面模式 ");
            }
            if (bit0 == 1) {
                stringBuilder.append("成人模式 ");
            }
            if (bit0 == 1) {
                stringBuilder.append("儿童模式 ");
            }
            emitter.onNext(stringBuilder.toString());
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tvDeviceMessage::setText);
    }
}
