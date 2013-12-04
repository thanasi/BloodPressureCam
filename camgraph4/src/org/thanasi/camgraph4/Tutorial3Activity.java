package org.thanasi.camgraph4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import org.thanasi.camgraph4.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

public class Tutorial3Activity extends Activity implements CvCameraViewListener2, OnTouchListener {
    private static final String TAG = "OCVSample::Activity";

    private Tutorial3View mOpenCvCameraView;
    private List<Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
    
    private Mat mProfile;
//    private String[] mProfileVals;
    private Mat mProfileVals;
    private boolean mSaved = true;
    private static final int MAXDATA = 300;
    private int mPVCounter = MAXDATA;
    private int mSavedCounter = 0;
    
    private Mat mTempMat;
    private Point mP1,mP2;
    private Scalar mColor;
    
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
                    mOpenCvCameraView.setOnTouchListener(Tutorial3Activity.this);
                    mProfile = new Mat(720,1,DTYPE);
                    mProfileVals = new Mat(MAXDATA, 720, DTYPE);
                    mTempMat = new Mat();
                    
                	mP1 = new Point(100,0);
                	mP2 = new Point(600,720);
                	mColor = new Scalar(0,100,0);
                    
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Tutorial3Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial3_surface_view);

        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        
//        mProfileVals = new String[MAXDATA];

        mPVCounter = 0;
        mSaved = false;
        
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
//    	mOpenCvCameraView.setFlashTorch();
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
    
    // TODO: MAKE ME WORK!

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	
    	mTempMat = inputFrame.gray().colRange(100, 600); 
    	
    	Core.reduce(mTempMat, mProfile, 1, Core.REDUCE_AVG, DTYPE);
//    	inputFrame.gray().row(0).copyTo(mProfile);
    	
//    	Log.i(TAG+"DUMP", mProfile.col(0).dump());
    	
    	if (mPVCounter<MAXDATA){
    		for (int i = 0; i<720;i++)
    			mProfileVals.put(mPVCounter, i, mProfile.get(i,0));
//    		mProfileVals.push_back(mProfile.t());
//    		Mat a = mProfileVals;
//    		mProfileVals = mProfileVals.setTo(mProfile.t(), mask);
//    		a.release();
//    		Log.i(TAG+"DUMP", mProfileVals.row(mPVCounter).dump());
    		
//    		mProfileVals[mPVCounter] = mProfile.dump();
//    		Log.i(TAG, "added string of length " + mProfileVals[mPVCounter].length());
    		mPVCounter++;    		
    	}
    	
    	else{
    		if (mSaved == false) {
	    		
	    		File dir = android.os.Environment.getExternalStorageDirectory().getAbsoluteFile();
	    		File outfile = new File(dir,"log"+mSavedCounter+".txt");
	    		FileOutputStream f;
				try {
					Log.i(TAG, "attempting to save file "+ outfile);
					f = new FileOutputStream(outfile);
					PrintWriter pw = new PrintWriter(f);
	    			
//					for (int i=0;i<MAXDATA;i++)
//						pw.println(mProfileVals[i].);
					pw.println(mProfileVals.dump());
					pw.flush();
					pw.close();
					f.close();
					mSaved = true;
					Log.i(TAG, "DATA HAS BEEN WRITTEN.");
//					Toast.makeText(this, "wrote file " + outfile, Toast.LENGTH_SHORT).show();
	    		} catch (Exception e) {
	    		  e.printStackTrace();
	    		}
    		}
    		
    	}
    	
    	mTempMat = inputFrame.rgba();

    	Core.rectangle(mTempMat, mP1, mP2, mColor, 3);
    	
        return mTempMat;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        List<String> effects = mOpenCvCameraView.getEffectList();

        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }

        mColorEffectsMenu = menu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effects.size()];

        int idx = 0;
        ListIterator<String> effectItr = effects.listIterator();
        while(effectItr.hasNext()) {
           String element = effectItr.next();
           mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
           idx++;
        }

        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        idx = 0;
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
        if (item.getGroupId() == 1)
        {
            mOpenCvCameraView.setEffect((String) item.getTitle());
            Toast.makeText(this, mOpenCvCameraView.getEffect(), Toast.LENGTH_SHORT).show();
        }
        else if (item.getGroupId() == 2)
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

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG,"onTouch event");
        // restart data logging on touch
        if (mSaved == true) {
        	mPVCounter = 0;
        	mSaved = false;
        	mSavedCounter += 1;
        	Toast.makeText(this, "begin logging file #" + mSavedCounter, Toast.LENGTH_SHORT).show();
        }
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//        String currentDateandTime = sdf.format(new Date());
//        String fileName = Environment.getExternalStorageDirectory().getPath() +
//                               "/sample_picture_" + currentDateandTime + ".jpg";
//        mOpenCvCameraView.takePicture(fileName);
//        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
        return false;
    }
}
