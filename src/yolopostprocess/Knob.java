package yolopostprocess;

import analysis.Util;

public class Knob{
	double resolution =1;
	double thresh = -1;
	public Knob(double r, double t) {
		resolution = r; thresh = t;
	}
	public String printSelf() {
		return "Res\t"+Util.format(resolution, "#.##")+"\t"+
				"Thresh\t"+Util.format(thresh, "#.##");
	}
	public Knob(double r) {resolution = r;}
	public boolean equals(Object obj) {
		if (!(obj instanceof Knob)) return false;
		Knob o = (Knob)obj;
		return (this.resolution == o.resolution &&
				this.thresh == o.thresh); 
	}
	public int hashCode(){
		return (resolution+","+thresh).hashCode();
	}
}
