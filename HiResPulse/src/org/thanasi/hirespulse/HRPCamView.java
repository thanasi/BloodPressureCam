package org.thanasi.hirespulse;

import java.util.List;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
//import android.util.Log;

public class HRPCamView extends JavaCameraView {


    public HRPCamView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }
    
    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }
    
    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }
 
    public void setFlashTorch() {
    	Camera.Parameters params = mCamera.getParameters();
    	params.setFlashMode(Parameters.FLASH_MODE_TORCH);
    	mCamera.setParameters(params);
    }
    
    public void setFlashOff() {
    	Camera.Parameters params = mCamera.getParameters();
    	params.setFlashMode(Parameters.FLASH_MODE_OFF);
    	mCamera.setParameters(params);
    }  
    
    public void setFocusNear() {
    	Camera.Parameters params = mCamera.getParameters();
    	params.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
    	mCamera.setParameters(params);
    }

}