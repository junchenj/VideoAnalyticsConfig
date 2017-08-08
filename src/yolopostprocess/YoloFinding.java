package yolopostprocess;

import java.util.*;

public class YoloFinding{
	private List<Knob> mKnobs = new ArrayList<Knob>();
	private List<Resource> mResources = new ArrayList<Resource>();
	private List<Finding> mFindings = new ArrayList<Finding>();
	public void insertFinding(Knob knob, Resource resource, Finding finding){
		mKnobs.add(knob); mResources.add(resource);
		mFindings.add(finding);
	}
	public Resource getResourceOfKnob(Knob knob){
		int i = getIndexOfKnob(knob);
		return (i >= 0 ? mResources.get(i) : null);
	}
	public Finding getFindingOfKnob(Knob knob){
		int i = getIndexOfKnob(knob);
		return (i >= 0 ? mFindings.get(i) : null );
	}
	private int getIndexOfKnob(Knob knob) {
		for (int i = 0; i < mKnobs.size(); i++)
			if (mKnobs.get(i).equals(knob)) return i;
		return -1;
	}
}
