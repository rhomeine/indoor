package com.bupt.indooranalysis;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.bupt.indooranalysis.fragment.HistoryFragment;

import java.security.Timestamp;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AccelerometerService extends Service implements SensorEventListener {

    private String LOG_TAG = "AccelerometerService";
    private float mGravity = SensorManager.STANDARD_GRAVITY - 0.8F;
    public SensorManager sensorManager;
    public Sensor sensor;
    private float GRAVITY_FILTER = 10.7f;
    private List<Float> accelelist = new ArrayList<Float>();
    private Timer acceletimer = new Timer();
    private Handler handler;
    //计步算法所需参数
    private float Gx = 0;
    private float Gy = 0;
    private float Gz = 0;
    private float a = 0;//采样所需的加速度样本
    private int acceleFlag = 0;
    private List<Float> newaccelelist1;
    private List<Float> newaccelelist2;
    private int acceleCount = 0;
    private float X;//自相关系数
    //磁感应计算所需参数
    private int count = 1;
    private float Bx = 0;
    private float By = 0;
    private float Bz = 0;


    public interface OnAccelerometerChangeListener {
        void onAccelerationChange(float delta);
    }

    private OnAccelerometerChangeListener onAccelerometerChangeListener;

    public void setOnAccelerometerChangeListener(OnAccelerometerChangeListener onAccelerometerChangeListener) {
        this.onAccelerometerChangeListener = onAccelerometerChangeListener;
    }

    public AccelerometerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);

        handler = new MHandler();
        acceletimer.schedule(new TimerTask() {
            @Override
            public void run() {
                acceleCount += 1;
                accelelist.add(a);

                if (acceleCount == 40) {
                    acceleCount = 0;
                    Message msg = new Message();
                    msg.what = 0x01;
                    handler.sendMessage(msg);
                }
            }
        }, 3000, 20);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
        super.onDestroy();

    }

    @Override
    public boolean onUnbind(Intent intent) {
        sensorManager.unregisterListener(this);
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        super.onRebind(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            Gx = event.values[0];
            Gy = event.values[1];
            Gz = event.values[2];

            a = (float) Math.sqrt(Math.pow(Gx, 2) + Math.pow(Gy, 2) + Math.pow(Gz, 2));
            float delta = Math.abs(a - GRAVITY_FILTER);

            //    Log.i(LOG_TAG,"X =>"+Float.toString(x)+" Y =>"+Float.toString(y) + " Z =>"+Float.toString(z));
            Log.i(LOG_TAG,"Acceleration=>"+Float.toString(a));
            if (delta > 1.5) {
                if (onAccelerometerChangeListener != null) {
                    onAccelerometerChangeListener.onAccelerationChange(delta);
                }
//                Log.i(LOG_TAG, "Delta=>" + Float.toString(delta));
            }
        }else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            //if(count ++== 20){
                count = 1;
                Bx = event.values[0];
                By = event.values[1];
                Bz = event.values[2];
//                Log.d(LOG_TAG,Bx+" "+By+" "+Bz);
            float tan = a*(Bz*Gx-Bx*Gz)/(By*(Gx*Gx+Gz*Gz)-Gy*(Bx*Gx+Bz*Gz));
//            Log.d(LOG_TAG,tan+"");
            //}

        }


    }

    public class MyBinder extends Binder {

        public AccelerometerService getService() {
            return AccelerometerService.this;
        }
    }

    private MyBinder myBinder = new MyBinder();

    private class MHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x01) {

                if (acceleFlag == 0) {
                    acceleFlag = 2;
                    newaccelelist1 = accelelist;
                    accelelist = new ArrayList<Float>();
                } else if (acceleFlag == 1) {
                    acceleFlag = 2;
                    newaccelelist1 = accelelist;
                    accelelist = new ArrayList<Float>();
                    //
                    float mean1 = 0;
                    float variance1 = 0;
                    for (float i : newaccelelist1) {
                        mean1 += i;
                    }
                    mean1 = mean1 / newaccelelist1.size();
                    for (float i : newaccelelist1) {
                        variance1 += (float) Math.pow(i - mean1, 2);
                    }
                    variance1 = (float) Math.sqrt(variance1);
                    //
                    float mean2 = 0;
                    float variance2 = 0;
                    for (float i : newaccelelist2) {
                        mean2 += i;
                    }
                    mean2 = mean2 / newaccelelist2.size();
                    for (float i : newaccelelist2) {
                        variance2 += (float) Math.pow(i - mean2, 2);
                    }
                    variance2 = (float) Math.sqrt(variance2);
                    //
                    float X1 = 0;
                    if (newaccelelist1.size() == newaccelelist2.size()) {
                        for (int i = 0; i < newaccelelist1.size(); i++) {
                            X1 += (newaccelelist1.get(i) - mean1) * (newaccelelist2.get(i) - mean2);
                        }
                        X1 = X1 / (variance1 * variance2);
                        X = X1;
                    }
                    HistoryFragment.blueTime.add(X);
//                    Log.d("Accelero", "1: " + newaccelelist1.size() + " " + X1 + " " + "variance1: " + variance1 + " " + variance2);
                } else if (acceleFlag == 2) {
                    acceleFlag = 1;
                    newaccelelist2 = accelelist;
                    accelelist = new ArrayList<Float>();
                    //
                    float mean1 = 0;
                    float variance1 = 0;
                    for (float i : newaccelelist1) {
                        mean1 += i;
                    }
                    mean1 = mean1 / newaccelelist1.size();
                    for (float i : newaccelelist1) {
                        variance1 += (float) Math.pow(i - mean1, 2);
                    }
                    variance1 = (float) Math.sqrt(variance1);
                    //
                    float mean2 = 0;
                    float variance2 = 0;
                    for (float i : newaccelelist2) {
                        mean2 += i;
                    }
                    mean2 = mean2 / newaccelelist2.size();
                    for (float i : newaccelelist2) {
                        variance2 += (float) Math.pow(i - mean2, 2);
                    }
                    variance2 = (float) Math.sqrt(variance2);
                    //
                    float X2 = 0;
                    if (newaccelelist1.size() == newaccelelist2.size()) {
                        for (int i = 0; i < newaccelelist1.size(); i++) {
                            X2 += (newaccelelist1.get(i) - mean1) * (newaccelelist2.get(i) - mean2);
                        }
                        X2 = X2 / (variance1 * variance2);
                        X = X2;
                    }
//                    Log.d("Accelero", "2: " + newaccelelist2.size() + " " + X2 + " " + "variance1: " + variance1 + " " + variance2);
                    HistoryFragment.blueTime.add(X);
                }
            }
        }
    }

}
