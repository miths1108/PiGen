package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

public class FormalConditionSimpleString extends FormalConditionSimple {
    protected String value;

    public FormalConditionSimpleString(String columnname, String symbolStr, String valueStr) {
        super(columnname, symbolStr);
        value = valueStr;
    }

    public FormalConditionSimpleString(FormalConditionSimpleString that) {
    	super(that);
        value = that.value;
	}

	@Override
    public String asString() {
        return super.asString() + " " + value;
    }

    @Override
    public String asQueryString() {
        return super.asQueryString() + " " + value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public FormalConditionSimpleString getDeepCopy() {
        FormalConditionSimpleString another = new FormalConditionSimpleString(columnname, symbol.symbolString, value);
        another.outputCardinality = outputCardinality;
        another.viewname = viewname;
        another.colPath = colPath;
        return another;
    }
}
