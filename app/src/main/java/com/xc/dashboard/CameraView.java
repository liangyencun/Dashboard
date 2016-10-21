package com.xc.dashboard;


import android.content.Context;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;
import android.widget.Toast;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;

/**
 * Created by Jimmy on 2016/6/1.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback, MediaRecorder.OnInfoListener{

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean cameraEnable;
    public boolean record; //whether or not the record button is enabled or not
    private boolean isRecording;
    private MediaRecorder recorder;
    private String saveFile;
    private File recording;

    public CameraView(Context context, Camera camera){
        super(context);

        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d("CameraView", "surfaceCreated, cameraEnable:"+cameraEnable);
        if (!cameraEnable) {
            try{
                mCamera = Camera.open();
            } catch (Exception e) {
                //Toast.makeText(getContext(), "Error opening camera!", Toast.LENGTH_LONG).show();
            }
        }
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            cameraEnable = true;
        } catch (IOException e) {
            cameraEnable = false;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (cameraEnable) {
            mCamera.stopPreview();
            mCamera.release();
            cameraEnable = false;
        }
        Log.d("CameraView", "surfaceDestroyed, cameraEnable:"+cameraEnable);
    }

    public void run(){
        if (saveFile == null){
            if (!createFile()) {
                Toast.makeText(getContext(), "Error saving file!", Toast.LENGTH_LONG).show();
                return;
            }
        }
        if (record){
            startRecording();
        }
        else{
            stopRecording();
        }
    }

    public boolean createFile(){
        String state = Environment.getExternalStorageState();
        boolean storageAvail = Environment.MEDIA_MOUNTED.equals(state);
        if (storageAvail){
            File tmp = Environment.getExternalStorageDirectory();
            tmp = new File(tmp.getPath()+"/Dashboard");
            tmp.mkdir();
            recording = new File(tmp, "recording.mp4");
            try{
                recording.createNewFile();
                return true;
            }catch(IOException e){
                e.printStackTrace();
            }
            Log.d("file", "isFile:"+recording.isFile());
        }
        return false;
    }

    public void startRecording(){
        mCamera.unlock();
        recorder = new MediaRecorder();
        recorder.setCamera(mCamera);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
        recorder.setOutputFile(recording.getPath());
        recorder.setMaxDuration(600000); //10 minutes
        recorder.setOnInfoListener(this);
        try{
            recorder.prepare();
        } catch (IOException e){
            e.printStackTrace();
        }
        recorder.start();
        Log.i("Camera", "Recording to "+recording.getPath());
        isRecording = true;
        ((MainActivity)getContext()).appView.startTime = System.currentTimeMillis();
    }

    public void stopRecording(){
        Log.i("Camera", "Recording stopped");
        Toast.makeText(getContext(), "Recording saved to "+recording.getPath(), Toast.LENGTH_LONG).show();
        try{
            if ((recorder != null) && isRecording){
                isRecording = false;
                recorder.stop();
                recorder.reset();
                recorder.release();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
            Log.i("Camera", "Max Duration Reached");
            mr.stop();
            mr.reset();
            mr.release();
            startRecording();
        }
    }
}
