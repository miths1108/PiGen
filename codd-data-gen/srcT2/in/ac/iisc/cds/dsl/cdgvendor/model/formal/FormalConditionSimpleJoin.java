package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

public class FormalConditionSimpleJoin extends FormalConditionSimple {
    protected String columnname2;

    public FormalConditionSimpleJoin(String columnname, String columnname2) {
        super(columnname, Symbol.EQ.symbolString);

        if (columnname2.contains(DOT)) {
            columnname2 = columnname2.split(SPLITTER)[1];
        }
        this.columnname2 = columnname2;
    }

    public String getColumnname2() {
        return columnname2;
    }

    public void setColumnname2(String columnname2) {
        this.columnname2 = columnname2;
    }

    @Override
    public String asString() {
        return super.asString() + " " + columnname2;
    }

    @Override
    public String asQueryString() {
        throw new RuntimeException("Not Implemented");
    }

    public void updateColumnnames(String columnname, String columnname2) {
        this.columnname = columnname;
        this.columnname2 = columnname2;
    }

    @Override
    public FormalConditionSimpleJoin getDeepCopy() {
        FormalConditionSimpleJoin another = new FormalConditionSimpleJoin(columnname, columnname2);
        another.outputCardinality = outputCardinality;
        another.viewname = viewname;
        //another.colPath = colPath;
        return another;
    }
}
