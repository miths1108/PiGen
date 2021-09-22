package in.ac.iisc.cds.dsl.cdgvendor.model;

public class SortPair<T> {
    final Comparable<T> comparable;
    int                 index;

    public SortPair(Comparable<T> comparable) {
        this.comparable = comparable;
        this.index = -1;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
