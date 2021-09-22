package in.ac.iisc.cds.dsl.dirty;

import in.ac.iisc.cds.dsl.cdgvendor.utils.Triplet;

public class DirtyValueIntervalWithCount extends DirtyValueInterval {

	long count;		// TODO: better name for this variable is occurrence
	public DirtyValueIntervalWithCount(long start, long end, long count) {
		super(start, end);
		this.count = count;
	}
	
	public DirtyValueIntervalWithCount getIntersection (DirtyValueIntervalWithCount that) {
		DirtyValueIntervalWithCount result = null;
		if (this.start <= that.start && that.start < this.end) {		// 2,3
			if (this.end <= that.end) {									// 2
				result = new DirtyValueIntervalWithCount(that.start, this.end, getMin(this.count, that.count));
			} else {													// 3
				result = new DirtyValueIntervalWithCount(that.start, that.end, getMin(this.count, that.count));
			}
		} else if (that.start <= this.start && this.start < that.end) {	// 1,4
			if (that.end <= this.end) {									// 1
				result = new DirtyValueIntervalWithCount(this.start, that.end, getMin(this.count, that.count));
			} else {													// 4
				result = new DirtyValueIntervalWithCount(this.start, this.end, getMin(this.count, that.count));
			}
		}
		return result;
	}
	
	public Triplet<DirtyValueIntervalWithCount, DirtyValueIntervalWithCount, DirtyValueIntervalWithCount> getMinus (DirtyValueIntervalWithCount that) {
		DirtyValueIntervalWithCount leftLeftover = null;
		DirtyValueIntervalWithCount centreLeftover = null;
		DirtyValueIntervalWithCount rightLeftover = null;
		
		if (this.start <= that.start && that.start < this.end) {		// 2,3
			if (this.end <= that.end) {									// 2
				leftLeftover = makeNonZeroInterval(this.start, that.start, this.count);
				centreLeftover = makeNonZeroInterval(that.start, this.end, getDiff(this.count, that.count));
				rightLeftover = makeNonZeroInterval(this.end, that.end, that.count);
			} else {													// 3
				leftLeftover = makeNonZeroInterval(this.start, that.start, this.count);
				centreLeftover = makeNonZeroInterval(that.start, that.end, getDiff(this.count, that.count));
				rightLeftover = makeNonZeroInterval(that.end, this.end, that.count);
			}
		} else if (that.start <= this.start && this.start < that.end) {	// 1,4
			if (that.end <= this.end) {									// 1
				leftLeftover = makeNonZeroInterval(that.start, this.start, that.count);
				centreLeftover = makeNonZeroInterval(this.start, that.end, getDiff(this.count, that.count));
				rightLeftover = makeNonZeroInterval(that.end, this.end, this.count);
			} else {													// 4
				leftLeftover = makeNonZeroInterval(that.start, this.start, that.count);
				centreLeftover = makeNonZeroInterval(this.start, this.end, getDiff(this.count, that.count));
				rightLeftover = makeNonZeroInterval(this.end, that.end, that.count);
			}
		}
		
		return new Triplet<DirtyValueIntervalWithCount, DirtyValueIntervalWithCount, DirtyValueIntervalWithCount>(leftLeftover, centreLeftover, rightLeftover);
	}
	
	private DirtyValueIntervalWithCount makeNonZeroInterval(long start, long end, long count) {
		if (count == 0 || end - start == 0)
			return null;
		return new DirtyValueIntervalWithCount(start, end, count);
	}

	private long getDiff(long a, long b) {
		return Math.abs(a - b);
	}

	private long getMin(long a, long b) {
		return (a < b)? a : b;
	}
	
	public long getIntervalSizeWithoutProjection() {
		return (end - start) * count;
	}
	
	@Override
	public String toString() {
		return super.toString() + " : " + count;
	}
	
	@Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (!(obj instanceof DirtyValueIntervalWithCount)) return false;

        DirtyValueIntervalWithCount that = (DirtyValueIntervalWithCount)obj;
        if (this.count != that.count || !super.equals((DirtyValueInterval)that))
        	return false;
        return true;
    }
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = super.hashCode();
		result = result * prime + (int)count;
		return result;
	}
	
	public Triplet<DirtyValueIntervalWithCount, DirtyValueIntervalWithCount, DirtyValueIntervalWithCount> splitForDonating(long toBeDonated) {
		double tempEnd = ((1.0 * getIntervalSizeWithoutProjection() - toBeDonated) / count) + start;
		long newEnd = (long)Math.floor(tempEnd);
		long donorStart = (long)Math.ceil(tempEnd);
		
		DirtyValueIntervalWithCount donatedPartWithOriginalCount = ((end - donorStart) == 0)? null: new DirtyValueIntervalWithCount(donorStart, end, count);
		
		DirtyValueIntervalWithCount singleIntervalToKeep = null;
		DirtyValueIntervalWithCount singleIntervalToDonate = null;
		if (newEnd != donorStart) {
			long newIntervalSizeForDonation = toBeDonated % count;
			singleIntervalToKeep = new DirtyValueIntervalWithCount(newEnd, donorStart, count - newIntervalSizeForDonation);
			singleIntervalToDonate = new DirtyValueIntervalWithCount(newEnd, donorStart, newIntervalSizeForDonation);
		}
		end = newEnd;
		return new Triplet<DirtyValueIntervalWithCount, DirtyValueIntervalWithCount, DirtyValueIntervalWithCount>(singleIntervalToKeep, donatedPartWithOriginalCount, singleIntervalToDonate);
	}

	/*
	public Triplet<DirtyValueIntervalWithCount, DirtyValueIntervalWithCount, DirtyValueIntervalWithCount> splitForDonating(long toBeDonated) {
		double temp = (1.0 * getIntervalSizeWithoutProjection() - toBeDonated) / getIntervalSizeWithProjection();
		long newCountForThisInterval = (long)Math.floor(temp);
		long donorCountForThisInterval = count - (long)Math.ceil(temp);
		
		DirtyValueIntervalWithCount donatedPartOfThisInterval = (donorCountForThisInterval == 0)? null: new DirtyValueIntervalWithCount(start, end, donorCountForThisInterval); 
		
		DirtyValueIntervalWithCount newIntervalToKeep = null;
		DirtyValueIntervalWithCount newIntervalToDonate = null;
		if (donorCountForThisInterval != newCountForThisInterval) {
			long newIntervalSizeForDonation = toBeDonated % getIntervalSizeWithoutProjection();
			newIntervalToKeep = new DirtyValueIntervalWithCount(start, end - newIntervalSizeForDonation, 1);
			newIntervalToDonate = new DirtyValueIntervalWithCount(end - newIntervalSizeForDonation, end, 1);
		}
		count = newCountForThisInterval;
		return new Triplet<DirtyValueIntervalWithCount, DirtyValueIntervalWithCount, DirtyValueIntervalWithCount>(newIntervalToKeep, donatedPartOfThisInterval, newIntervalToDonate);
	}*/
}
