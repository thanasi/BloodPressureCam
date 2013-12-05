package org.thanasi.hirespulse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
//import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private static final String TAG = "HiResPulse::MainActivity";
	
	private Button mSSNext;
//	private View mStartView;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		setContentView(R.layout.startscreen);
        
//		mStartView = (View) findViewById(R.id.StartScreenImage);
        mSSNext = (Button) findViewById(R.id.StartScreenNextButton);
        
//        Context context = this;
        
	 }
	
	
	@Override
	protected void onResume() {
		super.onResume();


        //Listening to button event
		mSSNext = (Button) findViewById(R.id.StartScreenNextButton);
		
        mSSNext.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View arg0) {
                //Starting a new Intent
                Intent HRPCamIntent = new Intent(getApplicationContext(), HRPCamActivity.class);
  
                Log.i(TAG,"Switching to HRPCamActivity");
 
                startActivity(HRPCamIntent);
                
                finish();
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
