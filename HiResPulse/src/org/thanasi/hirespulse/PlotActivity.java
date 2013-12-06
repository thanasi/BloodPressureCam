package org.thanasi.hirespulse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import org.thanasi.hirespulse.R;

/***********************************
 * @author David Buezas (david.buezas at gmail.com)
 * Feel free to copy, modify and use the source as it fits you.
 * 09/27/2012 nfellows - updated for 0.5.1 and made a few simplifications
 * 12/06/2013 thanasi - modified for my app
 * ***********************************/

public class PlotActivity extends Activity implements OnTouchListener {
    private static final String TAG = "HiResPulse::PlotActivity";
	
	private static final int SERIES_SIZE = 200;
    private XYPlot mySimpleXYPlot;
    private Button resetButton, mSaveDataButton, mNewDataButton;
    private SimpleXYSeries[] series = null;
    private PointF minXY;
    private PointF maxXY;
    
    private int 	[]		mShape;
    private float	[][] 	mData;
    private float	[]		mPoints;
    private Number	[]		mNPoints;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graphview);
        
    	Intent mI = getIntent();
    	Bundle extras = mI.getExtras();
    	
    	mPoints = extras.getFloatArray("points");
    	mShape = extras.getIntArray("shape");
        
    	mData = new float [mShape[0]][mShape[1]];
    	mNPoints = new Number [mShape[0] * mShape[1]];
    	int n = 0;
    	for (int i = 0; i<mShape[0]; i++) {
    		for (int j = 0; j<mShape[1]; j++) {
    			n = i * mShape[0] + j;
    			mData[i][j] = mPoints[n];
    			mNPoints[n] = mPoints[n];
    		}
    	}
        
        
        
        
        
        resetButton = (Button) findViewById(R.id.ResetButton);
    	mSaveDataButton = (Button) findViewById(R.id.SaveDataButton);
    	mNewDataButton = (Button) findViewById(R.id.NewDataButton);
        
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                minXY.x = series[0].getX(0).floatValue();
                maxXY.x = series[3].getX(series[3].size() - 1).floatValue();
                mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
                mySimpleXYPlot.redraw();
            }
        });
        
        mNewDataButton.setOnClickListener(new View.OnClickListener() {
        	 
            public void onClick(View arg0) {
                //Starting a new Intent
                Intent HRPCamIntent = new Intent(getApplicationContext(), HRPCamActivity.class);
                Log.i(TAG,"Switching to HRPCamActivity");
                startActivity(HRPCamIntent);
                finish();
            }
        });
        
        mSaveDataButton.setOnClickListener(new View.OnClickListener() {
        	 
            @SuppressLint("SimpleDateFormat")
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
					for (int i=0; i<mShape[0];i++){
						pw.print("[");
						for (int j=0; i<mShape[1]-1; j++)
							pw.print(mData[i][j] + ",");
						pw.print(mData[i][mShape[1]-1]+"]");
						if (i<mShape[0]-1)
							pw.print(",");
					}
					pw.print("]");
					pw.flush();
					pw.close();
					f.close();
					Log.i(TAG, "DATA HAS BEEN WRITTEN.");
	    		} catch (Exception e) {
	    		  e.printStackTrace();
	    		}
            	

            }
        });
        
        
        mySimpleXYPlot = (XYPlot) findViewById(R.id.myPlot);
        mySimpleXYPlot.setOnTouchListener(this);
        mySimpleXYPlot.getGraphWidget().setTicksPerRangeLabel(2);
        mySimpleXYPlot.getGraphWidget().setTicksPerDomainLabel(2);
        mySimpleXYPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        mySimpleXYPlot.getGraphWidget().setRangeValueFormat(
                new DecimalFormat("#####"));
        mySimpleXYPlot.getGraphWidget().setDomainValueFormat(
                new DecimalFormat("#####.#"));
        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(25);
        mySimpleXYPlot.setRangeLabel("");
        mySimpleXYPlot.setDomainLabel("");

        mySimpleXYPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        //mySimpleXYPlot.disableAllMarkup();
        series = new SimpleXYSeries[4];
        int scale = 1;
        for (int i = 0; i < 4; i++, scale *= 5) {
            series[i] = new SimpleXYSeries("S" + i);
            populateSeries(series[i], scale);
        }
        mySimpleXYPlot.addSeries(series[3],
                new LineAndPointFormatter(Color.rgb(50, 0, 0), null,
                        Color.rgb(100, 0, 0), null));
        mySimpleXYPlot.addSeries(series[2],
                new LineAndPointFormatter(Color.rgb(50, 50, 0), null,
                        Color.rgb(100, 100, 0), null));
        mySimpleXYPlot.addSeries(series[1],
                new LineAndPointFormatter(Color.rgb(0, 50, 0), null,
                        Color.rgb(0, 100, 0), null));
        mySimpleXYPlot.addSeries(series[0],
                new LineAndPointFormatter(Color.rgb(0, 0, 0), null,
                        Color.rgb(0, 0, 150), null));
        mySimpleXYPlot.redraw();
        mySimpleXYPlot.calculateMinMaxVals();
        minXY = new PointF(mySimpleXYPlot.getCalculatedMinX().floatValue(),
                mySimpleXYPlot.getCalculatedMinY().floatValue());
        maxXY = new PointF(mySimpleXYPlot.getCalculatedMaxX().floatValue(),
                mySimpleXYPlot.getCalculatedMaxY().floatValue());
    }

    private void populateSeries(SimpleXYSeries series, int max) {
        Random r = new Random();
        for(int i = 0; i < SERIES_SIZE; i++) {
            series.addLast(i, r.nextInt(max));
        }
    }

    // Definition of the touch states
    static final int NONE = 0;
    static final int ONE_FINGER_DRAG = 1;
    static final int TWO_FINGERS_DRAG = 2;
    int mode = NONE;

    PointF firstFinger;
    float distBetweenFingers;
    boolean stopThread = false;

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: // Start gesture
                firstFinger = new PointF(event.getX(), event.getY());
                mode = ONE_FINGER_DRAG;
                stopThread = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // second finger
                distBetweenFingers = spacing(event);
                // the distance check is done to avoid false alarms
                if (distBetweenFingers > 5f) {
                    mode = TWO_FINGERS_DRAG;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ONE_FINGER_DRAG) {
                    PointF oldFirstFinger = firstFinger;
                    firstFinger = new PointF(event.getX(), event.getY());
                    scroll(oldFirstFinger.x - firstFinger.x);
                    mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x,
                            BoundaryMode.FIXED);
                    mySimpleXYPlot.redraw();

                } else if (mode == TWO_FINGERS_DRAG) {
                    float oldDist = distBetweenFingers;
                    distBetweenFingers = spacing(event);
                    zoom(oldDist / distBetweenFingers);
                    mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x,
                            BoundaryMode.FIXED);
                    mySimpleXYPlot.redraw();
                }
                break;
        }
        return true;
    }

    private void zoom(float scale) {
        float domainSpan = maxXY.x - minXY.x;
        float domainMidPoint = maxXY.x - domainSpan / 2.0f;
        float offset = domainSpan * scale / 2.0f;

        minXY.x = domainMidPoint - offset;
        maxXY.x = domainMidPoint + offset;

        minXY.x = Math.min(minXY.x, series[3].getX(series[3].size() - 3)
                .floatValue());
        maxXY.x = Math.max(maxXY.x, series[0].getX(1).floatValue());
        clampToDomainBounds(domainSpan);
    }

    private void scroll(float pan) {
        float domainSpan = maxXY.x - minXY.x;
        float step = domainSpan / mySimpleXYPlot.getWidth();
        float offset = pan * step;
        minXY.x = minXY.x + offset;
        maxXY.x = maxXY.x + offset;
        clampToDomainBounds(domainSpan);
    }

    private void clampToDomainBounds(float domainSpan) {
        float leftBoundary = series[0].getX(0).floatValue();
        float rightBoundary = series[3].getX(series[3].size() - 1).floatValue();
        // enforce left scroll boundary:
        if (minXY.x < leftBoundary) {
            minXY.x = leftBoundary;
            maxXY.x = leftBoundary + domainSpan;
        } else if (maxXY.x > series[3].getX(series[3].size() - 1).floatValue()) {
            maxXY.x = rightBoundary;
            minXY.x = rightBoundary - domainSpan;
        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
}