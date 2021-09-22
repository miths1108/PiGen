package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

public class FormalConditionSimpleNumber extends FormalConditionSimple {
    protected double value;

    public FormalConditionSimpleNumber(String columnname, String symbolStr, String valueStr) {
        super(columnname, symbolStr);

        try {
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException ex) {
            throw new ExceptionInInitializerError("Unable to parse to double valueStr " + valueStr);
        }
    }

    public FormalConditionSimpleNumber(FormalConditionSimpleNumber that) {
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

    public double getValue() {
        return value;
    }

    @Override
    public FormalConditionSimpleNumber getDeepCopy() {
        FormalConditionSimpleNumber another = new FormalConditionSimpleNumber(columnname, symbol.symbolString, String.valueOf(value));
        another.outputCardinality = outputCardinality;
        another.viewname = viewname;
        another.colPath = colPath;
        return another;
    }

}
