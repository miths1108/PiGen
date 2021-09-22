package in.ac.iisc.cds.dsl.cdgvendor.utils;

public class MutablePair<F, S> {
    private F first; //first member of pair
    private S second; //second member of pair

    public MutablePair(F first, S second) {
        this.first = first;
        this.second = second;
    }
    
    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }
    
    public void setFirst(F first) {
    	this.first = first;
    }
    
    public void setSecond(S second) {
    	this.second = second;
    }
    
    public String toString () {
    	StringBuilder sb = new StringBuilder();
    	if(first != null)
    		sb.append(first.toString());
    	sb.append(" , ");
    	if (second != null)
    		sb.append(second.toString());
    	return sb.toString();
    }
}