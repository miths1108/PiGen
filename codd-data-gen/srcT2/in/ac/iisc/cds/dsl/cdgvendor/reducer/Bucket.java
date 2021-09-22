package in.ac.iisc.cds.dsl.cdgvendor.reducer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * A bucket corresponds to an attribute.
 * It is just a List<Integer> representing the values available for this attribute.
 * @author dsladmin
 *
 */
public class Bucket implements Serializable{

    private final List<Integer> valList;
    public static final Bucket  EMPTY_BUCKET = new Bucket();

    public Bucket() {
        valList = new ArrayList<>();
    }

    /**
     * Does deep copy
     * @param another
     */
    public Bucket(Bucket another) {
        valList = new ArrayList<>();
        valList.addAll(another.valList);
    }

    public void add(int val) {
        valList.add(val);
    }
    
    public Bucket deepCopy() {
    	Bucket bNew=new Bucket();
    	for(Integer i: this.getAll())
    		bNew.add(i);
    	return bNew;
    }
    public void clear() {
    	valList.clear();
    }

    public int min() {
        if (isEmpty()) {
            throw new RuntimeException("Getting min of empty bucket");
        }
        return at(0);
    }
    
    public List<Integer> getAll(){
    	return valList;
    }

    public int at(int index) {
        return valList.get(index);
    }

    public boolean contains(int val) {
        return valList.contains(val);
    }

    public int size() {
        return valList.size();
    }

    //Shadab
    public void addAll(Bucket b) {
    	valList.addAll(b.valList);
    	Collections.sort(valList);
    }
    public boolean isEqual(Bucket another) {
    	if (valList.size() != another.size())
    		return false;
    	for (int i=0; i<valList.size();i++) {
    		if (valList.get(i)!=another.at(i))
    			return false;
    	}
    	return true;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        for (int val : valList) {
            sb.append(val + ",");
        }
        String str = sb.toString();
        str = str.substring(0, str.length() - 1);
        return str + "]";
    }

    /************************************************************
     * Some primitive operations
     ************************************************************/
    public Bucket intersection(Bucket b2) {
        Bucket result = new Bucket(this);
        Bucket minusBucket = new Bucket(this);
        minusBucket = minusBucket.minus(b2);
        result = result.minus(minusBucket);
        if (result.valList.isEmpty()) {
            return EMPTY_BUCKET;
        }
        return result;
    }

    public boolean isEmpty() {
        return valList.isEmpty();
    }

    public Bucket minus(Bucket b2) {
        Bucket result = new Bucket(this);
        result.valList.removeAll(b2.valList);
        if (result.valList.isEmpty()) {
            return EMPTY_BUCKET;
        }
        return result;
    }

    /**
     * returns compareTo of min values of both buckets
     * @param b2
     * @return
     */
    public int compareTo2(Bucket b2) {
        if (isEmpty() || b2.isEmpty()) {
            throw new RuntimeException("Comparing empty bucket on min value");
        }
        return at(0) - b2.at(0);

    }
    public static Bucket getIntersection(Bucket b1,Bucket b2) {
        Bucket result = new Bucket(b1);
        Bucket minusBucket = new Bucket(b1);
        minusBucket = minusBucket.minus(b2);
        result = result.minus(minusBucket);
        if (result.valList.isEmpty()) {
            return null;
        }
        return result;
    }
    public static Bucket union(Bucket b1,Bucket b2) {
    	if(b1.size()==0)
    		return b2;
    	else if (b2.size()==0)
    		return b1;
    	
    	Bucket result=new Bucket();
    	int i,j;
    	for ( i = 0, j = 0; i < b1.size() && j < b2.size();) {
    	    if (b1.at(i) < b2.at(j)) {
    	        result.add(b1.at(i));
    	        i++;
    	    } else if (b1.at(i) > b2.at(j))  {
    	        result.add(b2.at(j));
    	        j++;
    	    }
    	    else {
    	    	result.add(b1.at(i));
    	        i++;
    	        j++;
    	    }
    	}
    	while(i < b1.size()) result.add(b1.at(i++));
    	while(j < b2.size()) result.add(b2.at(j++));
    	//valList.addAll(b.valList);
    	//Collections.sort(valList);
    	return result;
    }

}
