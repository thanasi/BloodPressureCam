package org.thanasi.hirespulse;


import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import org.thanasi.hirespulse.R;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;

import android.widget.Chronometer;
import android.widget.Toast;

public class HRPCamActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "HiResPulse::CamActivity";

    private HRPCamView mOpenCvCameraView;
    private List<Size> mResolutionList;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
    
    
    private Mat mProfile;
    private Mat mProfileVals;
    private static final int MAXDATA = 30;
    private int mPVCounter = MAXDATA;
    
    private float[] mMeanVals;
    private Mat mTempMat;
    private Point mP1,mP2;
    private Scalar mColor;
    
    private int[] mDataShape; 
    
    private Chronometer mChrono;
    
//    private int DTYPE = CvType.CV_8U;
    private int DTYPE = CvType.CV_32F;
    
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.enableFpsMeter();                    
                    mMeanVals = new float[MAXDATA];
                	
                	Size resolution = mOpenCvCameraView.getResolution();
                	mDataShape = new int[2];
                	mDataShape[0] = resolution.height;
                	mDataShape[1] = MAXDATA;
                	
                    mProfile = new Mat(mDataShape[1],1,DTYPE);
                    mProfileVals = new Mat(mDataShape[0], mDataShape[1], DTYPE);
                    mTempMat = new Mat();
                	
                	mP1 = new Point(mDataShape[1]/7,0);
                	mP2 = new Point(5*mDataShape[1]/7,mDataShape[1]);
                	mColor = new Scalar(0,100,0);
                	
                	Log.i(TAG, "mDataShape " + Arrays.toString(mDataShape));
                    
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public HRPCamActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {


//    	Intent i = getIntent();
    	Log.i(TAG, "Successfully launched new intent!");
   
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.cameraview);
        
        mChrono = (Chronometer) findViewById(R.id.CamViewChronometer);

        mOpenCvCameraView = (HRPCamView) findViewById(R.id.hi_res_pulse_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        

        mOpenCvCameraView.setCvCameraViewListener(this);
        
//        mProfileVals = new String[MAXDATA];

        mPVCounter = 0;
        
        mChrono.start();
        
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    	mOpenCvCameraView.setFocusNear();
    }

    public void onCameraViewStopped() {
    	if (mProfile != null)
    		mProfile.release();
    	if (mProfileVals != null)
    		mProfileVals.release();
    	if (mTempMat != null)
    		mTempMat.release();
    	
    	mProfile = null;
    	mProfileVals = null;
    	mTempMat = null;
    	
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	
    	mTempMat = inputFrame.gray().colRange((int) mP1.x, (int) mP2.x); 
    	
    	Core.reduce(mTempMat, mProfile, 1, Core.REDUCE_AVG, DTYPE);
    	
//    	Log.i(TAG+"DUMP", mProfile.col(0).dump());
    	
    	if (mPVCounter<MAXDATA){
    		
    		if (mPVCounter > -1) {
    		for (int i = 0; i<mDataShape[0];i++)
    			mProfileVals.put(mPVCounter, i, mProfile.get(i,0));
    		mMeanVals[mPVCounter] = (float) Core.mean(mTempMat).val[0];
    		}
    		
    		mPVCounter++;
    	}
    	
    	else{
    		mChrono.stop();
    		
            Intent GraphIntent = new Intent(getApplicationContext(), PlotActivity.class);
            
            Log.i(TAG,"Switching to FinishedActivity");
            
            float [] data = new float[mProfileVals.height()*mProfileVals.width()];
            mProfileVals.get(0, 0, data);
            
//            Log.i(TAG,"data " + data.length);
//            Log.i(TAG,"data " + Arrays.toString(data));
            
            GraphIntent.putExtra("points",data);
            GraphIntent.putExtra("shape",mDataShape);
            startActivity(GraphIntent);
        	
            // cleanup data structures
            if (mProfile != null)
        		mProfile.release();
        	if (mProfileVals != null)
        		mProfileVals.release();
        	if (mTempMat != null)
        		mTempMat.release();
        	
        	mProfile = null;
        	mProfileVals = null;
        	mTempMat = null;
            
//            Log.i(TAG,"Did I make it here?");
    		
    		finish(); // close this activity
    	}
    	
    	mTempMat = inputFrame.rgba();

    	Core.rectangle(mTempMat, mP1, mP2, mColor, 3);
    	
        return mTempMat;
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        int idx = 0;
        while(resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
         }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item.getGroupId() == 2)
        {
            int id = item.getItemId();
            Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        }

        return true;
    }
    
}
