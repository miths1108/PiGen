package in.ac.iisc.cds.dsl.dirty;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import in.ac.iisc.cds.dsl.cdgvendor.reducer.Bucket;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.BucketStructure;
import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;
import it.unimi.dsi.fastutil.ints.IntList;

public class DirtyVariableValuePairWithProjectionValues {
    public final Region variable;
    public long         rowcount;
    private final ProjectionValues projectionValues;
    
    public DirtyVariableValuePairWithProjectionValues(Region variable, long rowcount, ProjectionValues projectionValues) {
		this.variable = variable;
	    this.rowcount = rowcount;
	    this.projectionValues = projectionValues;
    }
    
    public ProjectionValues getProjectionValues() {
    	return projectionValues;
    }

	public void doSanityCheck(IntList origColIndxs) {
 		if( projectionValues != null) {
 		// sanity check: no intervals intersect, count of projection values is = count of selection values
 			for (Entry<Integer, LinkedList<DirtyValueIntervalWithCount>> entry : projectionValues.entrySet()) {
 				List<DirtyValueIntervalWithCount> intervals = entry.getValue();
 				long projCount = 0;
 				for (DirtyValueIntervalWithCount dirtyValueInterval1 : intervals) {
 					for (DirtyValueIntervalWithCount dirtyValueInterval2 : intervals) {
 						if (dirtyValueInterval1 != dirtyValueInterval2) {
 							if (dirtyValueInterval1.start < dirtyValueInterval2.start && !(dirtyValueInterval1.end-1 < dirtyValueInterval2.start)) {
 								throw new RuntimeException("Intersecting intervals found");
 							}
 							if (dirtyValueInterval2.start < dirtyValueInterval1.start && !(dirtyValueInterval2.end-1 < dirtyValueInterval1.start)) {
 								throw new RuntimeException("Intersecting intervals found");
 							}
 						}
 					}
 					projCount += dirtyValueInterval1.getIntervalSizeWithoutProjection();
 				}
 				if (projCount != rowcount)
 					throw new RuntimeException("Projection values not equal to selection");
 			}
		
 			// sanity check: For projection bucket of different bs's of same region, there should be only one and same value
        	for (Entry<Integer, LinkedList<DirtyValueIntervalWithCount>> entry : projectionValues.entrySet()) {
				int origColIndx = entry.getKey();
				int relativeColIndx = origColIndxs.indexOf(origColIndx);
				int onlyVal = -1;
				for (BucketStructure bs : variable.getAll()) {
					Bucket b = bs.getAll().get(relativeColIndx);
					if (b.size() != 1)
			        	throw new RuntimeException("Projection on region but bucket with multiple values found!");
					if (onlyVal == -1)
						onlyVal = b.getAll().get(0);
					else if (onlyVal != b.getAll().get(0))
						throw new RuntimeException("Two different values found for same column of different bs's in projection!");
					
				}
			}
        }
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(rowcount + " | " + variable + "\n");
		if (projectionValues != null)
			sb.append(projectionValues + "\n");
		return sb.toString();
	}
}
