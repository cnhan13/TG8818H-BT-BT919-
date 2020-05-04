package com.weiou.temperaturedemo;

import androidx.annotation.Nullable;

public class DeviceBean {
    //状态
    private int status;//-1 为连接  1 连接
    //名字
    private String name;
    //mac地址
    private String mac;

    DeviceBean(int status, String name, String mac) {
        this.status = status;
        this.name = name;
        this.mac = mac;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof DeviceBean) {
            DeviceBean bean = (DeviceBean) obj;
            return this.name.equals(bean.name) &&
                    this.mac.equals(bean.mac);
        }
        return super.equals(obj);
    }
}
