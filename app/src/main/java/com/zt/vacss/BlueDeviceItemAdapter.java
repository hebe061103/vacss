package com.zt.vacss;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class BlueDeviceItemAdapter extends RecyclerView.Adapter<BlueDeviceItemAdapter.MyViewHolder> {
    private final List<BluetoothDevice> mDeviceList;    //接受数据
    private final Context context;    //接受上下文

    public BlueDeviceItemAdapter(List<BluetoothDevice> mDeviceList, Context context) {
        this.mDeviceList = mDeviceList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //拿到布局
        View view = View.inflate(context, R.layout.blueinfo, null);
        //在ViewHolder中进行编码操作
        return new MyViewHolder(view);
    }

    @SuppressLint({"MissingPermission", "SetTextI18n"})
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tv.setText(mDeviceList.get(position).getName() + "  " + mDeviceList.get(position).getAddress());
    }

    @Override
    public int getItemCount() {
        return mDeviceList == null ? 0 : mDeviceList.size();
    }

    //创建ViewHolder类继承RecyclerView.ViewHolder
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv;

        public MyViewHolder(View itemView) {
            super(itemView);
            //找到控件
            tv = itemView.findViewById(R.id.bt_text);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //判断点击MonItemClickListener是否为空
                    if (MonItemClickListener !=null) {
                        //对MonItemClickListener进行点击
                        MonItemClickListener.OnRecyclerItemClickListener(getAdapterPosition());
                    }
                }
            });
        }
    }
    private static OnRecyclerItemClickListener MonItemClickListener;
    //设置点击监听事件用于外部引用
    public void setRecyclerItemClickListener(OnRecyclerItemClickListener listener){
        MonItemClickListener=listener;
    }
    //创建点击类接口
    public interface OnRecyclerItemClickListener{
        void OnRecyclerItemClickListener(int postion);
    }
}