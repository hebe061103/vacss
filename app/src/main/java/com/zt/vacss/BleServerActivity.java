package com.zt.vacss;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;

/** @noinspection deprecation*/
public class BleServerActivity extends AppCompatActivity implements EasyPermissions.RationaleCallbacks {
    private TextView mStatusView;
    private TextView mClientName;
    private Button start_sever;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothGattServer mBluetoothGattServer;
    private final BluetoothGattServerCallback mBluetoothGattServerCallback = new BluetoothGattServerCallback() {
        //设备连接/断开连接回调
        @SuppressLint({"MissingPermission", "SetTextI18n"})
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            showLog("连接状态发生改变，安卓系统回调onConnectionStateChange:device name="+ device.getName()+ "address="+device.getAddress()+ "status="+status+"newstate="+newState);
            if (newState == BluetoothProfile.STATE_CONNECTED){
                if (mClientName != null) {
                    mClientName.setText("client name : " + device.getName());
                }
            }
        }

        //特征值读取回调
        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            showLog("客户端有读的请求，安卓系统回调该onCharacteristicReadRequest()方法");
            Date dd=new Date();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strV;
            strV = sim.format(dd);
            mBluetoothGattServer.sendResponse(device,requestId,BluetoothGatt.GATT_SUCCESS,offset,strV.getBytes());
        }

        //特征值写入回调
        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            showLog("客户端有写的请求，安卓系统回调该onCharacteristicWriteRequest()方法");
            String strV = new String(value);
            showLog("onCharacteristicWriteRequest strV " + strV);
            if (!TextUtils.isEmpty(strV) && mStatusView != null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStatusView.setText(strV);
                    }
                });
            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStatusView = findViewById(R.id.tv_server_status);
                    }
                });
            }
            //特征被读取，在该回调方法中回复客户端响应成功
            //这个不返回 客户端 无法 read 服务端数据，而且返回了客户端没有响应？？？？？
            mBluetoothGattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,offset,value);

            //处理响应内容
            //value:客户端发送过来的数据
            //onResponseToClient(value,device,requestId,characteristic);
        }

        //描述读取回调
        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            showLog("server onDescriptorReadRequest ");
        }

        //描述写入回调
        @SuppressLint("MissingPermission")
        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            showLog("server onDescriptorWriteRequest ");
            mBluetoothGattServer.sendResponse(device,requestId,BluetoothGatt.GATT_SUCCESS,offset,value);
        }

        //添加本地服务回调
        @Override
        public void onServiceAdded(int status, BluetoothGattService service){
            super.onServiceAdded(status,service);
            if (status == BluetoothGatt.GATT_SUCCESS){
                isBroadcasting(true);
                showToast("添加自定义 服务成功");
            }else {
                startBroadcastFail("On onServiceAdded");
            }
        }
    };

    private final AdvertiseCallback mAdertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            showToast("server 广播成功");
            initServices(BleServerActivity.this);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            showToast("server 广播失败");
            startBroadcastFail("On startAdvertising");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_server);
        start_sever=findViewById(R.id.start_server);
        mStatusView = findViewById(R.id.tv_server_status);
        mClientName = findViewById(R.id.tv_client_name);
        initClick();
    }

    private void initClick() {
        start_sever.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startServer();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBroadcast();
    }
    protected void onResume() {
        super.onResume();
    }
    private void startServer() {
        //先判断有没有权限
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showToast("没有给蓝牙权限");
            requestBluetoothPermissions();
            return;
        }

        //判断及启动 位置信息
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //判断是否开启了GPS
        boolean ok = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!ok){
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            int LOCATION_START_RECODE = 3;
            startActivityForResult(intent, LOCATION_START_RECODE);
        }

        if(mBluetoothLeAdvertiser==null){
            mBluetoothManager=(BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            if(mBluetoothManager!=null){
                BluetoothAdapter bluetoothAdapter=mBluetoothManager.getAdapter();
                if(bluetoothAdapter!=null){
                    //使自身蓝牙可见
                    if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                        startActivity(discoverableIntent);
                    }
                    mBluetoothLeAdvertiser=bluetoothAdapter.getBluetoothLeAdvertiser();
                    startBroadcast();
                }else{
                    showToast("设备不支持蓝牙广播");
                }
            }else{
                showToast("不支持蓝牙");
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startBroadcast() {
        showToast("服务开始广播");
        AdvertiseSettings settings = buildAdvertiseSettings();
        AdvertiseData data = buildAdvertiseData();
        if(mBluetoothLeAdvertiser!=null){
            mBluetoothLeAdvertiser.startAdvertising(settings,data,mAdertiseCallback);
        }
    }

    @SuppressLint("MissingPermission")
    private void stopBroadcast() {
        if (mBluetoothLeAdvertiser!=null){
            mBluetoothLeAdvertiser.stopAdvertising(mAdertiseCallback);
            showToast("服务停止广播");
            isBroadcasting(false);
        }
    }

    private AdvertiseSettings buildAdvertiseSettings(){
        AdvertiseSettings.Builder settingsBuilder=new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingsBuilder.setTimeout(0);

        return settingsBuilder.build();
    }

    private AdvertiseData buildAdvertiseData(){
        AdvertiseData.Builder dataBuilder=new AdvertiseData.Builder();
        dataBuilder.setIncludeDeviceName(true);

        return dataBuilder.build();
    }
    @SuppressLint("MissingPermission")
    private void initServices(Context context){
        if (mBluetoothManager != null){
            mBluetoothGattServer = mBluetoothManager.openGattServer(context,mBluetoothGattServerCallback);
            BluetoothGattService service = new BluetoothGattService(UUID.randomUUID(),BluetoothGattService.SERVICE_TYPE_PRIMARY);
            BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(UUID.randomUUID(),
                    BluetoothGattCharacteristic.PROPERTY_WRITE|
                            BluetoothGattCharacteristic.PROPERTY_NOTIFY|
                            BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_WRITE|
                            BluetoothGattCharacteristic.PERMISSION_READ);

            service.addCharacteristic(characteristic);
            boolean result = mBluetoothGattServer.addService(service);
            if (!result) {
                startBroadcastFail("on initServices");
            }
        }
    }

    private void requestBluetoothPermissions() {
        int BLUE_PERMISSIONS_RECODE = 1;
        requestPermissions(new String[]{
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.BLUETOOTH_ADVERTISE
        }, BLUE_PERMISSIONS_RECODE);
    }

    private void isBroadcasting(Boolean flag){
        if (mStatusView != null){
            mStatusView.setText(flag?"广播中...":"广播未启动");
        }
    }

    @SuppressLint("SetTextI18n")
    private void startBroadcastFail(String where){
        if (mStatusView != null){
            mStatusView.setText("启动广播失败 在: " + where);
        }
    }
    private void showLog(String text){
        String TAG = "BleServerActivity";
        Log.d(TAG, "showLog: " + text);
    }
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 回调结果传递给EasyPermission
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    @Override
    public void onRationaleAccepted(int requestCode) {
        showLog("权限申请成功!");
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        showLog("用户巳拒绝权限申请!");
    }
}
