package com.zt.vacss;

import static androidx.core.content.ContextCompat.startActivities;
import static androidx.core.content.ContextCompat.startActivity;
import static com.zt.vacss.BleClientActivity.connect_ok;
import static com.zt.vacss.BleClientActivity.disconnectFromDevice;
import static com.zt.vacss.BleClientActivity.receiveData;
import static com.zt.vacss.BleClientActivity.sendData;
import static com.zt.vacss.MainActivity.goAnim;
import static com.zt.vacss.MainActivity.hc06_online;
import static com.zt.vacss.MainActivity.left_Rotation_angle_add;
import static com.zt.vacss.MainActivity.left_Rotation_angle_del;
import static com.zt.vacss.MainActivity.left_Rotation_angle_still_time_add;
import static com.zt.vacss.MainActivity.left_Rotation_angle_still_time_del;
import static com.zt.vacss.MainActivity.left_speed_add;
import static com.zt.vacss.MainActivity.left_speed_add_low;
import static com.zt.vacss.MainActivity.left_speed_del;
import static com.zt.vacss.MainActivity.left_speed_del_low;
import static com.zt.vacss.MainActivity.left_time_add;
import static com.zt.vacss.MainActivity.left_time_del;
import static com.zt.vacss.MainActivity.low_left_time_add;
import static com.zt.vacss.MainActivity.low_left_time_del;
import static com.zt.vacss.MainActivity.low_right_time_add;
import static com.zt.vacss.MainActivity.low_right_time_del;
import static com.zt.vacss.MainActivity.mEA;
import static com.zt.vacss.MainActivity.right_Rotation_angle_add;
import static com.zt.vacss.MainActivity.right_Rotation_angle_del;
import static com.zt.vacss.MainActivity.right_Rotation_angle_still_time_add;
import static com.zt.vacss.MainActivity.right_Rotation_angle_still_time_del;
import static com.zt.vacss.MainActivity.right_move_speed_add;
import static com.zt.vacss.MainActivity.right_move_speed_add_low;
import static com.zt.vacss.MainActivity.right_move_speed_del;
import static com.zt.vacss.MainActivity.right_move_speed_del_low;
import static com.zt.vacss.MainActivity.right_move_time_add;
import static com.zt.vacss.MainActivity.right_move_time_del;
import static com.zt.vacss.MainActivity.sga_left_Rotation_angle_add;
import static com.zt.vacss.MainActivity.sga_left_Rotation_angle_del;
import static com.zt.vacss.MainActivity.sga_left_Rotation_angle_still_time_add;
import static com.zt.vacss.MainActivity.sga_left_Rotation_angle_still_time_del;
import static com.zt.vacss.MainActivity.sga_right_Rotation_angle_add;
import static com.zt.vacss.MainActivity.sga_right_Rotation_angle_del;
import static com.zt.vacss.MainActivity.sga_right_Rotation_angle_still_time_add;
import static com.zt.vacss.MainActivity.sga_right_Rotation_angle_still_time_del;
import static com.zt.vacss.MainActivity.sgb_left_Rotation_angle_add;
import static com.zt.vacss.MainActivity.sgb_left_Rotation_angle_del;
import static com.zt.vacss.MainActivity.sgb_left_Rotation_angle_still_time_add;
import static com.zt.vacss.MainActivity.sgb_left_Rotation_angle_still_time_del;
import static com.zt.vacss.MainActivity.sgb_right_Rotation_angle_add;
import static com.zt.vacss.MainActivity.sgb_right_Rotation_angle_del;
import static com.zt.vacss.MainActivity.sgb_right_Rotation_angle_still_time_add;
import static com.zt.vacss.MainActivity.sgb_right_Rotation_angle_still_time_del;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

public class buttonListener implements View.OnClickListener {
    public final Context context;

    public buttonListener(Context context){
        this.context = context;
    }
    @Override
    public void onClick(View v) {
        if (hc06_online) {
            if (v == mEA){
                if (mEA.getText().toString().contains("启 用")){
                    goAnim(context,30);
                    new AlertDialog.Builder(context)
                            .setTitle("是否己经装针?")
                            .setPositiveButton("未装针", (dialogInterface, i) -> {
                                goAnim(context,30);
                                sendData("9");//未装针，发送装针指令
                                Intent intent = new Intent(context, default_set.class);
                                intent.putExtra("zz", "no");
                                context.startActivities(new Intent[]{intent});
                            })
                            .setNegativeButton("己装针", (dialog, which) -> {
                                goAnim(context,30);
                                sendData("y");
                            })
                            .show();
                    receiveData();
                } else if (mEA.getText().toString().contains("停 用")) {
                    goAnim(context,30);
                    sendData("z");
                    receiveData();
                }
            }else if (v == left_speed_del) {
                goAnim(context,30);
                sendData("2");
                receiveData();
            } else if (v == left_speed_add) {
                goAnim(context,30);
                sendData("1");
                receiveData();
            } else if (v == left_time_del) {
                goAnim(context,30);
                sendData("5");
                receiveData();
            } else if (v == left_time_add) {
                goAnim(context,30);
                sendData("6");
                receiveData();
            } else if (v == right_move_speed_del) {
                goAnim(context,30);
                sendData("4");
                receiveData();
            } else if (v == right_move_speed_add) {
                goAnim(context,30);
                sendData("3");
                receiveData();
            } else if (v == right_move_time_del) {
                goAnim(context,30);
                sendData("7");
                receiveData();
            } else if (v == right_move_time_add) {
                goAnim(context,30);
                sendData("8");
                receiveData();
            } else if (v == left_speed_del_low) {
                goAnim(context,30);
                sendData("c");
                receiveData();
            } else if (v == left_speed_add_low) {
                goAnim(context,30);
                sendData("b");
                receiveData();
            } else if (v == low_left_time_del) {
                goAnim(context,30);
                sendData("f");
                receiveData();
            } else if (v == low_left_time_add) {
                goAnim(context,30);
                sendData("g");
                receiveData();
            } else if (v == right_move_speed_del_low) {
                goAnim(context,30);
                sendData("e");
                receiveData();
            } else if (v == right_move_speed_add_low) {
                goAnim(context,30);
                sendData("d");
                receiveData();
            } else if (v == low_right_time_del) {
                goAnim(context,30);
                sendData("h");
                receiveData();
            } else if (v == low_right_time_add) {
                goAnim(context,30);
                sendData("i");
                receiveData();
            } else if (v == left_Rotation_angle_del) {
                goAnim(context,30);
                sendData("n");
                receiveData();
            } else if (v == left_Rotation_angle_add) {
                goAnim(context,30);
                sendData("o");
                receiveData();
            } else if (v == left_Rotation_angle_still_time_del) {
                goAnim(context,30);
                sendData("t");
                receiveData();
            } else if (v == left_Rotation_angle_still_time_add) {
                goAnim(context,30);
                sendData("u");
                receiveData();
            } else if (v == right_Rotation_angle_del) {
                goAnim(context,30);
                sendData("x");
                receiveData();
            } else if (v == right_Rotation_angle_add) {
                goAnim(context,30);
                sendData("~");
                receiveData();
            } else if (v == right_Rotation_angle_still_time_del) {
                goAnim(context,30);
                sendData("v");
                receiveData();
            } else if (v == right_Rotation_angle_still_time_add) {
                goAnim(context,30);
                sendData("w");
                receiveData();
            } else if (v == sga_left_Rotation_angle_del) {
                goAnim(context,30);
                sendData("j");
                receiveData();
            } else if (v == sga_left_Rotation_angle_add) {
                goAnim(context,30);
                sendData("k");
                receiveData();
            } else if (v == sga_left_Rotation_angle_still_time_del) {
                goAnim(context,30);
                sendData("p");
                receiveData();
            } else if (v == sga_left_Rotation_angle_still_time_add) {
                goAnim(context,30);
                sendData("q");
                receiveData();
            } else if (v == sga_right_Rotation_angle_del) {
                goAnim(context,30);
                sendData("!");
                receiveData();
            } else if (v == sga_right_Rotation_angle_add) {
                goAnim(context,30);
                sendData("@");
                receiveData();
            } else if (v == sga_right_Rotation_angle_still_time_del) {
                goAnim(context,30);
                sendData("%");
                receiveData();
            } else if (v == sga_right_Rotation_angle_still_time_add) {
                goAnim(context,30);
                sendData("^");
                receiveData();
            } else if (v == sgb_left_Rotation_angle_del) {
                goAnim(context,30);
                sendData("l");
                receiveData();
            } else if (v == sgb_left_Rotation_angle_add) {
                goAnim(context,30);
                sendData("m");
                receiveData();
            } else if (v == sgb_left_Rotation_angle_still_time_del) {
                goAnim(context,30);
                sendData("r");
                receiveData();
            } else if (v == sgb_left_Rotation_angle_still_time_add) {
                goAnim(context,30);
                sendData("s");
                receiveData();
            } else if (v == sgb_right_Rotation_angle_del) {
                goAnim(context,30);
                sendData("#");
                receiveData();
            } else if (v == sgb_right_Rotation_angle_add) {
                goAnim(context,30);
                sendData("$");
                receiveData();
            } else if (v == sgb_right_Rotation_angle_still_time_del) {
                goAnim(context,30);
                sendData("&");
                receiveData();
            } else if (v == sgb_right_Rotation_angle_still_time_add) {
                goAnim(context,30);
                sendData("*");
                receiveData();
            }
        }
    }
}
