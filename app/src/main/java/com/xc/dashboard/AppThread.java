package com.xc.dashboard;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * Created by Aaron on 2016-05-23.
 */
public class AppThread extends Thread {
    boolean threadLoaded;
    AppView appView;
    private AppState appState;
    public enum AppState{LOADING, CAMERA, RECORD};

    public AppThread(AppView appView){
        this.appView = appView;
        threadLoaded = false;
        appState = AppState.LOADING;
    }

    public void run(){
        SurfaceHolder sh = appView.getHolder();
        Canvas c;
        while (!Thread.interrupted()){
            switch (appState){
                case LOADING:
                    //load
                    setAppState(AppState.CAMERA);
                    break;
                case CAMERA:
                case RECORD:
                    c = sh.lockCanvas(null);
                    try {
                        synchronized (sh) {
                            appView.draw(c);
                        }
                    } catch (Exception e) {
                    } finally {
                        if (c != null) {
                            sh.unlockCanvasAndPost(c);
                        }
                    }
                    // Set the approximate frame rate by setting this delay
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // Thread was interrupted while sleeping.
                        return;
                    }
                    break;
            }
        }
    }

    public void setAppState(AppState appState){
        this.appState = appState;
    }
    public AppState getAppState(){
        return appState;
    }
}
