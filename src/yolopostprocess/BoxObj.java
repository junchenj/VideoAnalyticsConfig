package yolopostprocess;

public class BoxObj {
	
	private double mMinX;
	private double mMinY;
	private double mMaxX;
	private double mMaxY;
	private String mLabel;
	private double mConfidence;

	public double getMinX() {return mMinX;}
	public double getMinY() {return mMinY;}
	public double getMaxX() {return mMaxX;}
	public double getMaxY() {return mMaxY;}
	public String getLabel() {return mLabel;}
	public double getConfidence(){return mConfidence;}
	
	public boolean equals(Object obj){
		if (!(obj instanceof BoxObj)) return false;
		BoxObj o = (BoxObj)obj;
		return (this.mMinX == o.mMinX &&
				this.mMinY == o.mMinY &&
				this.mMaxX == o.mMaxX &&
				this.mMaxY == o.mMaxY &&
				this.mLabel.equals(o.mLabel) &&
				this.mConfidence == o.mConfidence);
	}
	public int hashCode(){
		return this.toString().hashCode();
	}
	public String toString(){
		return mMinX+","+mMinY+","+mMaxX+","+mMaxY+
				","+mLabel+","+mConfidence;
	}
	
	public BoxObj clone(){
		BoxObj result = new Builder()
				.setMinX(mMinX)
				.setMinY(mMinY)
				.setMaxX(mMaxX)
				.setMaxY(mMaxY)
				.setLabel(mLabel)
				.setConfidence(mConfidence)
				.build();
		return result;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	static class Builder{
		private double mMinX;
		private double mMinY;
		private double mMaxX;
		private double mMaxY;
		private String mLabel;
		private double mConfidence = 1; // by default, 100%

		public Builder setMinX(double v){mMinX = v; return this;}
		public Builder setMinY(double v){mMinY = v; return this;}
		public Builder setMaxX(double v){mMaxX = v; return this;}
		public Builder setMaxY(double v){mMaxY = v; return this;}
		public Builder setLabel(String str){mLabel = str; return this;}
		public Builder setConfidence(double v){mConfidence = v; return this;}
		
		public BoxObj build(){
			BoxObj result = new BoxObj();
			result.mMinX = mMinX;
			result.mMinY = mMinY;
			result.mMaxX = mMaxX;
			result.mMaxY = mMaxY;
			result.mLabel = mLabel;
			result.mConfidence = mConfidence;
			return result;
		}
	}

}
