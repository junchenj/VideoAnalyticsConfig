package yolopostprocess;

import java.util.*;

import analysis.*;

public class MainAccuracyTest {
	
	static int Type = 0;

	static int target = Category.VEHICLES_GENERAL;
//	static int target = Category.PEOPLE_GENERAL;
//	static int target = Category.VEHICLES_GENERAL;
	
	static String YoloResultFolder = Type == 0 ? 
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
	static String AnalysisResultFolder = 
//			"/Users/junchenjiang/Documents/research/2017-summer/"
//			+ "MSR-Video/yolo/junchen-data/result-plots/";
			"/Users/junchenjiang/Documents/workspace-msr/VideoAnalyticsConfig/"
			+ "ResultPlots/";
	static int OriginalResolution = 1920;
	static List<Integer> ResolutionList = Arrays.asList(
//			1920
//			, 1580, 1340, 1100, 960, 720, 
			/* hide higher resolutions since Yolo resizes
			 * images to 448x448*/
			1920, 480, 240, 120, 60
			);
	static List<Double> ThreshList = Arrays.asList(
			0.01, 0.05, 0.1, 0.25, 0.5, 0.8
			);
	
	public static void main(String[] args){
		Set<String> picIds = YoloUtil.getPics(YoloResultFolder);
//		picIds.remove("1478020237191578132");
//		picIds.remove("1478019984182279255");
//		picIds.remove("1479498419466839188");
		Map<String, Finding> groundTruthFindings = YoloUtil.getGroundTruth(picIds, 
				GroundTruthFile, Type);
		Map<String, YoloUtil.YoloFinding> yoloFindings = YoloUtil.getYoloFindings(
				picIds, YoloResultFolder);
		Set<String> usefulPicIds = new HashSet<String>();
		for (String picId : picIds){
			if (!groundTruthFindings.containsKey(picId)) continue; 
			if (!yoloFindings.containsKey(picId)) continue;
			if (!YoloUtil.isCompleteFindings(yoloFindings.get(picId), ResolutionList)) 
				continue;
			usefulPicIds.add(picId);
		}
		
		System.out.println("# of pics: "+usefulPicIds.size()+"\r\n");
		Map<String, Double> labelToCountGroundTruth = new HashMap<String, Double>();
		Map<String, Double> labelToCountYolo = new HashMap<String, Double>();
		for (String picId : usefulPicIds){
			Finding groundTruth = groundTruthFindings.get(picId);
			YoloUtil.YoloFinding yoloFinding = yoloFindings.get(picId);
			
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
			Map<String, YoloUtil.YoloFinding> yoloFindings) {
		List<Knob> knobList = new ArrayList<Knob>();
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
				EvalStats evalStats = YoloUtil.getEval(
						groundTruth, ourFindingResized, target);
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
				EvalStats evalStats = knobToPicIdToEvalStats.get(knob).get(picId);
				System.out.println("PicId="+picId+
						"\t"+knob.printSelf()+
						"\t"+evalStats.printSelf());
			}
			System.out.println();
		}
		
		String f = "#.###";
		{// fixing threshold, tuning resolution
			String outputfile = AnalysisResultFolder
					+ "tune-res-fix-thresh-type-"+Type
					+ "-category-"+Category.getNameOfCategory(target)+".txt";
			Util.writeStringToFile(outputfile, "res\t"
					+ "thresh\t"
					+ "filesize\t"
					+ "var\t"
					+ "fscore\t"
					+ "var" + "\r\n");
			double thresh = 0.1;
			for (int i = 0; i < ResolutionList.size(); i++){
				Knob knob = new Knob(ResolutionList.get(i), thresh);
				Map<String, EvalStats> picIdToEvalStats = 
						knobToPicIdToEvalStats.get(knob);
				Map<String, Resource> picIdToResource = 
						knobToPicIdToResource.get(knob);
				Util.appendNewlineToFile(outputfile, 
						knob.resolution+
						"\t"+knob.thresh+
						"\t"+Util.format(
								Util.getAve(Resource.GetFileSizeList(
										picIdToResource)),f)+
						"\t"+Util.format(
								Util.getStdErrOfMean(Resource.GetFileSizeList(
										picIdToResource)),f)+
						"\t"+Util.format(
								Util.getAve(EvalStats.GetFScoreList(
										picIdToEvalStats)),f)+
						"\t"+Util.format(
								Util.getStdErrOfMean(EvalStats.GetFScoreList(
										picIdToEvalStats)),f));
			}			
		}
		{// fixing resolution, tuning threshold
			String outputfile = AnalysisResultFolder
					+ "tune-thresh-fix-res-type-"+Type
					+ "-category-"+Category.getNameOfCategory(target)+".txt";
			Util.writeStringToFile(outputfile, "res\t"
					+ "thresh\t"
					+ "filesize\t"
					+ "var\t"
					+ "fscore\t"
					+ "var" + "\r\n");
			double resolution = ResolutionList.get(0);
			for (int i = 0; i < ThreshList.size(); i++){
				Knob knob = new Knob(resolution, ThreshList.get(i));
				Map<String, EvalStats> picIdToEvalStats = 
						knobToPicIdToEvalStats.get(knob);
				Map<String, Resource> picIdToResource = 
						knobToPicIdToResource.get(knob);
				Util.appendNewlineToFile(outputfile, 
						knob.resolution+
						"\t"+knob.thresh+
						"\t"+Util.format(
								Util.getAve(Resource.GetFileSizeList(
										picIdToResource)),f)+
						"\t"+Util.format(
								Util.getStdErrOfMean(Resource.GetFileSizeList(
										picIdToResource)),f)+
						"\t"+Util.format(
								Util.getAve(EvalStats.GetFScoreList(
										picIdToEvalStats)),f)+
						"\t"+Util.format(
								Util.getStdErrOfMean(EvalStats.GetFScoreList(
										picIdToEvalStats)),f));
			}
		}
		{// scatter figure
			String outputfile = AnalysisResultFolder
					+ "tune-thresh-res-type-"+Type
					+ "-category-"+Category.getNameOfCategory(target)+".txt";
			String outputfileBest = AnalysisResultFolder
					+ "tune-thresh-res-bestcase-type-"+Type
					+ "-category-"+Category.getNameOfCategory(target)+".txt";
			String outputfileRandom = AnalysisResultFolder
					+ "tune-thresh-res-randomcase-type-"+Type
					+ "-category-"+Category.getNameOfCategory(target)+".txt";
			String format = "res";
			for (int j = 0; j < ThreshList.size(); j++)
				format += "\t"+"thresh"+ThreshList.get(j)+"\t"
						+ "filesize\t"
						+ "var\t"
						+ "fscore\t"
						+ "var" + "\r\n";
			Util.writeStringToFile(outputfile, "");
			Util.appendNewlineToFile(outputfile, format+"\r\n");
			for (int i = 0; i < ResolutionList.size(); i++){
				String line = ResolutionList.get(i)+"";
				for (int j = 0; j < ThreshList.size(); j++){
					Knob knob = new Knob(ResolutionList.get(i), ThreshList.get(j));
					Map<String, EvalStats> picIdToEvalStats = 
							knobToPicIdToEvalStats.get(knob);
					Map<String, Resource> picIdToResource = 
							knobToPicIdToResource.get(knob);
					line += "\t"+knob.thresh+
							"\t"+Util.format(
									Util.getAve(Resource.GetFileSizeList(
											picIdToResource)),f)+
							"\t"+Util.format(
									Util.getStdErrOfMean(Resource.GetFileSizeList(
											picIdToResource)),f)+
							"\t"+Util.format(
									Util.getAve(EvalStats.GetFScoreList(
											picIdToEvalStats)),f)+
							"\t"+Util.format(
									Util.getStdErrOfMean(EvalStats.GetFScoreList(
											picIdToEvalStats)),f);
				}
				Util.appendNewlineToFile(outputfile, line);
			}
			Map<String, Knob> picIdToBestKnob = new HashMap<String, Knob>();
			Map<String, Resource> picIdToBestResource = new HashMap<String, Resource>();
			Map<String, EvalStats> picIdToBestEvalStats = new HashMap<String, EvalStats>();
			Map<String, Knob> picIdToRandomKnob = new HashMap<String, Knob>();
			Map<String, Resource> picIdToRandomResource = new HashMap<String, Resource>();
			Map<String, EvalStats> picIdToRandomEvalStats = new HashMap<String, EvalStats>();
			for (String picId : picIds){
				Map<Object, Double> knobToUtility = new HashMap<Object, Double>();
				for (Knob knob : knobList){
					Resource resource = knobToPicIdToResource.get(knob).get(picId);
					EvalStats evalStats = knobToPicIdToEvalStats.get(knob).get(picId);
					double fsize = Util.getAve(resource.fileSizeList());
					double fscore = evalStats.getFScore();
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
				picIdToBestEvalStats.put(picId, 
						knobToPicIdToEvalStats.get(bestKnob).get(picId));
				Knob randomKnob = knobList.get(new Random().nextInt(knobList.size()));
				picIdToRandomKnob.put(picId, randomKnob);
				picIdToRandomResource.put(picId, 
						knobToPicIdToResource.get(randomKnob).get(picId));
				picIdToRandomEvalStats.put(picId, 
						knobToPicIdToEvalStats.get(randomKnob).get(picId));
			}
			Resource bestResource = Resource.Coalesce(picIdToBestResource);
			EvalStats bestEvalStats = EvalStats.Coalesce(picIdToBestEvalStats);
			Util.writeStringToFile(outputfileBest, 
					Util.format(Util.getAve(bestResource.fileSizeList()), f)+"\t"+
					Util.format(bestEvalStats.getFScore(), f));
			Resource randomResource = Resource.Coalesce(picIdToRandomResource);
			EvalStats randomEvalStats = EvalStats.Coalesce(picIdToRandomEvalStats);
			Util.writeStringToFile(outputfileRandom, 
					Util.format(Util.getAve(randomResource.fileSizeList()), f)+"\t"+
					Util.format(randomEvalStats.getFScore(), f));
		}
		
		{// confidence-accuracy correlation
			String outputfile = AnalysisResultFolder
					+ "confidence-fscore-bucketized-type-"+Type
					+ "-category-"+Category.getNameOfCategory(target)+".csv";
			String kendallOutputfile = AnalysisResultFolder
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
					double fscore = evalStats.getFScore();
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

}
