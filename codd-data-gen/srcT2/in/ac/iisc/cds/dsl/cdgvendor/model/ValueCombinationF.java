package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import in.ac.iisc.cds.dsl.cdgvendor.reducer.Bucket;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketStructure;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;



public class ValueCombinationF implements Serializable {
	private static final long serialVersionUID = 3L;
    private static final String COMMA            = ",";

    public List<Double>       colValues;
    public long                rowcount;

    public ValueCombinationF() {
    	this.colValues=new ArrayList<>();
    	
    }
    public ValueCombinationF(List<Double> colValues, long rowcount) {
        this.colValues = colValues;
        this.rowcount = rowcount;
    }

    public String toStringFileDump() {
        StringBuilder sb = new StringBuilder();
        sb.append(rowcount);
        for ( ListIterator<Double>iter = colValues.listIterator(); iter.hasNext();) {
            sb.append(COMMA).append(iter.next());
        }
        return sb.toString();
    }
	public boolean isPartOf(Region region) {
		for(BucketStructure bs:region.getAll()) {
			boolean found=true;
			for(int i=0;i<bs.size();i++) {
				
				Bucket b=bs.at(i);
				if(!contains(b,colValues.get(i))) {
					found=false;
					break;
				}
				
			}
			if(found)
				return found;
				
		}
		return false;
		
	}
	private boolean contains(Bucket b, Double Double1) {
		// TODO Auto-generated method stub
		Integer floored=(int) Math.floor(Double1);
		if(b.contains(floored))
			return true;
		return false;
	}

    /**
     * Only colValues appear
     * @return
     */
   

}
