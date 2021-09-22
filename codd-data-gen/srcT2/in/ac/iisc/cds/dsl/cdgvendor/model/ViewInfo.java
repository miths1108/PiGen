package in.ac.iisc.cds.dsl.cdgvendor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewInfo {
    private final Set<String>  tableNonkeys;  //all other than key columns in the view without after transitively adding nonkeys
    private final Set<String>  viewNonkeys;   //all other than key columns in the view after transitively adding nonkeys
    private final List<String> fkeyViews;     //list of views to which current view holds an fkey
    private final int          topoSeqno;
    private long               rowcount;
    private Boolean            isNeverFKeyed; //true iff some view holds FK to this view

    public ViewInfo(int seqno, int rowcount, Set<String> fkeyViews) {
        tableNonkeys = new HashSet<>();
        viewNonkeys = new HashSet<>();
        topoSeqno = seqno;
        this.rowcount = rowcount;
        this.fkeyViews = new ArrayList<>(fkeyViews);
        Collections.sort(this.fkeyViews); //important to have it as an ordered list as the fkey columns are not named in compressed database summary
        isNeverFKeyed = null;
    }

    public boolean getIsNeverFKeyed() {
        if (isNeverFKeyed == null)
            throw new RuntimeException("Trying to get isNeverFKeyed while it is still not initialized");
        return isNeverFKeyed;
    }

    public void setIsNeverFKeyed(boolean isNeverFKeyed) {
        this.isNeverFKeyed = isNeverFKeyed;
    }

    public Set<String> getTableNonkeys() {
        return tableNonkeys;
    }

    public Set<String> getViewNonkeys() {
        return viewNonkeys;
    }

    public int getTopoSeqno() {
        return topoSeqno;
    }

    public void scaleRowCount(long scaleFactor) {
        rowcount *= scaleFactor;
    }

    public long getRowcount() {
        return rowcount;
    }

    public List<String> getFkeyViews() {
        return fkeyViews;
    }

    public int getNonPkeyColCount() {
        return tableNonkeys.size() + fkeyViews.size();
    }

    @Override
    public String toString() {
        return topoSeqno + ": " + rowcount + ") " + viewNonkeys + " fkeys to: " + fkeyViews;
    }

}
