package org.thanasi.hirespulse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
//import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
//import android.view.View;
import android.widget.Button;

public class FinishedActivity extends Activity {

	private static final String TAG = "HiResPulse::FinishedActivity";
//	private String data;
	private float[] data;
	
	private Button mSaveData,mRecap;
//	private View mStartView;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Successfully launched new intent!");
		setContentView(R.layout.endview);
		
    	Intent i = getIntent();
    	Bundle extras = i.getExtras();
    	
    	data = extras.getFloatArray("points");
    	Log.i(TAG,"data " + data.length);
//    	data = extras.getString("data");
        
//		mStartView = (View) findViewById(R.id.EndScreenImage);
        mRecap = (Button) findViewById(R.id.RecaptureButton);
        mSaveData = (Button) findViewById(R.id.SaveDataButton);
        
//        Context context = this;
        
	 }
	
	
	@Override
	protected void onResume() {
		super.onResume();

		

        //Listening to button event
		mRecap = (Button) findViewById(R.id.RecaptureButton);
		mSaveData = (Button) findViewById(R.id.SaveDataButton);
		
        mRecap.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View arg0) {
                //Starting a new Intent
                Intent HRPCamIntent = new Intent(getApplicationContext(), HRPCamActivity.class);
  
                Log.i(TAG,"Switching to HRPCamActivity");
 
                startActivity(HRPCamIntent);
                
                finish();
            }
        });
        
        mSaveData.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View arg0) {
                //Starting a new Intent
            	
            	File dir = android.os.Environment.getExternalStorageDirectory().getAbsoluteFile();
            	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            	String currentDateandTime = sdf.format(new Date());
	    		File outfile = new File(dir,"log"+currentDateandTime+".txt");
	    		FileOutputStream f;
				try {
					Log.i(TAG, "attempting to save file "+ outfile);
					f = new FileOutputStream(outfile);
					PrintWriter pw = new PrintWriter(f);
//					pw.println(data);
					pw.print("[");
					for (int i=0; i<data.length-1;i++)
						pw.print(data[i] + ",");
					pw.print(data[data.length-1] + "]");
					pw.flush();
					pw.close();
					f.close();
					Log.i(TAG, "DATA HAS BEEN WRITTEN.");
	    		} catch (Exception e) {
	    		  e.printStackTrace();
	    		}
            	

            }
        });
	}
	
	 @Override
	 protected void onPause() {
		 super.onPause();

		 // don't do anything special for now
     }
	 
	 @Override
	 protected void onStop() {
		 super.onStop();

		 // don't do anything special for now
     }
	
}
