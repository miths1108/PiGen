package in.ac.iisc.cds.dsl.dirty;

import in.ac.iisc.cds.dsl.cdgvendor.model.ValueCombination;
import it.unimi.dsi.fastutil.ints.IntList;

public class DirtyValueCombinationWithProjectionValues extends ValueCombination {
	
	final private ProjectionValues projectionValues;

	public DirtyValueCombinationWithProjectionValues(IntList colValues, long rowcount, ProjectionValues projectionValues) {
		super(colValues, rowcount);
		this.projectionValues = projectionValues;
		
		if (projectionValues != null && rowcount != projectionValues.checkCountConssistency())
			throw new RuntimeException("Projection count != rowcount");
	}

	public DirtyValueCombinationWithProjectionValues(ValueCombination reverseMappedValueCombination, ProjectionValues projectionValues) {
		this(reverseMappedValueCombination.getColValues(), reverseMappedValueCombination.getRowcount(), projectionValues);
	}
	
	public ProjectionValues getProjectionValues() {
		return projectionValues;
	}
	
	@Override
	public boolean equals(Object obj) {
		throw new RuntimeException("Not implemented!");
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		if (projectionValues != null)
			result = result * prime + projectionValues.hashCode();
		return result;
	}

	@Override
    public String toString() {
		if (projectionValues != null)
			return super.toString() + "\n" + projectionValues + "\n";
		else
			return super.toString() + "\n";
    }
}
