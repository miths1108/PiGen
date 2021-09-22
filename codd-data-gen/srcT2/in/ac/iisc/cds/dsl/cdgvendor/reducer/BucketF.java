package in.ac.iisc.cds.dsl.cdgvendor.reducer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BucketF implements Serializable {
	private static final long serialVersionUID = 15L;
	private final List<Double> valList;
    public static final Bucket  EMPTY_BUCKET = new Bucket();

    public BucketF() {
        valList = new ArrayList<>();
    }

    /**
     * Does deep copy
     * @param another
     */
    public BucketF(BucketF another) {
        valList = new ArrayList<>();
        valList.addAll(another.valList);
    }

    public void add(Double val) {
        valList.add(val);
    }
    
    public void clear() {
    	valList.clear();
    }

    
    
    public List<Double> getAll(){
    	return valList;
    }

    public Double at(int index) {
        return valList.get(index);
    }

    public boolean contains(int val) {
        return valList.contains(val);
    }

    public int size() {
        return valList.size();
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        for (double val : valList) {
            sb.append(val + ",");
        }
        String str = sb.toString();
        str = str.substring(0, str.length() - 1);
        return str + "]";
    }
}
