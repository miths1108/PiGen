package in.ac.iisc.cds.dsl.dirty;

public class DirtyValueInterval {
	public long start;
	public long end;	// excluding end
	
	public DirtyValueInterval(long start, long end) {
		this.start = start;
		this.end = end;
	}
	
	public long getIntervalSizeWithProjection() {
		return end - start;
	}
	
	@Override
	public String toString() {
		return "[" + start + ", " + end + ")";
	}
	
	@Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (!(obj instanceof DirtyValueInterval)) return false;

        DirtyValueInterval that = (DirtyValueInterval)obj;
        if (this.start != that.start || this.end != that.end)
        	return false;
        return true;
    }

    @Override
    public int hashCode(){
    	final int prime = 31;
        int result = (int)start;
        result = prime * result + (int)end;
        return result;
    }
}
