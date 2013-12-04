package org.thanasi.camgraph3;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.thanasi.camgraph3.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;

public class ImageManipulationsActivity extends Activity implements CvCameraViewListener2 {
    private static final String  TAG                 = "ThanasiCameraGraph::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;

    private Size                 mSizeRgba;
    
    private Mat                  mRgba;
    private Mat					 mTemp;
    private Mat                  mGray;
    private Mat					 mDynamics;
    private Mat					 mProfile;
//    private Scalar               mColorsRGB[];
//    private Point                mP1;
//    private Point                mP2;
//    private float                mBuff[];
    private Mat                  mRgbaInnerWindow;
    private Mat                  mGrayInnerWindow;
    private Mat                  mZoomWindow;
    private Mat                  mZoomCorner;
    
    private Mat					 mR1;
    private int 				 counter;
    private int					 MAXCOUNT = 60;
    private boolean				 saved = false;
    
    private Bitmap				 imgBitmap;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.enableFpsMeter();
                   
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ImageManipulationsActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.image_manipulations_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        
        
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
        mGray = new Mat();
        mDynamics = new Mat(MAXCOUNT, width, CvType.CV_32F);
        mRgba = new Mat();
        mTemp = new Mat();
        mProfile = new Mat();
//        mBuff = new float[width];
//        mColorsRGB = new Scalar [] {new Scalar(255,0,0),
//				        			new Scalar(0,255,0),
//				        			new Scalar(0,0,255)};
//        
//        mP1 = new Point();
//        mP2 = new Point();
        
        counter = 0;
     

    }

    private void CreateAuxiliaryMats() {
        if (mRgba.empty())
            return;

        mSizeRgba = mRgba.size();
        
//        int rows = (int) mSizeRgba.height;
//        int cols = (int) mSizeRgba.width;
        
    }

    public void onCameraViewStopped() {
        // Explicitly deallocate Mats
        if (mZoomWindow != null)
            mZoomWindow.release();
        if (mZoomCorner != null)
            mZoomCorner.release();
        if (mGrayInnerWindow != null)
            mGrayInnerWindow.release();
        if (mRgbaInnerWindow != null)
            mRgbaInnerWindow.release();
        if (mRgba != null)
            mRgba.release();
        if (mTemp != null)
        	mTemp.release();
        if (mGray != null)
            mGray.release();
        if (mDynamics != null)
        	mDynamics.release();
        if (mProfile != null)
            mProfile.release();
        
        if (mR1 != null)
            mR1.release();
//        if (mR2 != null)
//            mR2.release();
        
        mDynamics = null;
        mRgba = null;
        mGray = null;
        mProfile = null;
        mTemp = null;
        mRgbaInnerWindow = null;
        mGrayInnerWindow = null;
        mZoomCorner = null;
        mZoomWindow = null;
        
        mR1 = null;
        imgBitmap = null;

    }
    
    protected File getPhotoPath(String addName) {
        
        File dir=getPhotoDirectory();
        dir.mkdirs();

        return(new File(dir, getPhotoFilename(addName)));
		}
		
	protected File getPhotoDirectory() {
		return(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
		}
		
	protected String getPhotoFilename(String addName) {
		String ts=
		    new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		
		return(addName + "_" + ts + ".png");
		}

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if ((mSizeRgba == null) || (mRgba.cols() != mSizeRgba.width) || (mRgba.height() != mSizeRgba.height)
        		|| (mR1 == null))
            CreateAuxiliaryMats();
        
        // cycle through the 3 color channels
        	
        	// reduce the image to one row
    	Core.reduce(mGray, mProfile, 1, Core.REDUCE_AVG, CvType.CV_8UC1);

    	// debug data types and shapes
    	Log.i(TAG, "mProfile: " + mProfile.rows() + " " + mProfile.cols() + " " + mProfile.channels() + " " + mProfile.depth() + " " + mProfile.type());
    	Log.d(TAG, "mProfileDump: " + mProfile.dump());
//	    	Log.i(TAG, "mBuff: " + mBuff.length + " float");
	    	
//	    	mProfile.t().get(0, 0, mBuff);;
    	
//    	
//	        for(int h=0; h<mSizeRgba.height ; h++) {
//	            mP1.y = mP2.y = h;
//	            mP1.x = mSizeRgba.width;
//	            mP2.x = mSizeRgba.width - (int) mBuff[h]/2;
//	            Core.line(mRgba, mP1, mP2, mColorsRGB[0], 1);
//	        }
////    	}
//        
    	
//    	if (counter < MAXCOUNT) {
//    		Log.i(TAG, "counter: " + counter);
//    		mProfile.row(0).copyTo(mDynamics.row(counter));
//    		counter++;
//    	}
//    	
//    	if (counter >= MAXCOUNT && saved==false) {
//    		Log.i(TAG, "saving");
//    		File photo=getPhotoPath("Dynamics");
//
//    		 try {
//    			 imgBitmap = Bitmap.createBitmap(mProfile.cols(), mProfile.rows(), Bitmap.Config.ARGB_8888);
//    			 Utils.matToBitmap(mProfile, imgBitmap);
//                 FileOutputStream out = new FileOutputStream(photo.getPath());
//                 Log.d(TAG, "img saving");
//                 imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); //if you find that the image is poor in quality, adjust the "90" here to a different compression amount.
//                 Log.d(TAG, "img saved");
//                 out.close();
//                 saved = true;
//              } 
//              catch (Exception e) {
//            	  e.printStackTrace();
//              }
//    	}

    	
    	//        Core.line(mRgba, new Point(mDividerx,0), new Point (mDividerx,mSizeRgba.height), new Scalar(0,0,0), 3);
        return mGray;
    }
    
    
    
}
