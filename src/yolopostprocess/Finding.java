package yolopostprocess;

import java.util.*;
import analysis.*;

public class Finding {
	
	private List<BoxObj> mBoxItems = new ArrayList<BoxObj>();
	private String mPicId;

	public Map<String, Double> getLabelToCount() {
		Map<String, Double> result = new HashMap<String, Double>();
		for (BoxObj obj : mBoxItems)
			Util.increamentKeyDouble(result, obj.getLabel(), 1);
		return result;
	}
	
	public Map<String, Double> getLabelToCount(double thresh) {
		Map<String, Double> result = new HashMap<String, Double>();
		for (BoxObj obj : mBoxItems)
			if (obj.getConfidence() > thresh)
				Util.increamentKeyDouble(result, obj.getLabel(), 1);
		return result;
	}

	public List<BoxObj> getBoxObjs() {
		List<BoxObj> result = new ArrayList<BoxObj>();
		for (BoxObj obj : mBoxItems) result.add(obj);
		return result;
	}
	public List<BoxObj> getBoxObjs(double thresh) {
		List<BoxObj> result = new ArrayList<BoxObj>();
		for (BoxObj obj : mBoxItems)
			if (obj.getConfidence() > thresh)
				result.add(obj);
		return result;
	}
	
	public void add(BoxObj obj) {
		mBoxItems.add(obj);
	}

	public Finding filterByThresh(double thresh) {
		Finding result = new Builder().setPicId(mPicId).build();
		for (BoxObj obj : mBoxItems)
			if (obj.getConfidence() > thresh) result.add(obj);
		return result;
	}

	public Finding resizeByResolution(double res, double originalRes) {
		Finding result = new Builder().setPicId(mPicId).build();
		for (BoxObj obj : mBoxItems){
			double rate = originalRes/res;
			BoxObj newObj = new BoxObj.Builder()
					.setMinX(obj.getMinX()*rate)
					.setMinY(obj.getMinY()*rate)
					.setMaxX(obj.getMaxX()*rate)
					.setMaxY(obj.getMaxY()*rate)
					.setLabel(obj.getLabel())
					.setConfidence(obj.getConfidence())
					.build();
			result.add(newObj);
		}
		return result;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	static class Builder{
		private String mPicId;

		public Builder setPicId(String str)
		{mPicId = str; return this;}
		
		public Finding build(){
			Finding result = new Finding();
			result.mPicId = mPicId;
			return result;
		}
	}

}
