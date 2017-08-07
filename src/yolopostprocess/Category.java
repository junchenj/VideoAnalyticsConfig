package yolopostprocess;

import java.util.*;

public class Category {
	
	public final static int VEHICLES_GENERAL = 1;
	private static Set<String> mVehicleLabelsGT = 
			new HashSet<String>(Arrays.asList(
					"\"car\"", "Car",
					"\"truck\"", "Truck"
					));
	private static Set<String> mVehicleLabelsYolo = 
			new HashSet<String>(Arrays.asList(
					"car",
					"bus",
					"truck",
					"train"
					));
	
	public final static int PEOPLE_GENERAL = 2;
	private static Set<String> mPeopleLabelsGT = 
			new HashSet<String>(Arrays.asList(
					"\"pedestrian\"","\"biker\"", "Pedestrian"
					));
	private static Set<String> mPeopleLabelsYolo = 
			new HashSet<String>(Arrays.asList(
					"person"
					));
	
	public final static int SIGNS_GENERAL = 3;
	private static Set<String> mSignLabelsGT = 
			new HashSet<String>(Arrays.asList(
					"\"trafficLight\""
					));
	private static Set<String> mSignLabelsYolo = 
			new HashSet<String>(Arrays.asList(
					"traffic light"
					));

	public static String getNameOfCategory(int targetCategory){
		switch (targetCategory){
		case VEHICLES_GENERAL:
			return "Vehicles";
		case PEOPLE_GENERAL:
			return "People";
		case SIGNS_GENERAL:
			return "Signs";
		default: return "Others";
		}
	}
	
	public static boolean IsLabelInTargetGroundTruth(
			String label, int targetCategory){
		switch (targetCategory){
		case VEHICLES_GENERAL:
			return mVehicleLabelsGT.contains(label);
		case PEOPLE_GENERAL:
			return mPeopleLabelsGT.contains(label);
		case SIGNS_GENERAL:
			return mSignLabelsGT.contains(label);
		default: return false;
		}
	}
	public static boolean IsLabelInTargetYolo(
			String label, int targetCategory){
		switch (targetCategory){
		case VEHICLES_GENERAL:
			return mVehicleLabelsYolo.contains(label);
		case PEOPLE_GENERAL:
			return mPeopleLabelsYolo.contains(label);
		case SIGNS_GENERAL:
			return mSignLabelsYolo.contains(label);
		default: return false;
		}
	}
}
