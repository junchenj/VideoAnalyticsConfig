package yolopostprocess;

import java.util.*;

import analysis.Util;

public class YoloUtil {
	
	public static boolean isCompleteFindings(YoloFinding finding, 
			List<Integer> ResolutionList) {
		for (double resolution : ResolutionList)
			if (finding.getFindingOfKnob(new Knob(resolution)) == null)
				return false;
		return true;
	}

	public static EvalStats getEval(Finding gt, Finding our, int target) {
		EvalStats result = new EvalStats();
		
		List<BoxObj> gtObjs = gt.getBoxObjs();
		List<BoxObj> ourObjs = our.getBoxObjs();
		
		List<BoxObj> gtTargetObjs = new ArrayList<BoxObj>();
		List<BoxObj> ourTargetObjs = new ArrayList<BoxObj>();
		for (BoxObj obj : gtObjs)
			if (Category.IsLabelInTargetGroundTruth(obj.getLabel(), target))
				gtTargetObjs.add(obj);
		for (BoxObj obj : ourObjs)
			if (Category.IsLabelInTargetYolo(obj.getLabel(), target))
				ourTargetObjs.add(obj);
		
		Set<BoxObj> matchedGtObjs = new HashSet<BoxObj>();
		for (BoxObj ourObj : ourTargetObjs){
			boolean foundInGroundTruth = false;
			for (BoxObj gtObj : gtTargetObjs) 
				if (isSame(ourObj, gtObj)){
					foundInGroundTruth = true;
//					gtTargetObjs.remove(gtObj);
					matchedGtObjs.add(gtObj);
					result.insertMatchedObj(ourObj, gtObj);
					break;
				}
			if (!foundInGroundTruth)
				result.insertFalsePositive(ourObj);
		}
//		falseNegatives = gtTargetObjs;
		for (BoxObj obj : gtTargetObjs){
			if (!matchedGtObjs.contains(obj))
				result.insertFalseNegative(obj);
		}
		
		return result;
	}

	public static boolean isSame(BoxObj o1, BoxObj o2) {
		double overlapMinX = Double.max(o1.getMinX(), o2.getMinX());
		double overlapMinY = Double.max(o1.getMinY(), o2.getMinY());
		double overlapMaxX = Double.min(o1.getMaxX(), o2.getMaxX());
		double overlapMaxY = Double.min(o1.getMaxY(), o2.getMaxY());
		double overlapArea = 
				(overlapMaxX > overlapMinX ? overlapMaxX-overlapMinX : 0) *
				(overlapMaxY > overlapMinY ? overlapMaxY-overlapMinY : 0);
		double area1 = (o1.getMaxX()-o1.getMinX())*(o1.getMaxY()-o1.getMinY());
		double area2 = (o2.getMaxX()-o2.getMinX())*(o2.getMaxY()-o2.getMinY());
		double ratio = overlapArea/(area1+area2-overlapArea);
		if (overlapArea > 0)
			System.out.print("");
//		return ratio > 0.4;
		return ratio > 0;
	}

	public static Map<String, YoloFinding> getYoloFindings(Set<String> picIds, 
			String YoloResultFolder) {
		Map<String, YoloFinding> result = new HashMap<String, YoloFinding>();
		List<String> fileNames = getResultFiles(YoloResultFolder);
		for (String fileName : fileNames){
			String picId = getIdFromFileName(fileName);
			if (!picIds.contains(picId)) continue;
			int pos1 = fileName.indexOf("-");
			int pos2 = fileName.indexOf(".");
			int resolution = Integer.valueOf(fileName.substring(pos1+4, pos2));
			Knob knob = new Knob(resolution);
			int fileSize = -1;
			{ //get file size
				String line = Util.readFileToLines(YoloResultFolder+fileName).get(0);
				//file size = 18212 bytes
				String str = line.substring("file size = ".length(), 
						line.indexOf(" bytes"));
				if (str.equals("") || str.equals(" ")) continue;
				fileSize = Integer.valueOf(str);
			}
			Resource resource = Resource.ofSingle(fileSize);
			Finding finding = parseYolo(picId, fileName, YoloResultFolder);
			YoloFinding yoloFinding = result.containsKey(picId) ? 
					result.get(picId) : new YoloFinding();
			yoloFinding.insertFinding(knob, resource, finding);
			result.put(picId, yoloFinding);
		}
		return result;
	}

	public static Finding parseYolo(String picId, String fileName, 
			String YoloResultFolder) {
		List<String> lines = Util.readFileToLines(YoloResultFolder+fileName);
		Finding finding = Finding.builder().setPicId(picId).build();
		for (int i = 2; i < lines.size(); i += 2){
			String line1 = lines.get(i);
			// stop sign: 8%
			String line2 = lines.get(i+1);
			// 138, 503, 195, 562 
			String[] fields2 = line2.split(", ");
			
			double minX = Double.valueOf(fields2[0]);
			double minY = Double.valueOf(fields2[1]);
			double maxX = Double.valueOf(fields2[2]);
			double maxY = Double.valueOf(fields2[3]);
			String label = line1.substring(0, line1.indexOf(": "));
			double confidence = Double.valueOf(
					line1.substring(line1.indexOf(": ")+2, line1.length()-1))/100;
			finding.add(BoxObj.builder()
					.setMinX(minX)
					.setMinY(minY)
					.setMaxX(maxX)
					.setMaxY(maxY)
					.setLabel(label)
					.setConfidence(confidence)
					.build());
		}
		return finding;
	}

	public static Map<String, Finding> getGroundTruth(Set<String> picIds, 
			String GroundTruthFile, int Type) {
		Map<String, Finding> result = new HashMap<String, Finding>();
		List<String> lines = Util.readFileToLines(GroundTruthFile);
		for (int i = 0; i < lines.size(); i++){
			if (Type == 0){
				// 785,533,905,644,1479498371963069978.jpg,Car,
				// http://crowdai.com/images/Wwj-gorOCisE7uxA/visualize
				if (i == 0) continue; // omit the first line
				String[] fields = lines.get(i).split(",");
				String str = fields[4];
				String picId = str.substring(0, str.length()-4);
				if (!picIds.contains(picId)) continue;
				double minX = Double.valueOf(fields[0]);
				double minY = Double.valueOf(fields[1]);
				double maxX = Double.valueOf(fields[2]);
				double maxY = Double.valueOf(fields[3]);
				String label = fields[5];
				Finding finding = result.containsKey(picId) ? 
						result.get(picId) : 
							Finding.builder().setPicId(picId).build();
				finding.add(BoxObj.builder()
						.setMinX(minX)
						.setMinY(minY)
						.setMaxX(maxX)
						.setMaxY(maxY)
						.setLabel(label)
						.build());
				result.put(picId, finding);			
			} else {
				// 1478019952686311006.jpg 950 574 1004 620 0 "car"
				String[] fields = lines.get(i).split(" ");
				String str = fields[0];
				String picId = str.substring(0, str.length()-4);
				if (!picIds.contains(picId)) continue;
				double minX = Double.valueOf(fields[1]);
				double minY = Double.valueOf(fields[2]);
				double maxX = Double.valueOf(fields[3]);
				double maxY = Double.valueOf(fields[4]);
				String label = fields[6];
				Finding finding = result.containsKey(picId) ? 
						result.get(picId) : 
							Finding.builder().setPicId(picId).build();
				finding.add(BoxObj.builder()
						.setMinX(minX)
						.setMinY(minY)
						.setMaxX(maxX)
						.setMaxY(maxY)
						.setLabel(label)
						.build());
				result.put(picId, finding);	
			}
		}
		return result;
	}

	public static Set<String> getPics(String YoloResultFolder) {
		Set<String> result = new HashSet<String>();
		List<String> fileNames = getResultFiles(YoloResultFolder);
		for (String fileName : fileNames){
			String picId = getIdFromFileName(fileName);
			result.add(picId);
		}
		return result;
	}
	
	public static List<String> getResultFiles(String str) {
		List<String> temp = Util.getListOfFilesFromFolder(str);
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < temp.size(); i++){
			if (!temp.get(i).endsWith(".txt")) continue;
			result.add(temp.get(i));
		}
		return result;
	}
	
	public static String getIdFromFileName(String str){
		// 1479498389464606786-res960.txt
		if (str.contains("/"))
			return str.substring(str.lastIndexOf("/"), str.lastIndexOf("-res"));
		else
			return str.substring(0, str.lastIndexOf("-res"));
	}

}
