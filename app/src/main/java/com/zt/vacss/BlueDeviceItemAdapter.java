package com.zt.vacss;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BlueDeviceItemAdapter extends RecyclerView.Adapter<BlueDeviceItemAdapter.myViewHolder> {
    private final List<BluetoothDevice> mDeviceList;
    private final Context context;

    public BlueDeviceItemAdapter(List<BluetoothDevice> mDeviceList, Context context) {
        this.mDeviceList = mDeviceList;
        this.context = context;
    }
    @NonNull
    @Override
    public BlueDeviceItemAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.activity_bluetooth_scan, null);
        return new myViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull BlueDeviceItemAdapter.myViewHolder holder, int position) {
        holder.bv.setText(mDeviceList.get(position).getName());
        Toast.makeText(context, mDeviceList.get(position).getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return mDeviceList==null ? 0 :mDeviceList.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder{
        private final TextView bv;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            bv = itemView.findViewById(R.id.bv);
        }
    }
}
