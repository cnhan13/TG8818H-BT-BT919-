package com.weiou.temperaturedemo;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private final static List<DeviceBean> list = new ArrayList<>();
    private static DeviceCallback callback;

    DeviceAdapter(DeviceCallback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public DeviceAdapter.DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAdapter.DeviceViewHolder holder, int position) {
        holder.name.setText(list.get(position).getName());
        if (list.get(position).getStatus() == -1) {
            holder.radio.setChecked(false);
        } else {
            holder.radio.setChecked(true);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    void addDevice(BluetoothDevice device) {
        DeviceBean bean = new DeviceBean(-1, device.getName(), device.getAddress());
        synchronized (list) {
            boolean contains = list.contains(bean);
            if (contains) {
                return;
            }
            list.add(bean);
            notifyDataSetChanged();
        }
    }

    void updateStatus(String mac) {
        Observable.create(emitter -> {
            for (int i = 0; i < list.size(); i++) {
                if (mac.equals(list.get(i).getMac())) {
                    list.get(i).setStatus(1);
                } else {
                    list.get(i).setStatus(-1);
                }
            }
            emitter.onNext(new Object());
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> notifyDataSetChanged());
    }

    void clearList() {
        list.clear();
        notifyDataSetChanged();
    }

    void resetList() {
        Observable.create(emitter -> {
            for (DeviceBean bean : list) {
                bean.setStatus(-1);
            }
            notifyDataSetChanged();
        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();

    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private RadioButton radio;

        DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_name);
            radio = itemView.findViewById(R.id.radiobutton);
            itemView.setOnClickListener(v -> callback.onItemClick(list.get(getAdapterPosition()).getMac()));
            radio.setClickable(false);
        }
    }

    public interface DeviceCallback {
        void onItemClick(String mac);
    }
}
