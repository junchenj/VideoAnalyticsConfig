package analysis;

import java.io.Serializable;
import java.util.*;

public class Config implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9194206646450859110L;

	private int mMetricId = SkypeUtil.METRIC_VEVO_JOINTIME;
	public void setMetricId(int v){mMetricId = v;}
	public int metricId(){return mMetricId;}
	
	private Timer mTimer = new Timer();
	public Timer timer(){return mTimer;}
	
	private int mMinReliableSamples;
	public void setMinReliableSample(int v){mMinReliableSamples = v;};
	public int minReliableSamples(){return mMinReliableSamples;}
	
//	private int mEpochLenSeconds;
//	public void setEpochLenSeconds(int v){mEpochLenSeconds = v;};
//	public int epochLenSeconds(){return mEpochLenSeconds;}
//	
//	private int mGroupType = -1;
//	public void setGroupType(int v){mGroupType = v;};
//	public int groupType(){return mGroupType;}

	public String temp_logfile = "";
	
	
}
