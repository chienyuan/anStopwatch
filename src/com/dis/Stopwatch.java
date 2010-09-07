package com.dis;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Stopwatch extends Activity {
	
	private TextView mStopwatchText;
	private TextView mTvSplitText;
	private TextView mTvLapList;
	private Button mBtnStart;
	private StopwatchThread mSwThread;
	private ArrayList<String> mLaps = new ArrayList<String>();
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // capture our View elements
        mStopwatchText = (TextView) findViewById(R.id.stopwatch_text);
        mBtnStart = (Button) findViewById(R.id.start);
        Button btnReset = (Button) findViewById(R.id.reset);
        
        mTvSplitText = (TextView) findViewById(R.id.split_text);
        Button btnSplit = (Button) findViewById(R.id.split);
        Button btnSplitReset = (Button) findViewById(R.id.split_reset);

        mTvLapList = (TextView) findViewById(R.id.lap_list);
        Button btnNewLap = (Button) findViewById(R.id.new_lap);
        Button btnLapReset = (Button) findViewById(R.id.lap_reset);

        // add a click listener to the button
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String txt = mBtnStart.getText().toString();
            	 if (txt.equals("Start") || txt.equals("Resume") ) {
            		// Start the clock!
            		mSwThread.go();
            		mBtnStart.setText("Stop");
            	} else if (txt.equals("Stop")) {
            		// Stop the clock!
            		mSwThread.noGo();
            		mBtnStart.setText("Resume");
            	}
            }
        });
        
        btnReset.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		mSwThread.reset();
        		mBtnStart.setText("Start");
        		mStopwatchText.setText("0.00.00");
        	}
        });
        
        btnSplit.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		mTvSplitText.setText(mStopwatchText.getText());
        	}
        });

        btnSplitReset.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		mTvSplitText.setText("");
        	}
        });
        
        btnNewLap.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		mLaps.add(mSwThread.getLap());
        		Iterator<String> it = mLaps.iterator();
        		StringBuilder sb = new StringBuilder();
        		while(it.hasNext()){
        			sb.append(it.next());
        		}
        		mTvLapList.setText(sb.toString());
        	}
        });
        
        btnLapReset.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		mSwThread.resetLap();
        		mLaps.clear();
        		mTvLapList.setText("");
        	}
        });
        
        mSwThread = new StopwatchThread(new Handler(){ 
        	public void handleMessage(Message msg){
        		mStopwatchText.setText(msg.getData().get("time").toString());
        	}
        });
        mSwThread.start();
	}
	
	
	
	private class StopwatchThread extends Thread {
		/** Whether or not stopwatch is running. */
		private boolean going = false;
		/** Stores elapsed milliseconds of previous runs. */
		private long prevElapsed = 0;
		/** Stores beginning time of this run. */
		private Date startDate = new Date();
		/** Current lap number. */
		private int lapNum = 0;
		/** Elapsed time at end of last lap. */
		private long lastLapTime = 0;
		
		private Handler handler;
		
		public StopwatchThread(Handler h){
			this.handler = h;
		}

		/** Returns elapsed time in milliseconds.
		 *@return The elapsed time
		 */
		private long elapsedTime() {
		    return prevElapsed +
			(going ? new Date().getTime() - startDate.getTime() : 0);
		}
		/** Changes the number of elapsed milliseconds into a string.
		 *@param time Number of elapsed milliseconds
		 *@return The elapsed time as a string.
		 */
		private String msToString(long time) {
		    String ms, sec, min;
		    if (time % 10 >= 5) //round to nearest hundredth
			time += 5;
		    ms = Long.toString(time % 1000);
		    while (ms.length() < 3)
			ms = "0" + ms;
		    ms = ms.substring(0, ms.length() - 1);
		    time /= 1000;
		    sec = Long.toString(time % 60);
		    if (sec.length() == 1) sec = "0" + sec;
		    time /= 60;
		    min = Long.toString(time);
		    return min + ":" + sec + "." + ms;
		}

		/** Called when the stopwatch is to go.
		 */
		public void go() {
		    startDate = new Date();
		    going = true;
		}
		/** Called when the stopwatch is to stop.
		 */
		public void noGo() {
		    prevElapsed = elapsedTime();
		    going = false;
		}
		/** Resets the stopwatch.
		 */
		public void reset() {
		    going = false;
		    prevElapsed = 0;
		    lastLapTime = 0;
		}
		/** Adds a lap to the list.
		 */
		public String getLap() {
		    long elapsed = elapsedTime();
		    String ret = Integer.toString(++lapNum)+ " - " +
				"Elapsed Time: " + msToString(elapsed) + " - " +
				"Lap Time: " + msToString(elapsed - lastLapTime) + "\n";
		    lastLapTime = elapsed;
		    return ret;
		}
		/** Resets the lap list.
		 */
		public void resetLap() {
		    lapNum = 0;
		    lastLapTime = 0;
		    
		}
		/** Main code of the thread.
		 */
		public void run() {
		    while (true) {
		    	Message msg = new Message();
		    	Bundle data = new Bundle();
		    	data.putString("time", msToString(elapsedTime()));
		    	msg.setData(data);
		    	handler.sendMessage(msg);
		    	yield();
		    }
		}
   }
}
