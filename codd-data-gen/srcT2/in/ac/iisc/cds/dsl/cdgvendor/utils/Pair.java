package in.ac.iisc.cds.dsl.cdgvendor.utils;

import java.io.Serializable;

public class Pair<F, S> implements Serializable {
	private static final long serialVersionUID = 12L;
    private F first; //first member of pair
    private S second; //second member of pair

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public S getSecond() {
        return second;
    }

    public void setSecond(S second) {
        this.second = second;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = first != null ? first.hashCode() : 0;
        result = prime * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
    	if (this == obj) return true;
    	if (getClass() != obj.getClass())
    		return false;
    	@SuppressWarnings("unchecked")
    	Pair<F, S> that = (Pair<F, S>)obj;
    	return ((  this.first == that.first ||
                    ( this.first != null && that.first != null &&
                      this.first.equals(that.first))) &&
                 (  this.second == that.second ||
                    ( this.second != null && that.second != null &&
                      this.second.equals(that.second))) );
    }

    public String toString()
    { 
           return "(" + first + ", " + second + ")"; 
    }
}