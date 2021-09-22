package in.ac.iisc.cds.dsl.dirty;

import java.util.List;

public class DirtyValueIntervalWithCountAndInitValue extends DirtyValueIntervalWithCount {

	final long initValue;
	public DirtyValueIntervalWithCountAndInitValue(long start, long end, long count, long initValue) {
		super(start, end, count);
		this.initValue = initValue;
	}
	
	@Override
	public String toString() {
		return "{" + initValue + "}" + super.toString();
	}
	
	@Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (!(obj instanceof DirtyValueIntervalWithCountAndInitValue)) return false;

        DirtyValueIntervalWithCountAndInitValue that = (DirtyValueIntervalWithCountAndInitValue)obj;
        if (this.initValue != that.initValue || !super.equals((DirtyValueIntervalWithCount)that))
        	return false;
        return true;
    }

    @Override
    public int hashCode(){
    	final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int)initValue;
        return result;
    }

	public static List<DirtyValueIntervalWithCountAndInitValue> mergeIntervals(List<DirtyValueIntervalWithCountAndInitValue> origIntervals) {
		throw new RuntimeException("Not implemented!");
//		List<DirtyValueIntervalWithCountAndInitValue> result = new LinkedList<>();
//		
//		return result;
	}
}
