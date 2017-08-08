package yolopostprocess;

import java.util.*;

import analysis.Util;

public class Resource{
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
	public static List<Double> GetFileSizeList(Map<String, Resource> keyToResource) {
		List<Double> result = new ArrayList<Double>();
		for (String key : keyToResource.keySet())
			result.add(Util.getAve(keyToResource.get(key).fileSizeList()));
		return result;
	}
}
