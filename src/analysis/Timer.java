package analysis;


import java.io.Serializable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: junchenjiang
 * Date: 5/11/15
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class Timer implements Serializable{

    private List<String> _nameList = new ArrayList<String>();
    private Map<String, Long> _nameToStartTime = new HashMap<String, Long>();
    private Map<String, Long> _nameToEndTime = new HashMap<String, Long>();
    private List<String> _metaList = new ArrayList<String>();
    private Map<String, List<String>> _metaToNameList =
            new HashMap<String, List<String>>();

    private void markStart(long time, String name){
        _nameList.add(name);
        _nameToStartTime.put(name, time);
    }

    private void markEnd(long time, String name){
        if (_nameToStartTime.containsKey(name))
            _nameToEndTime.put(name, time);
        else {
//            System.out.println("ERROR: inserting "+name+" without start time!");
        	System.out.println("Debugging: "+name+" not in "+_nameToStartTime.keySet());
        	throw new RuntimeException(
        			"ERROR: inserting "+name+" without start time!");
        }
    }

    public void markStartWithMetaNameId(String meta, Object id){
        long time = System.currentTimeMillis();
        String name = meta+"-"+id;
        if (!_metaList.contains(meta)) _metaList.add(meta);
        markStart(time, name);
        List<String> nameList = _metaToNameList.containsKey(meta) ?
                _metaToNameList.get(meta) : new ArrayList<String>();
        nameList.add(name);
        _metaToNameList.put(meta, nameList);
    }

    public void markEndWithMetaNameId(String meta, Object id){
        long time = System.currentTimeMillis();
        String name = meta+"-"+id;
        markEnd(time, name);
    }

    public String debugStart(){
        String str = "";
        for (int i = 0; i < _metaList.size(); i++){
            String meta = _metaList.get(i);
            List<String> list = _metaToNameList.get(meta);
            for (int j = 0; j < list.size(); j++){
                String name = list.get(j);
                if (!_nameToStartTime.containsKey(name))
                    str = str+"\t"+name+" not found";
            }
        }
        return "DEBUG =====> "+str;
    }

    public String print(){
        long time1 = System.currentTimeMillis();
        String result = "";
        for (int i = 0; i < _metaList.size(); i++){
            String meta = _metaList.get(i);
            List<Double> lens = new ArrayList<Double>();
            List<String> list = _metaToNameList.get(meta);
            boolean allFinished = true;
            for (int j = 0; j < list.size(); j++){
                String name = list.get(j);
                long startTime = _nameToStartTime.get(name);
                if (!_nameToEndTime.containsKey(name)) {
                    allFinished = false; break;
                }
                long endTime = _nameToEndTime.get(name);
                lens.add((double)(endTime-startTime));
            }
            if (allFinished){
                result += "name="+meta+
                        "\tmean="+msToSecStr(Util.getAve(lens))+"sec"+
                        "\tc="+lens.size()+
                        "\tsum="+msToSecStr(Util.getSum(lens))+"sec"+
                        "\tmin="+msToSecStr(Util.getPerc(lens, 0.0))+"sec"+
                        "\tmed="+msToSecStr(Util.getPerc(lens, 0.5))+"sec"+
                        "\tmax="+msToSecStr(Util.getPerc(lens, 1.0))+"sec\r\n";
            }
            else
                result += "name="+meta+" unfinished\r\n";
        }
        long time2 = System.currentTimeMillis();
        System.out.println("printing takes "+(time2-time1)+" ms");
        return result;
    }

    private String msToSecStr(double msDouble){
        return Util.format(msDouble/1000.0, "#.####");
    }
}

