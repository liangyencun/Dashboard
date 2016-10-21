package com.xc.dashboard;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    LocationManager locationManager;
    LocationListener locationListener;
    public AppView appView;

    private boolean sensorActive;
    private boolean speedActive;

    private Camera mCamera = null;
    private CameraView mCameraView = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        loadCamera();
        loadOverlay();
    }

    protected void onResume() {
        super.onResume();
        loadSensor();
        loadSpeed();
    }

    protected void onPause() {
        super.onPause();
        unloadSensor();
        unloadSpeed();
    }

    public void loadSensor(){
        if (sensorActive)
            return;
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorActive = true;
    }

    public void loadSpeed(){
        if (speedActive)
            return;
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (appView != null && location.getAccuracy() < 30) {
                    Log.d("debug", location.getLongitude()+","+location.getLatitude()+" "+location.getSpeed()+"m/s");
                    appView.speed = location.getSpeed();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 5, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 50, 5, locationListener);
        }
        catch (SecurityException e){
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_LONG).show();
        }
        speedActive = true;
    }

    public void loadCamera(){
        Log.d("MainActivity", "loadCamera");
        try{
            mCamera = Camera.open();
        } catch (Exception e) {
            Toast.makeText(this, "Error opening camera!", Toast.LENGTH_LONG).show();
        }
        if(mCamera != null) {
            mCameraView = new CameraView(this, mCamera);
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);
        }
    }

    public void loadOverlay(){
        Log.d("AppView", "Load Overlay");
        appView = new AppView(this);
        FrameLayout appView_ = (FrameLayout) findViewById(R.id.appview);
        if (appView != null) {
            appView_.addView(appView);
        }
    }

    public void unloadSensor(){
        mSensorManager.unregisterListener(this);
        Log.i("Dashboard", "Sensors Unloaded");
        sensorActive = false;
    }

    public void unloadSpeed(){
        try {
            locationManager.removeUpdates(locationListener);
        }
        catch (SecurityException e){ }
        locationManager = null;
        locationListener = null;
        Log.i("Dashboard", "Location Unloaded");
        speedActive = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER){
            return;
        }
        if (appView != null) {
            appView.accelX = event.values[0];
            appView.accelY = event.values[1];
            appView.accelZ = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void toggleMetric(View button){
        appView.metric = ((ToggleButton)button).isChecked();
    }

    public void toggleRecord(View button) {
        mCameraView.record = ((CheckBox) button).isChecked();
        if (mCameraView.record){
            appView.appThread.setAppState(AppThread.AppState.RECORD);
            appView.startTime = System.currentTimeMillis();
        }
        else{
            appView.appThread.setAppState(AppThread.AppState.CAMERA);
        }
        mCameraView.run();
    }

}
