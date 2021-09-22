package in.ac.iisc.cds.dsl.cdgvendor.model;

import in.ac.iisc.cds.dsl.cdgvendor.reducer.Region;

public class VariableValuePair {
    public Region variable;
    public long         rowcount;

    public VariableValuePair(Region variable, long rowcount) {
        this.variable = variable;
        this.rowcount = rowcount;
    }
}