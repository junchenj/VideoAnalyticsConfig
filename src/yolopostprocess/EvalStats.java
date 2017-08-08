package yolopostprocess;

import java.util.*;

import analysis.Util;

public class EvalStats{
	Map<BoxObj, BoxObj> mObjIdentifiedToObjGroundTruth = 
			new HashMap<BoxObj, BoxObj>();
	List<BoxObj> mObjsFalsePositive = new ArrayList<BoxObj>();
	List<BoxObj> mObjsFalseNegative = new ArrayList<BoxObj>();

	public double numOfObjIdentified(){
		return mObjsFalsePositive.size()+mObjIdentifiedToObjGroundTruth.size();
	}
//	public double getFscore() {
//		return this.toEval().fScoreList().get(0);
//	}
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
	public double getPrecision(){
		double truePositiveCount = mObjIdentifiedToObjGroundTruth.size();
		double falsePositiveCount = mObjsFalsePositive.size();
		return truePositiveCount/
				(truePositiveCount+falsePositiveCount+Double.MIN_NORMAL);
	}
	public double getRecall(){
		double falseNegativeCount = mObjsFalseNegative.size();
		double truePositiveCount = mObjIdentifiedToObjGroundTruth.size();
		return truePositiveCount/
				(truePositiveCount+falseNegativeCount+Double.MIN_NORMAL);	
	}
	public double getFScore(){
		double p = getPrecision();
		double r = getRecall();
		return 2*(p*r)/(p+r+Double.MIN_NORMAL);
	}
//	public Eval toEval(){
//		double falseNegativeCount = mObjsFalseNegative.size();
//		double truePositiveCount = mObjIdentifiedToObjGroundTruth.size();
//		double falsePositiveCount = mObjsFalsePositive.size();
//		
//		double Precision = truePositiveCount/
//				(truePositiveCount+falsePositiveCount+Double.MIN_NORMAL);
//		double Recall = truePositiveCount/
//				(truePositiveCount+falseNegativeCount+Double.MIN_NORMAL);
//		return Eval.ofSingle(Precision, Recall);
//	}
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
	public String printSelf() {
		return "Precision"
				+"\t"+Util.format(getPrecision(), "#.###")
				+"\tRecall"
				+"\t"+Util.format(getRecall(), "#.###")
				+"\tF1"
				+"\t"+Util.format(getFScore(), "#.###");
	}
	public static List<Double> GetFScoreList(Map<String, EvalStats> keyToEvalStats) {
		List<Double> result = new ArrayList<Double>();
		for (String key : keyToEvalStats.keySet())
			result.add(keyToEvalStats.get(key).getFScore());
		return result;
	}
	
//	public static class Eval{
//		private List<Double> mPrecisionList;
//		private List<Double> mRecallList;
//		public Eval(List<Double> pList, List<Double> rList) {
//			mPrecisionList = new ArrayList<Double>(pList);
//			mRecallList = new ArrayList<Double>(rList);
//		}
//		public List<Double> predicisionList(){return mPrecisionList;}
//		public List<Double> recallList(){return mRecallList;}
//		public List<Double> fScoreList(){
//			List<Double> result = new ArrayList<Double>();
//			for (int i = 0; i < mPrecisionList.size(); i++){
//				double p = mPrecisionList.get(i);
//				double r = mRecallList.get(i);
//				result.add(2*(p*r)/(p+r+Double.MIN_NORMAL));
//			}
//			return result;
//		}
//		public static Eval ofSingle(double p, double r) {
//			return new Eval(Arrays.asList(p), Arrays.asList(r));
//		}
//		public static Eval Coalesce(List<Eval> itemList) {
//			List<Double> precisionList = new ArrayList<Double>();
//			List<Double> recallList = new ArrayList<Double>();
//			for (Eval item : itemList){
//				precisionList.addAll(item.mPrecisionList);
//				recallList.addAll(item.mRecallList);
//			}
//			return new Eval(precisionList, recallList);
//		}
//		public static Eval Coalesce(Map<String, Eval> keyToItem) {
//			return Eval.Coalesce(new ArrayList<Eval>(
//					keyToItem.values()));
//		}
//		public String printSelf() {
//			List<Double> fscores = fScoreList();
//			return "Precision"
//					+"\t"+Util.format(Util.getAve(mPrecisionList), "#.###")
//					+"\t"+Util.format(Util.getStdErrOfMean(mPrecisionList), "#.###")
//					+"\tRecall"
//					+"\t"+Util.format(Util.getAve(mRecallList), "#.###")
//					+"\t"+Util.format(Util.getStdErrOfMean(mRecallList), "#.###")
//					+"\tF1"
//					+"\t"+Util.format(Util.getAve(fscores), "#.###")
//					+"\t"+Util.format(Util.getStdErrOfMean(fscores), "#.###");
//		}
//		
//	}

}

