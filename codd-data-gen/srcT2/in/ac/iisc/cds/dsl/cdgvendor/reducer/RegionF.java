package in.ac.iisc.cds.dsl.cdgvendor.reducer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegionF implements Serializable {
	private static final long serialVersionUID = 9L;
	
	private final List<BucketStructureF> bsList;

    //public static final Region          EMPTY_REGION = new Region();
    public Double splitPoint, nextSplitPoint,intervalSize;
    public long splitCount, rowCount, cutOff;
    public long counter=0L;
    public RegionF() {
        bsList = new ArrayList<>();
        counter=0L;
    }

    public List<Double> generateNextPartialTuple() {
    	if(counter==rowCount)
    	{
    		counter=0L;
    		return null;
    	}
    	List<Double>tuple=new ArrayList<>();
    	int tupleSize=bsList.get(0).size();
    	//double []partialTuple=new double[tupleSize];
    	Double point=splitPoint + (double) (counter++ + cutOff) * (intervalSize / splitCount);
    	//partialTuple[0]=point;
    	tuple.add(point);
    	for(int i=1;i<tupleSize;i++) {
    		//partialTuple[i]=bsList.get(0).at(i).at(0);
    		tuple.add(bsList.get(0).at(i).at(0));
    	}
    	return tuple;
    	
    }
    public void add(BucketStructureF val) {
        bsList.add(val);
    }

    public void addAll(List<BucketStructureF> vals) {
        bsList.addAll(vals);
    }

    public BucketStructureF at(int index) {
        return bsList.get(index);
    }

    public int size() {
        return bsList.size();
    }

    public List<BucketStructureF> getAll() {
        return bsList;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        for (BucketStructureF bs : bsList) {
            sb.append(bs + "\n");
        }
        String str = sb.toString();
        str = str.substring(0, str.length() - 1);
        return str + "]";
    }
  //Shadab
    public void remove(int ind) {
    	bsList.remove(ind);
    }
}
