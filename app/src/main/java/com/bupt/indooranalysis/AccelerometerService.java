package com.bupt.indooranalysis;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class AccelerometerService extends Service implements SensorEventListener{

    private String LOG_TAG = "AccelerometerService";
    private float mGravity = SensorManager.STANDARD_GRAVITY - 0.8F;
    public SensorManager sensorManager;
    public Sensor sensor;
    private float GRAVITY_FILTER = 10.7f;

    public interface OnAccelerometerChangeListener{
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
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);

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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

            if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                float a = (float) Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
                float delta = Math.abs(a-GRAVITY_FILTER);

                //    Log.i(LOG_TAG,"X =>"+Float.toString(x)+" Y =>"+Float.toString(y) + " Z =>"+Float.toString(z));
                //Log.i(LOG_TAG,"Acceleration=>"+Float.toString(a));
                if(delta > 1.5){
                    if(onAccelerometerChangeListener!=null){
                        onAccelerometerChangeListener.onAccelerationChange(delta);
                    }
                    Log.i(LOG_TAG,"Delta=>"+Float.toString(delta));
                }
            }



    }

    public class MyBinder extends Binder {

        public AccelerometerService getService(){
            return AccelerometerService.this;
        }
    }

    private MyBinder myBinder = new MyBinder();

}
