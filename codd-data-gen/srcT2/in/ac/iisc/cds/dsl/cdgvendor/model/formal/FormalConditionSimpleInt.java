package in.ac.iisc.cds.dsl.cdgvendor.model.formal;

public class FormalConditionSimpleInt extends FormalConditionSimple {

    private static final long serialVersionUID = 5367658934225245607L;
    protected int             value;
    
    public FormalConditionSimpleInt(FormalConditionSimple formalConditionSimple, int value) {
        super(formalConditionSimple);

        this.value = value;
    }
    
    public FormalConditionSimpleInt(FormalConditionSimpleInt formalConditionSimpleInt, int value) {
        super(formalConditionSimpleInt.getColumnname(), formalConditionSimpleInt.symbol.symbolString);

        outputCardinality = formalConditionSimpleInt.outputCardinality;
        viewname = formalConditionSimpleInt.viewname;

        this.value = value;
    }

    //deserializes from strRep like "T7_date_dim_C10_d_dow EQ 1"
    public FormalConditionSimpleInt(String strRep, long outputCardinality, String viewname) {
        super(strRep.split(" ")[0], strRep.split(" ")[1]);

        this.outputCardinality = outputCardinality;
        this.viewname = viewname;

        value = Integer.parseInt(strRep.split(" ")[2]);
    }

    @Override
    public String asString() {
        return super.asString() + " " + value;
    }

    @Override
    public String asQueryString() {
        return super.asQueryString() + " " + value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + value;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FormalConditionSimpleInt other = (FormalConditionSimpleInt) obj;
        if (value != other.value)
            return false;
        return true;
    }

    @Override
    public FormalConditionSimpleInt getDeepCopy() {
//        throw new UnsupportedOperationException("Not Implemented yet");
    	
    	FormalConditionSimpleInt another = new FormalConditionSimpleInt(this, value);
        return another;
    }

}
