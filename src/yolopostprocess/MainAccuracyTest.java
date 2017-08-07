package yolopostprocess;

import java.util.*;

import analysis.*;

public class MainAccuracyTest {
	
	static int Type = 0;

	static int target = Category.VEHICLES_GENERAL;
//	static int target = Category.PEOPLE_GENERAL;
//	static int target = Category.VEHICLES_GENERAL;
	
	static String ResultFolder = Type == 0 ? 
			"/Users/junchenjiang/Documents/research/2017-summer/"
			+ "MSR-Video/yolo/junchen-data/result-ty0-multi-res-new/" :
//			+ "MSR-Video/yolo/junchen-data/result-ty0-multi-res-livestream/" :
			"/Users/junchenjiang/Documents/research/2017-summer/"
			+ "MSR-Video/yolo/junchen-data/result-ty1-multi-res-new/";
//			+ "MSR-Video/yolo/junchen-data/result-ty1-multi-res-livestream/";
	static String GroundTruthFile = Type == 0 ? 
			"/Users/junchenjiang/Downloads/"
			+ "object-detection-crowdai/labels.csv" :
			"/Users/junchenjiang/Downloads/"
			+ "object-dataset/labels.csv";
	static int OriginalResolution = 1920;
	static List<Integer> ResolutionList = Arrays.asList(
			1920
			, 1580, 1340, 1100, 960, 720, 
			480, 240, 120, 60
			);
	static List<Double> ThreshList = Arrays.asList(
			0.01, 0.05, 0.1, 0.25, 0.5, 0.8
			);
	
	public static void main(String[] args){
		Set<String> picIds = getPics();
//		picIds.remove("1478020237191578132");
//		picIds.remove("1478019984182279255");
//		picIds.remove("1479498419466839188");
		Map<String, Finding> groundTruthFindings = getGroundTruth(picIds);
		Map<String, YoloFinding> yoloFindings = getYoloFindings(picIds);
		Set<String> usefulPicIds = new HashSet<String>();
		for (String picId : picIds){
			if (!groundTruthFindings.containsKey(picId)) continue; 
			if (!yoloFindings.containsKey(picId)) continue;
			if (!isCompleteFindings(yoloFindings.get(picId))) continue;
			usefulPicIds.add(picId);
		}
		
		System.out.println("# of pics: "+usefulPicIds.size()+"\r\n");
		Map<String, Double> labelToCountGroundTruth = new HashMap<String, Double>();
		Map<String, Double> labelToCountYolo = new HashMap<String, Double>();
		for (String picId : usefulPicIds){
			Finding groundTruth = groundTruthFindings.get(picId);
			YoloFinding yoloFinding = yoloFindings.get(picId);
			
			Map<String, Double> map1 = groundTruth.getLabelToCount();
			for (String label : map1.keySet())
				Util.increamentKeyDouble(labelToCountGroundTruth, 
						label, map1.get(label));
			for (double resolution : ResolutionList){
				Knob knob = new Knob(resolution);
				Map<String, Double> map2 = yoloFinding.getFindingOfKnob(knob)
						.getLabelToCount(0.0);
				for (String label : map2.keySet())
					Util.increamentKeyDouble(labelToCountYolo, 
							label, map2.get(label));
			}
		}
		System.out.println(Util.keyRankSummary(labelToCountGroundTruth, 1000, false));
		System.out.println(Util.keyRankSummary(labelToCountYolo, 1000, false));
		
		PrecesionRecallAnalyze(usefulPicIds, groundTruthFindings, yoloFindings);
	}

	private static void PrecesionRecallAnalyze(
			Set<String> picIds,
			Map<String, Finding> groundTruthFindings,
			Map<String, YoloFinding> yoloFindings) {
		List<Knob> knobList = new ArrayList<Knob>();
		Map<Knob, Resource> knobToResource = new HashMap<Knob, Resource>();
		Map<Knob, Eval> knobToEval = new HashMap<Knob, Eval>();

		Map<Knob, Map<String, Resource>> knobToPicIdToResource = 
				new HashMap<Knob, Map<String, Resource>>();
		Map<Knob, Map<String, EvalStats>> knobToPicIdToEvalStats = 
				new HashMap<Knob, Map<String, EvalStats>>();
		
		for (int i = 0; i < ResolutionList.size(); i++)
			for (int j = 0; j < ThreshList.size(); j++)
				knobList.add(new Knob(ResolutionList.get(i), ThreshList.get(j)));
		
		for (String picId : picIds){
			for (Knob knobActual : knobList){
				double thresh = knobActual.thresh;
				double res = knobActual.resolution;
				Knob knobKey = new Knob(res);
				Resource resource = yoloFindings.get(picId).getResourceOfKnob(knobKey);
				Map<String, Resource> picIdToResource = 
						knobToPicIdToResource.containsKey(knobActual) ? 
								knobToPicIdToResource.get(knobActual) :
									new HashMap<String, Resource>();
				picIdToResource.put(picId, resource);
				knobToPicIdToResource.put(knobActual, picIdToResource);
				Finding groundTruth = groundTruthFindings.get(picId);
				Finding ourFinding = yoloFindings.get(picId).getFindingOfKnob(knobKey)
						.filterByThresh(thresh);
				Finding ourFindingResized = ourFinding.resizeByResolution(
						res, OriginalResolution);
//				Finding ourFindingResized = ourFinding;
				EvalStats evalStats = getEval(groundTruth, ourFindingResized, target);
				Eval eval = evalStats.toEval();
				Map<String, EvalStats> picIdToEvalStats = 
						knobToPicIdToEvalStats.containsKey(knobActual) ? 
								knobToPicIdToEvalStats.get(knobActual) :
									new HashMap<String, EvalStats>();
				picIdToEvalStats.put(picId, evalStats);
				knobToPicIdToEvalStats.put(knobActual, picIdToEvalStats);
			}
		}
		for (String picId : picIds){		
			for (int i = 0; i < ResolutionList.size(); i++){
				double thresh = 0.01;
				Knob knob = new Knob(ResolutionList.get(i), thresh);
				Eval eval = knobToPicIdToEvalStats.get(knob).get(picId).toEval();
				System.out.println("PicId="+picId+
						"\t"+knob.printSelf()+
						"\t"+eval.printSelf());
			}
			System.out.println();
		}
		
		for (Knob knob : knobList){
			Resource overallResource = Resource.Coalesce(knobToPicIdToResource.get(knob));
			Eval overallEval = EvalStats.Coalesce(knobToPicIdToEvalStats.get(knob))
					.toEval();
			knobToResource.put(knob, overallResource);
			knobToEval.put(knob, overallEval);
		}
		for (Knob knob : knobList){
			System.out.println(knob.printSelf()+
//					"\t"+actualKnobToResource.get(knobActual).printSelf()+
					"\t"+knobToEval.get(knob).printSelf());
		}
		
		String f = "#.###";
		{// fixing threshold, tuning resolution
			String outputfile = "/Users/junchenjiang/Documents/research/2017-summer/"
					+ "MSR-Video/yolo/junchen-data/result-plots/"
					+ "tune-res-fix-thresh-type-"+Type
					+ "-category-"+Category.getNameOfCategory(target)+".txt";
			Util.writeStringToFile(outputfile, "res\t"
					+ "thresh\t"
					+ "filesize\t"
					+ "var\t"
					+ "fscore\t"
					+ "var");
			double thresh = 0.1;
			for (int i = 0; i < ResolutionList.size(); i++){
				Knob knob = new Knob(ResolutionList.get(i), thresh);
				Eval eval = knobToEval.get(knob);
				Resource resource = knobToResource.get(knob);
				Util.appendNewlineToFile(outputfile, 
						knob.resolution+
						"\t"+knob.thresh+
						"\t"+Util.format(
								Util.getAve(resource.fileSizeList()),f)+
						"\t"+Util.format(
								Util.getStdErrOfMean(resource.fileSizeList()),f)+
						"\t"+Util.format(
								Util.getAve(eval.fScoreList()),f)+
						"\t"+Util.format(
								Util.getStdErrOfMean(eval.fScoreList()),f));
			}			
		}
		{// fixing resolution, tuning threshold
			String outputfile = "/Users/junchenjiang/Documents/research/2017-summer/"
					+ "MSR-Video/yolo/junchen-data/result-plots/"
					+ "tune-thresh-fix-res-type-"+Type
					+ "-category-"+Category.getNameOfCategory(target)+".txt";
			Util.writeStringToFile(outputfile, "res\t"
					+ "thresh\t"
					+ "filesize\t"
					+ "var\t"
					+ "fscore\t"
					+ "var");
			double resolution = ResolutionList.get(0);
			for (int i = 0; i < ThreshList.size(); i++){
				Knob knob = new Knob(resolution, ThreshList.get(i));
				Eval eval = knobToEval.get(knob);
				Resource resource = knobToResource.get(knob);
				Util.appendNewlineToFile(outputfile, 
						knob.resolution+
						"\t"+knob.thresh+
						"\t"+Util.format(
								Util.getAve(resource.fileSizeList()),f)+
						"\t"+Util.format(
								Util.getStdErrOfMean(resource.fileSizeList()),f)+
						"\t"+Util.format(
								Util.getAve(eval.fScoreList()),f)+
						"\t"+Util.format(
								Util.getStdErrOfMean(eval.fScoreList()),f));
			}			
		}
		{// scatter figure
			String outputfile = "/Users/junchenjiang/Documents/research/2017-summer/"
					+ "MSR-Video/yolo/junchen-data/result-plots/"
					+ "tune-thresh-res-type-"+Type
					+ "-category-"+Category.getNameOfCategory(target)+".txt";
			String outputfileBest = "/Users/junchenjiang/Documents/research/2017-summer/"
					+ "MSR-Video/yolo/junchen-data/result-plots/"
					+ "tune-thresh-res-bestcase-type-"+Type
					+ "-category-"+Category.getNameOfCategory(target)+".txt";
			String outputfileRandom = "/Users/junchenjiang/Documents/research/2017-summer/"
					+ "MSR-Video/yolo/junchen-data/result-plots/"
					+ "tune-thresh-res-randomcase-type-"+Type
					+ "-category-"+Category.getNameOfCategory(target)+".txt";
			String format = "res";
			for (int j = 0; j < ThreshList.size(); j++)
				format += "\t"+"thresh"+ThreshList.get(j)+"\t"
						+ "filesize\t"
						+ "var\t"
						+ "fscore\t"
						+ "var";
			Util.writeStringToFile(outputfile, "");
			Util.appendNewlineToFile(outputfile, format+"\r\n");
			for (int i = 0; i < ResolutionList.size(); i++){
				String line = ResolutionList.get(i)+"";
				for (int j = 0; j < ThreshList.size(); j++){
					Knob knob = new Knob(ResolutionList.get(i), ThreshList.get(j));
					Eval eval = knobToEval.get(knob);
					Resource resource = knobToResource.get(knob);
					line += "\t"+knob.thresh+
							"\t"+Util.format(
									Util.getAve(resource.fileSizeList()),f)+
							"\t"+Util.format(
									Util.getStdErrOfMean(resource.fileSizeList()),f)+
							"\t"+Util.format(
									Util.getAve(eval.fScoreList()),f)+
							"\t"+Util.format(
									Util.getStdErrOfMean(eval.fScoreList()),f);					
				}
				Util.appendNewlineToFile(outputfile, line);
			}
			Map<String, Knob> picIdToBestKnob = new HashMap<String, Knob>();
			Map<String, Resource> picIdToBestResource = new HashMap<String, Resource>();
			Map<String, Eval> picIdToBestEval = new HashMap<String, Eval>();
			Map<String, Knob> picIdToRandomKnob = new HashMap<String, Knob>();
			Map<String, Resource> picIdToRandomResource = new HashMap<String, Resource>();
			Map<String, Eval> picIdToRandomEval = new HashMap<String, Eval>();
			for (String picId : picIds){
				Map<Object, Double> knobToUtility = new HashMap<Object, Double>();
				for (Knob knob : knobList){
					Resource resource = knobToPicIdToResource.get(knob).get(picId);
					Eval eval = knobToPicIdToEvalStats.get(knob).get(picId).toEval();
					double fsize = Util.getAve(resource.fileSizeList());
					double fscore = Util.getAve(eval.fScoreList());
//					double utility = fsize * (1-fscore);
					double utility = fsize/250000.0+(1-fscore);
					// smaller the better
					knobToUtility.put(knob, utility);
				}
				Knob bestKnob = (Knob) 
						Util.orderKeyByValueAscentObject(knobToUtility).get(0);
				picIdToBestKnob.put(picId, bestKnob);
				picIdToBestResource.put(picId, 
						knobToPicIdToResource.get(bestKnob).get(picId));
				picIdToBestEval.put(picId, 
						knobToPicIdToEvalStats.get(bestKnob).get(picId).toEval());
				Knob randomKnob = knobList.get(new Random().nextInt(knobList.size()));
				picIdToRandomKnob.put(picId, randomKnob);
				picIdToRandomResource.put(picId, 
						knobToPicIdToResource.get(randomKnob).get(picId));
				picIdToRandomEval.put(picId, 
						knobToPicIdToEvalStats.get(randomKnob).get(picId).toEval());
			}
			Resource bestResource = Resource.Coalesce(picIdToBestResource);
			Eval bestEval = Eval.Coalesce(picIdToBestEval);
			Util.writeStringToFile(outputfileBest, 
					Util.format(Util.getAve(bestResource.fileSizeList()), f)+"\t"+
					Util.format(Util.getAve(bestEval.fScoreList()), f));
			Resource randomResource = Resource.Coalesce(picIdToRandomResource);
			Eval randomEval = Eval.Coalesce(picIdToRandomEval);
			Util.writeStringToFile(outputfileRandom, 
					Util.format(Util.getAve(randomResource.fileSizeList()), f)+"\t"+
					Util.format(Util.getAve(randomEval.fScoreList()), f));
		}
		
		{// confidence-accuracy correlation
			String outputfile = "/Users/junchenjiang/Documents/research/2017-summer/"
					+ "MSR-Video/yolo/junchen-data/result-plots/"
					+ "confidence-fscore-bucketized-type-"+Type
					+ "-category-"+Category.getNameOfCategory(target)+".csv";
			String kendallOutputfile = "/Users/junchenjiang/Documents/research/2017-summer/"
					+ "MSR-Video/yolo/junchen-data/result-plots/"
					+ "confidence-fscore-kendall-type-"+Type
					+ "-category-"+Category.getNameOfCategory(target)+".csv";
			List<Double> confidenceList = new ArrayList<Double>();
			List<Double> fscoreList = new ArrayList<Double>();
			List<Double> confidenceListTruePositive = new ArrayList<Double>();
			List<Double> confidenceListFalsePositive = new ArrayList<Double>();
			List<Double> kendallScoreList = new ArrayList<Double>();
			for (String picId : picIds){
				List<Double> localConfidences = new ArrayList<Double>();
				List<Double> localFscores = new ArrayList<Double>();
				for (double resolution : ResolutionList){
					Knob knob = new Knob(resolution, ThreshList.get(0));
					EvalStats evalStats = knobToPicIdToEvalStats.get(knob).get(picId);
					if (evalStats.numOfObjIdentified() == 0) continue;
					double confidence = evalStats.getAvgConfidence();
					double fscore = evalStats.getFscore();
					confidenceList.add(confidence);
					fscoreList.add(fscore);
					for (BoxObj obj : 
						evalStats.mObjIdentifiedToObjGroundTruth.keySet())
						confidenceListTruePositive.add(obj.getConfidence());
					for (BoxObj obj : evalStats.mObjsFalsePositive)
						confidenceListFalsePositive.add(obj.getConfidence());
					if (evalStats.getNumObjsIdentified() > 1){
						localConfidences.add(confidence);
						localFscores.add(fscore);						
					}
				}
				if (localConfidences.size() >= 2)
					kendallScoreList.add(Util.calcKendall2(
							localConfidences, localFscores));
			}
			System.out.println("AvgConfidence (True Positive)"
					+ " = "+Util.getAve(confidenceListTruePositive)
					+ " +/- "+Util.getStdDev(confidenceListTruePositive));
			System.out.println("AvgConfidence (False Positive)"
					+ " = "+Util.getAve(confidenceListFalsePositive)
					+ " +/- "+Util.getStdDev(confidenceListFalsePositive));
			Util.printToBucketizedCsv(confidenceList, fscoreList, 20, outputfile);
			Util.printToCdfCsv(kendallScoreList, kendallOutputfile);
		}
	}

	private static boolean isCompleteFindings(YoloFinding finding) {
		for (double resolution : ResolutionList)
			if (finding.getFindingOfKnob(new Knob(resolution)) == null)
				return false;
		return true;
	}

	private static EvalStats getEval(Finding gt, Finding our, int target) {
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
		
//		List<BoxObj> falseNegatives = new ArrayList<BoxObj>(); //from GT
//		List<BoxObj> truePositives = new ArrayList<BoxObj>(); //from Ours
//		List<BoxObj> falsePositives = new ArrayList<BoxObj>(); //from Ours
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

	private static boolean isSame(BoxObj o1, BoxObj o2) {
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

	private static Map<String, YoloFinding> getYoloFindings(Set<String> picIds) {
		Map<String, YoloFinding> result = new HashMap<String, YoloFinding>();
		List<String> fileNames = getResultFiles(ResultFolder);
		for (String fileName : fileNames){
			String picId = getIdFromFileName(fileName);
			if (!picIds.contains(picId)) continue;
			int pos1 = fileName.indexOf("-");
			int pos2 = fileName.indexOf(".");
			int resolution = Integer.valueOf(fileName.substring(pos1+4, pos2));
			Knob knob = new Knob(resolution);
			int fileSize = -1;
			{ //get file size
				String line = Util.readFileToLines(ResultFolder+fileName).get(0);
				//file size = 18212 bytes
				String str = line.substring("file size = ".length(), 
						line.indexOf(" bytes"));
				if (str.equals("") || str.equals(" ")) continue;
				fileSize = Integer.valueOf(str);
			}
			Resource resource = Resource.ofSingle(fileSize);
			Finding finding = parseYolo(picId, fileName);
			YoloFinding yoloFinding = result.containsKey(picId) ? 
					result.get(picId) : new YoloFinding();
			yoloFinding.insertFinding(knob, resource, finding);
			result.put(picId, yoloFinding);
		}
		return result;
	}

	private static Finding parseYolo(String picId, String fileName) {
		List<String> lines = Util.readFileToLines(ResultFolder+fileName);
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

	private static Map<String, Finding> getGroundTruth(Set<String> picIds) {
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

	private static Set<String> getPics() {
		Set<String> result = new HashSet<String>();
		List<String> fileNames = getResultFiles(ResultFolder);
		for (String fileName : fileNames){
			String picId = getIdFromFileName(fileName);
			result.add(picId);
		}
		return result;
	}
	
	private static List<String> getResultFiles(String str) {
		List<String> temp = Util.getListOfFilesFromFolder(str);
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < temp.size(); i++){
			if (!temp.get(i).endsWith(".txt")) continue;
			result.add(temp.get(i));
		}
		return result;
	}
	
	private static String getIdFromFileName(String str){
		// 1479498389464606786-res960.txt
		if (str.contains("/"))
			return str.substring(str.lastIndexOf("/"), str.lastIndexOf("-res"));
		else
			return str.substring(0, str.lastIndexOf("-res"));
	}
	
	private static class Knob{
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
	
	private static class Resource{
		private List<Double> mFileSizeList;
		public Resource(List<Double> list) {
			mFileSizeList = new ArrayList<Double>(list);
		}
		public List<Double> fileSizeList() {return mFileSizeList;}
		public static Resource ofSingle(double fileSize) {
			return new Resource(Arrays.asList(fileSize));
		}
		public String printSelf() {
			return "FileSize\t"+Util.format(Util.getAve(mFileSizeList), "#.##");
		}
		public static Resource Coalesce(List<Resource> itemList) {
			List<Double> fileSizeList = new ArrayList<Double>();
			for (Resource item : itemList)
				fileSizeList.addAll(item.mFileSizeList);
			return new Resource(fileSizeList);
		}
		public static Resource Coalesce(Map<String, Resource> keyToItem) {
			return Resource.Coalesce(new ArrayList<Resource>(
					keyToItem.values()));
		}
	}
	
	private static class EvalStats{
		Map<BoxObj, BoxObj> mObjIdentifiedToObjGroundTruth = 
				new HashMap<BoxObj, BoxObj>();
		List<BoxObj> mObjsFalsePositive = new ArrayList<BoxObj>();
		List<BoxObj> mObjsFalseNegative = new ArrayList<BoxObj>();

		public double numOfObjIdentified(){
			return mObjsFalsePositive.size()+mObjIdentifiedToObjGroundTruth.size();
		}
		public double getFscore() {
			return this.toEval().fScoreList().get(0);
		}
		public double getAvgConfidence() {
			List<Double> cfList = new ArrayList<Double>();
			for (BoxObj obj : mObjsFalsePositive)
				cfList.add(obj.getConfidence());
			for (BoxObj obj : mObjIdentifiedToObjGroundTruth.keySet())
				cfList.add(obj.getConfidence());
			return Util.getAve(cfList);
		}
		public double getNumObjsIdentified(){
			return mObjIdentifiedToObjGroundTruth.size()+mObjsFalsePositive.size();
		}
		public Eval toEval(){
			double falseNegativeCount = mObjsFalseNegative.size();
			double truePositiveCount = mObjIdentifiedToObjGroundTruth.size();
			double falsePositiveCount = mObjsFalsePositive.size();
			
			double Precision = truePositiveCount/
					(truePositiveCount+falsePositiveCount+Double.MIN_NORMAL);
			double Recall = truePositiveCount/
					(truePositiveCount+falseNegativeCount+Double.MIN_NORMAL);
			return Eval.ofSingle(Precision, Recall);
		}
		public void insertMatchedObj(BoxObj objIdentified, BoxObj objGroundTruth){
			mObjIdentifiedToObjGroundTruth.put(objIdentified, objGroundTruth);
		}
		public void insertFalsePositive(BoxObj obj){
			mObjsFalsePositive.add(obj);
		}
		public void insertFalseNegative(BoxObj obj){
			mObjsFalseNegative.add(obj);
		}
		public void addNewEvalStats(EvalStats other){
			this.mObjIdentifiedToObjGroundTruth.putAll(other.mObjIdentifiedToObjGroundTruth);
			this.mObjsFalsePositive.addAll(other.mObjsFalsePositive);
			this.mObjsFalseNegative.addAll(other.mObjsFalseNegative);
		}
		public static EvalStats Coalesce(List<EvalStats> itemList) {
			EvalStats result = new EvalStats();
			for (EvalStats item : itemList)
				result.addNewEvalStats(item);
			return result;
		}
		public static EvalStats Coalesce(Map<String, EvalStats> keyToItem) {
			return EvalStats.Coalesce(new ArrayList<EvalStats>(
					keyToItem.values()));
		}
	}
	
	private static class Eval{
		private List<Double> mPrecisionList;
		private List<Double> mRecallList;
		public Eval(List<Double> pList, List<Double> rList) {
			mPrecisionList = new ArrayList<Double>(pList);
			mRecallList = new ArrayList<Double>(rList);
		}
		public List<Double> predicisionList(){return mPrecisionList;}
		public List<Double> recallList(){return mRecallList;}
		public List<Double> fScoreList(){
			List<Double> result = new ArrayList<Double>();
			for (int i = 0; i < mPrecisionList.size(); i++){
				double p = mPrecisionList.get(i);
				double r = mRecallList.get(i);
				result.add(2*(p*r)/(p+r+Double.MIN_NORMAL));
			}
			return result;
		}
		public static Eval ofSingle(double p, double r) {
			return new Eval(Arrays.asList(p), Arrays.asList(r));
		}
		public static Eval Coalesce(List<Eval> itemList) {
			List<Double> precisionList = new ArrayList<Double>();
			List<Double> recallList = new ArrayList<Double>();
			for (Eval item : itemList){
				precisionList.addAll(item.mPrecisionList);
				recallList.addAll(item.mRecallList);
			}
			return new Eval(precisionList, recallList);
		}
		public static Eval Coalesce(Map<String, Eval> keyToItem) {
			return Eval.Coalesce(new ArrayList<Eval>(
					keyToItem.values()));
		}
		public String printSelf() {
			List<Double> fscores = fScoreList();
			return "Precision"
					+"\t"+Util.format(Util.getAve(mPrecisionList), "#.###")
					+"\t"+Util.format(Util.getStdErrOfMean(mPrecisionList), "#.###")
					+"\tRecall"
					+"\t"+Util.format(Util.getAve(mRecallList), "#.###")
					+"\t"+Util.format(Util.getStdErrOfMean(mRecallList), "#.###")
					+"\tF1"
					+"\t"+Util.format(Util.getAve(fscores), "#.###")
					+"\t"+Util.format(Util.getStdErrOfMean(fscores), "#.###");
		}
		
	}
	
	private static class YoloFinding{
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

}
