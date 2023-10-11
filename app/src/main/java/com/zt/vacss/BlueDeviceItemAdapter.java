package com.zt.vacss;

import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class BlueDeviceItemAdapter extends BaseAdapter{
    private List<BluetoothDevice> mDataList;
    public BlueDeviceItemAdapter(List<BluetoothDevice> dataList) {
        this.mDataList = dataList;

    }
    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int i) {
        return mDataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}
