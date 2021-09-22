package in.ac.iisc.cds.dsl.cdgvendor.utils;

public class Triplet<T, U, V> {

    private final T first;
    private final U second;
    private final V third;

    public Triplet(T first, U second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst() { return first; }
    public U getSecond() { return second; }
    public V getThird() { return third; }
    
    @Override
    public String toString () {
    	StringBuilder sb = new StringBuilder();
    	if (first != null)
    		sb.append(first.toString());
    	sb.append(" | ");
    	if (second != null)
    		sb.append(second.toString());
    	sb.append(" | ");
    	if (third != null)
    		sb.append(third.toString());
    	return sb.toString();
    }
    
    @Override
    public int hashCode() {
		throw new RuntimeException("Not implemented!");
    }

    @Override
    public boolean equals(Object obj) {
		throw new RuntimeException("Not implemented!");
    }
}