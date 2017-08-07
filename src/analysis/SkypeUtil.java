package analysis;

import java.util.*;

public class SkypeUtil {

	public static final int METRIC_VEVO_BUFRATIO = 10;
	public static final int METRIC_VEVO_AVGBITRATE = 11;
	public static final int METRIC_VEVO_JOINTIME = 12;

	public static final int METRIC_NGC_RTT = 20;
	public static final int METRIC_NGC_LOSS = 21;
	public static final int METRIC_NGC_JITTER = 22;
	public static final int METRIC_NGC_DMOS = 23;

	public static final int METRIC_BING_RTT = 30;
	
	public static final int METRIC_VIACOM_BUFRATIO = 40;
	public static final int METRIC_VIACOM_AVGBITRATE = 41;
	public static final int METRIC_VIACOM_JOINTIME = 42;
	public static final int METRIC_VIACOM_BUFRATIO_BAD = 43;
	public static final int METRIC_VIACOM_AVGBITRATE_BAD = 44;
	public static final int METRIC_VIACOM_JOINTIME_BAD = 45;
	
	public static final int METRIC_SYNTHETIC = 100;
	
	public static String GetMetricName(int metricId){
		switch (metricId){
		case METRIC_VEVO_BUFRATIO:		return "METRIC_VEVO_BUFRATIO";
		case METRIC_VEVO_AVGBITRATE:	return "METRIC_VEVO_AVGBITRATE";
		case METRIC_VEVO_JOINTIME:		return "METRIC_VEVO_JOINTIME";
		
		case METRIC_NGC_RTT:					return "METRIC_NGC_RTT";
		case METRIC_NGC_LOSS:					return "METRIC_NGC_LOSS";
		case METRIC_NGC_JITTER:				return "METRIC_NGC_JITTER";
		case METRIC_NGC_DMOS:				return "METRIC_NGC_DMOS";
		
		case METRIC_BING_RTT:					return "METRIC_BING_RTT";
		
		case METRIC_SYNTHETIC:					return "METRIC_SYNTHETIC";

		case METRIC_VIACOM_BUFRATIO:		return "METRIC_VIACOM_BUFRATIO";
		case METRIC_VIACOM_AVGBITRATE:	return "METRIC_VIACOM_AVGBITRATE";
		case METRIC_VIACOM_JOINTIME:		return "METRIC_VIACOM_JOINTIME";
		case METRIC_VIACOM_BUFRATIO_BAD:		return "METRIC_VIACOM_BUFRATIO_BAD";
		case METRIC_VIACOM_AVGBITRATE_BAD:	return "METRIC_VIACOM_AVGBITRATE_BAD";
		case METRIC_VIACOM_JOINTIME_BAD:		return "METRIC_VIACOM_JOINTIME_BAD";
		default: throw new RuntimeException();
		}
	}
	
	public static String BestDecision(Map<String, Double> map, Config config){
		List<String> rank = Util.orderKeyByValueAscentDouble(map);
		if (LargerTheBetter(config.metricId())) return rank.get(rank.size()-1);
		else return rank.get(0);
	}
	public static List<String> BestDecisionList(Map<String, Double> map, Config config){
		List<String> rank = Util.orderKeyByValueAscentDouble(map);
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < rank.size(); i++){
			if (LargerTheBetter(config.metricId())) result.add(rank.get(rank.size()-1-i));
			else result.add(rank.get(i));
		}
		return result;
	}
	public static Object BestDecisionObject(Map<Object, Double> map, Config config){
		List<Object> rank = Util.orderKeyByValueAscentObject(map);
		if (LargerTheBetter(config.metricId())) return rank.get(rank.size()-1);
		else return rank.get(0);
	}
	public static List<Object> BestDecisionListObject(
			Map<Object, Double> map, Config config){
		List<Object> rank = Util.orderKeyByValueAscentObject(map);
		List<Object> result = new ArrayList<Object>();
		for (int i = 0; i < rank.size(); i++){
			if (LargerTheBetter(config.metricId())) result.add(rank.get(rank.size()-1-i));
			else result.add(rank.get(i));
		}
		return result;
	}

	public static boolean LargerTheBetter(int metricId) {
		if (metricId == SkypeUtil.METRIC_VEVO_AVGBITRATE) return true;
		if (metricId == SkypeUtil.METRIC_SYNTHETIC) return true;
		else return false;
	}

	public static double GetScaleOfUcb(int metricId) {
		switch (metricId){
		case METRIC_VEVO_BUFRATIO:		return 0.01;
		case METRIC_VEVO_AVGBITRATE:	return 4000;
		case METRIC_VEVO_JOINTIME:		return 8000;
		
		case METRIC_NGC_RTT:					return 150;
		case METRIC_NGC_LOSS:					return 0.01;
		case METRIC_NGC_JITTER:				return 15;
		case METRIC_NGC_DMOS:				return 0.3;
		
		case METRIC_BING_RTT:					return 100;
		default: throw new RuntimeException();
		}
	}
}
